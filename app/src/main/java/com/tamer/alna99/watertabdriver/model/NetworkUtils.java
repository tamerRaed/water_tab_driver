package com.tamer.alna99.watertabdriver.model;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class NetworkUtils {

    private static NetworkUtils instance;
    public static final String BASE_URL = "https://miahy.herokuapp.com/";
    private final ApiInterface apiInterface;

    private NetworkUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("User-Agent", Objects.requireNonNull(System.getProperty("http.agent")))
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static NetworkUtils getInstance() {
        if (instance == null) {
            instance = new NetworkUtils();
        }
        return instance;
    }

    public ApiInterface getApiInterface() {
        return apiInterface;
    }

}
