package com.ayigityol.pathfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ayigityol.pathfinder.POJO.places.SearchLine;
import com.ayigityol.pathfinder.adapters.PlaceAutocompleteAdapter;
import com.ayigityol.pathfinder.services.RetrofitService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    //  constants
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final String sharedPrefFileKey = "HistoryJSON";

    //  statics (used in services package
    public static GoogleMap mMap;
    public static LatLng origin = null, destination = null;
    public static SearchLine lastSearch;

    //  class attributes
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView fromAutoCompl, toAutoCompl;
    private FloatingActionButton navigateButton;
    private TextView historyNotFound;
    private LinearLayout historyDatabaseContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //// TODO: 10.08.2017
        //  SearchLine DB row initialize
        lastSearch = new SearchLine();

        //  API Initialization for Places Autocomplete Text View
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        //  UI Components
        fromAutoCompl = (AutoCompleteTextView) findViewById(R.id.fromAutoCompl);
        toAutoCompl = (AutoCompleteTextView) findViewById(R.id.toAutoCompl);
        navigateButton = (FloatingActionButton) findViewById(R.id.findPath);
        historyNotFound = (TextView) findViewById(R.id.historyNotFound);
        historyDatabaseContainer = (LinearLayout) findViewById(R.id.historyDatabaseContainer);


        //  set on item click listeners to Autocomplete Text Views
        fromAutoCompl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get chosen place
                final AutocompletePrediction item = mAdapter.getItem(i);

                //find coordinates by place id
                // using retrofit http library and put a marker to map
                RetrofitService.getCoordinatesById(item.getPlaceId(), "from");
                // go to next autocomplete text view.
                //for better ux experience
                findViewById(android.R.id.content).findViewById(R.id.toAutoCompl).requestFocus();
            }
        });
        toAutoCompl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final AutocompletePrediction item = mAdapter.getItem(i);
                RetrofitService.getCoordinatesById(item.getPlaceId(), "to");

            }
        });


        //  set autocomplete adapter for predictions
        //  3. parameter is boundary places of suggestions @TR boundaries
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, new LatLngBounds(
                new LatLng(35.9025, 25.90902), new LatLng(42.02683, 44.5742)),
                null);
        fromAutoCompl.setAdapter(mAdapter);
        toAutoCompl.setAdapter(mAdapter);


        //  find & draw a path on click
        //  record path to history
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gsonBuilder = new Gson();
                ArrayList<SearchLine> searchHistory;

                if (origin != null && destination != null) {

                    //  clear markers since we will add 2 of them again
                    mMap.clear();
                    MarkerOptions org = new MarkerOptions();
                    MarkerOptions dest = new MarkerOptions();
                    org.position(origin);
                    dest.position(destination);

                    //  set colours to
                    org.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    dest.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    //  add map
                    mMap.addMarker(org);
                    mMap.addMarker(dest);

                    //  update map camera to first market
                    //  so that we can see the red path
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 4));

                    //  draw path and store for the future
                    //  possible usage
                    RetrofitService.drawPath();

                    //  retrieve old records and insert current search record
                    String serializedJSONArray = PreferenceManager.
                            getDefaultSharedPreferences(getApplicationContext()).getString(sharedPrefFileKey, "");
                    Type type = new TypeToken<List<SearchLine>>() {
                    }.getType();

                    if (serializedJSONArray != "") {
                        searchHistory = gsonBuilder.fromJson(serializedJSONArray, type);
                    } else {
                        searchHistory = new ArrayList<SearchLine>();

                    }
                    searchHistory.add(lastSearch);

                    //  store current array to sp. file
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                            .putString(sharedPrefFileKey, gsonBuilder.toJson(searchHistory)).apply();

                    // update history view with new record
                    createSearchHistoryView();


                } else {

                    Toast.makeText(getApplicationContext(), "Plase chose one origin and one destination!"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });


        //update search history
        createSearchHistoryView();

        //  runtime permission for location that
        //  will used in the future.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //  Obtain the SupportMapFragment and get
        //  notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //move camera to Ankara, Turkey
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.925533, 32.866287), 11));
    }

    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //  Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //  Show an explanation to the user *asynchronously* -- don't block
                //  this thread waiting for the user's response! After the user
                //  sees the explanation, try again to request the permission.

                //  Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                //  No explanation needed,
                //  we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        //  Toast user in case of a API fail.
        Toast.makeText(this, "Connection Failed to Google Services!", Toast.LENGTH_SHORT).show();
    }

    private void createSearchHistoryView() {

        ArrayList<SearchLine> searchHistory;
        Gson gsonBuilder = new Gson();

        //  First check pass values
        String serializedJSONArray = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext()).getString(sharedPrefFileKey, "");
        Type type = new TypeToken<List<SearchLine>>() {
        }.getType();


        //  if there is a previous data: read from sheared pref. file
        //  else, open a new array to add current search inputs
        if (serializedJSONArray != "") {
            searchHistory = gsonBuilder.fromJson(serializedJSONArray, type);
        } else {
            //  no record found
            //  exit funciton
            return;
        }

        //  flush view children to avoid duplicate
        historyDatabaseContainer.removeAllViews();

        //  trace each history record
        //  if there is a record, disable historyNotFound label
        for (int i =  searchHistory.size()-1; i >= 0; i--) {

            if (i == searchHistory.size()-1) {
                historyNotFound.setVisibility(View.GONE);
            }

            //  keep only last 10 searches
            if(i<searchHistory.size()-10){
                searchHistory.remove(i);
            }
            else if (searchHistory.get(i).getFrom() != null && searchHistory.get(i).getTo()  != null){
                //  infiltrate from row xml resource file
                View child = getLayoutInflater().inflate(R.layout.item_history_line, null);

                //  update UI texts and append!
                ((TextView) child.findViewById(R.id.fromTextView)).setText(searchHistory.get(i).getFrom().getResult().getName());
                ((TextView) child.findViewById(R.id.toTextView)).setText(searchHistory.get(i).getTo().getResult().getName());
                historyDatabaseContainer.addView(child);
            }

        }

    }

}
