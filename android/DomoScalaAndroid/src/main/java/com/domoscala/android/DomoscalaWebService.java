package com.domoscala.android;

import com.domoscala.android.messages.BuildingsResponse;
import com.domoscala.android.messages.SetDeviceStatusRequest;
import com.domoscala.android.messages.SetDeviceStatusResponse;
import retrofit.Callback;
import retrofit.http.*;

/**
 * This is an interface to be used with Retrofit Java library to perform HTTP RESTful requests to the DomoScala server
 */
public interface DomoscalaWebService {

    @GET("/buildings")
    public void getBuildings(Callback<BuildingsResponse> callback);


    @PUT("/{building}/{room}/{device}")
    public void setDeviceStatus(
            @Path("building") String buildingName,
            @Path("room") String roomName,
            @Path("device") String deviceName,
            @Body SetDeviceStatusRequest request,
            Callback<SetDeviceStatusResponse> callback
    );

}
