package com.domoscala.android;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.domoscala.android.messages.Building;
import com.domoscala.android.messages.BuildingsResponse;
import com.domoscala.android.messages.Device;
import com.domoscala.android.messages.Room;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;


public class DevicesActivity extends ExpandableListActivity {


    private DomoscalaWebService webService = null;
    private String buildingName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String httpUrl = "http://" +
                    getIntent().getStringExtra(ConnectActivity.HOSTNAME) +
                    ":" +
                    getIntent().getStringExtra(ConnectActivity.PORT);

            // Initialize Retrofit
            RestAdapter restAd = new RestAdapter.Builder()
                    .setEndpoint(httpUrl)
                    .build();
            webService = restAd.create(DomoscalaWebService.class);

            // Start getting Buildings
            webService.getBuildings(new Callback<BuildingsResponse>() {

                @Override
                public void success(BuildingsResponse buildingsResponse, Response response) {

                    if (buildingsResponse.buildings.length == 0) {
                        finish();
                        return;
                    }
                    Building building = buildingsResponse.buildings[0];
                    setTitle(building.id); // put the building name as the title in the action bar
                    buildingName = building.id;
                    ArrayList<DevicesListGroup> devicesListGroups = new ArrayList<>();
                    for (Room room : building.rooms) {
                        DevicesListGroup group = new DevicesListGroup(room.id);
                        devicesListGroups.add(group);
                        for (Device device : room.devices) {
                            DevicesListGroup.DevicesListItem item = group.new DevicesListItem(device.id, device.devType);
                            group.deviceItems.add(item);
                        }
                    }

                    DevicesListAdapter listAdapter = new DevicesListAdapter(devicesListGroups, DevicesActivity.this);
                    setListAdapter(listAdapter); // before doing this the ListActivity shows a loading indicator
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                    new AlertDialog.Builder(DevicesActivity.this)
                            .setTitle("Error")
                            .setMessage(retrofitError.isNetworkError() ?
                                            "Network error" :
                                            "HTTP error " + retrofitError.getResponse().getStatus() + " while getting buildings"
                            )
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            });

        } catch(Exception e){
            // probably it's a wrong url or something... give an error and close the activity
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Invalid URL of the server")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
