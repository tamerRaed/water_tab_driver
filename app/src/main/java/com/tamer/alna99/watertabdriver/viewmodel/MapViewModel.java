package com.tamer.alna99.watertabdriver.viewmodel;

import android.util.Log;

import com.tamer.alna99.watertabdriver.model.DataWrapper;
import com.tamer.alna99.watertabdriver.model.NetworkUtils;
import com.tamer.alna99.watertabdriver.model.Result;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapViewModel {
    private final DataWrapper<Result> dataWrapper;
    private final NetworkUtils networkUtils;

    public MapViewModel() {
        networkUtils = NetworkUtils.getInstance();
        dataWrapper = new DataWrapper<>();
    }

    public DataWrapper<Result> updateResult() {
        return dataWrapper;
    }

    public void updateLocation(String driverID, double[] location) {
        networkUtils.getApiInterface().updateLocation(driverID, location).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        dataWrapper.setData(Result.success(response.body().string()));
                        Log.d("dddd", response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    dataWrapper.setData(Result.error("Error"));
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                dataWrapper.setData(Result.error("Error"));
            }
        });
    }

}
