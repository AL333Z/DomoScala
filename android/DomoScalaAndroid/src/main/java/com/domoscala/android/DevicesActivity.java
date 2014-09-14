package com.domoscala.android;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import com.domoscala.android.messages.*;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class DevicesActivity extends ExpandableListActivity {


    private DomoscalaWebService webService = null;
    private String buildingName = null;
    private List<DevicesListGroup> devicesListGroups = new ArrayList<>();
    private DevicesListAdapter listAdapter = null;


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
                    for (Room room : building.rooms) {
                        DevicesListGroup group = new DevicesListGroup(room.id);
                        devicesListGroups.add(group);
                        for (Device device : room.devices) {
                            DevicesListGroup.DevicesListItem item = group.new DevicesListItem(device.id, device.devType);
                            group.deviceItems.add(item);
                        }
                    }

                    listAdapter = new DevicesListAdapter(devicesListGroups, DevicesActivity.this);
                    setListAdapter(listAdapter); // before doing this the ListActivity shows a loading indicator

                    // Connect the WebSocket
                    try {
                        URI wsUrl = new URI("ws://" +
                                getIntent().getStringExtra(ConnectActivity.HOSTNAME) +
                                ":" +
                                getIntent().getStringExtra(ConnectActivity.PORT) +
                                "/push");
                        DevicesEventsWebSocket ws = new DevicesEventsWebSocket(wsUrl);
                        ws.connect();
                    } catch (Exception e){
                        e.printStackTrace();
                        finish();
                    }
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



    private class DevicesEventsWebSocket extends WebSocketClient {

        public DevicesEventsWebSocket(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DevicesActivity.this, "WebSocket connected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onMessage(String msg) {
            try {
                // Deserialize JSON message
                Gson gson = new Gson();
                final WebSocketDeviceEvent event = gson.fromJson(msg, WebSocketDeviceEvent.class);

                // Update the UI with the new device value
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(DevicesListGroup group : devicesListGroups){
                            if(group.room.equals(event.roomId)){
                                for(DevicesListGroup.DevicesListItem item : group.deviceItems){
                                    if(item.deviceName.equals(event.deviceId)){
                                        item.currentValue = event.status.um + ": " + event.status.value;
                                        listAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                }
                            }
                        }
                        Toast.makeText(DevicesActivity.this, "Received event of an unexistent device...", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DevicesActivity.this, "WebSocket message handling error!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DevicesActivity.this, "WebSocket disconnected!", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DevicesActivity.this, "WebSocket error!!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public void setLampStatus(final String room, final String device, final double value){
        SetDeviceStatusRequest req = new SetDeviceStatusRequest();
        req.um = "doubleValue";
        req.value = value;
        webService.setDeviceStatus(buildingName, room, device, req, new Callback<SetDeviceStatusResponse>() {
            @Override
            public void success(SetDeviceStatusResponse setDeviceStatusResponse, Response response) {
            }
            @Override
            public void failure(RetrofitError retrofitError) {
                retrofitError.printStackTrace();
                Toast.makeText(DevicesActivity.this, "Setting lamp value failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
