package com.tamer.alna99.watertabdriver.model;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class NetworkUtils {

    private static NetworkUtils instance;
    public static final String BASE_URL = "https://miahy.herokuapp.com/";
    private final ApiInterface apiInterface;
    String PARAM_EMAIL = "email";
    String PARAM_PASSWORD = "password";

    private NetworkUtils() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
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

//    public HashMap<String, String> getLoginParams(String email, String password){
//        HashMap<String, String> map = new HashMap<>();
//        map.put(PARAM_EMAIL, email);
//        map.put(PARAM_PASSWORD, password);
//        return map;
//    }

}
