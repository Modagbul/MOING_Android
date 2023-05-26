package com.example.moing.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClientJwt {
    private static final String BASE_URL = "http://13.209.196.123";
    private static final long CONNECT_TIMEOUT_SEC = 20L;
    private static Retrofit retrofit = null;

    public static Retrofit getInstance(final String token) {

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.addInterceptor(loggingInterceptor)
                    .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Authorization", token)
                                    .method(original.method(), original.body());
                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    });

            Gson gson = new GsonBuilder().setLenient().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(clientBuilder.build())
                    .build();

        return retrofit;
    }

    public static RetrofitAPI getApiService(String token) {
        return getInstance(token).create(RetrofitAPI.class);
    }
}
