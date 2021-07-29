package com.tamer.alna99.watertabdriver.model;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    String LOGIN_PATH = "api/users/logInDriver";
    String UPDATE_LOCATION_PATH = "api/users/updateDriverLocation";

    String ID_PARAM = "driverID";
    String LOCATION_PARAM = "location";
    String PARAM_EMAIL = "email";
    String PARAM_PASSWORD = "password";


    @FormUrlEncoded
    @POST(LOGIN_PATH)
    Call<ResponseBody> login(@Field(PARAM_EMAIL) String email, @Field(PARAM_PASSWORD) String password);

    @FormUrlEncoded
    @POST(UPDATE_LOCATION_PATH)
    Call<ResponseBody> updateLocation(@Field(ID_PARAM) String driverID, @Field(LOCATION_PARAM) double[] location);
}
