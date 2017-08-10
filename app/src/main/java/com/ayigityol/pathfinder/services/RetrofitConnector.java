package com.ayigityol.pathfinder.services;

import com.ayigityol.pathfinder.POJO.places.Info;
import com.ayigityol.pathfinder.POJO.paths.Path;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;



public interface RetrofitConnector {
    /*
   * Retrofit Connection Interface
   * @METHOD_NAME('/ENDPOINT_URL/')
   * @Query: Get methodunun query Parametreleri için.
   * Path objesi dönüyor
   */

    //  direction matrix end point
    @GET("api/directions/json?key=GOOGLE-SERVER-API-KEY")
    Call<Path> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination);

    //  place_id to coordinate
    @GET("api/place/details/json?key=GOOGLE-SERVER-API-KEY")
    Call<Info> getPlaceLatLng(@Query("placeid") String placeid);

}
