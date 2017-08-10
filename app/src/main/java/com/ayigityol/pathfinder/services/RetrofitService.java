package com.ayigityol.pathfinder.services;

import android.graphics.Color;
import android.util.Log;

import com.ayigityol.pathfinder.MapsActivity;
import com.ayigityol.pathfinder.POJO.places.Info;
import com.ayigityol.pathfinder.POJO.paths.Path;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class RetrofitService {

    private static final String url = "https://maps.googleapis.com/maps/";
    static public Polyline line = null;

    private static RetrofitConnector initRetrofitService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RetrofitConnector.class);
    }


    public static void drawPath() {

        //  connection service
        RetrofitConnector service = initRetrofitService();
        Call<Path> call = service.getDistanceDuration("metric", MapsActivity.origin.latitude + "," + MapsActivity.origin.longitude, MapsActivity.destination.latitude + "," + MapsActivity.destination.longitude);

        //  send request and handle callback
        call.enqueue(new Callback<Path>() {
            @Override
            public void onResponse(Response<Path> response, Retrofit retrofit) {

                try {
                    //  Remove previous line from map
                    if (RetrofitService.line != null) {
                        line.remove();
                    }
                    //  This loop will draw line from
                    //  responses JSON body
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();

                        List<LatLng> list = decodePoly(encodedString);
                        line = MapsActivity.mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(15)
                                .color(Color.RED)
                                .geodesic(true)
                        );
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }


    //  code taken web resource
    //  decodes Ploylines from request
    private static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }


    // get Coordinate by id
    public static void getCoordinatesById(String placeid, final String owner) {

        //  connection service
        RetrofitConnector service = initRetrofitService();
        Call<Info> call = service.getPlaceLatLng(placeid);

        //  send reuqest handle calback
        //
        call.enqueue(new Callback<Info>() {
            @Override
            public void onResponse(Response<Info> response, Retrofit retrofit) {

                try {
                    if(owner.equals("from")){
                        MapsActivity.origin = new LatLng(response.body().getResult().getGeometry().getLocation().getLat(),
                                response.body().getResult().getGeometry().getLocation().getLng());
                        MapsActivity.lastSearch.setFrom(response.body());

                    }
                    else{
                        MapsActivity.destination = new LatLng(response.body().getResult().getGeometry().getLocation().getLat(),
                                response.body().getResult().getGeometry().getLocation().getLng());
                        MapsActivity.lastSearch.setTo(response.body());
                    }

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });

    }




}
