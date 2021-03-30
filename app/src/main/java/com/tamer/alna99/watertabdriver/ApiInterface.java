package com.tamer.alna99.watertabdriver;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    String LOGIN_PATH = "/api/users/logInDriver";
    String REGISTER_PATH = "api/users/registerDriver";
    String PARAM_EMAIL = "email";
    String PARAM_PASSWORD = "password";
    String PARAM_NAME = "name";
    String PARAM_LONG = "long";
    String PARAM_LAT = "lat";

    @FormUrlEncoded
    @POST(LOGIN_PATH)
    Call<ResponseBody> login(@Field(PARAM_EMAIL) String email, @Field(PARAM_PASSWORD) String password);

    @FormUrlEncoded
    @POST(REGISTER_PATH)
    Call<ResponseBody> register(@Field(PARAM_NAME) String name,
                                @Field(PARAM_EMAIL) String email,
                                @Field(PARAM_LONG) String longitude,
                                @Field(PARAM_LAT) String latitude,
                                @Field(PARAM_PASSWORD) String password);

}
