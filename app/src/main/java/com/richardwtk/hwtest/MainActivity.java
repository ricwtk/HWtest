package com.richardwtk.hwtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private RecyclerView hwRecyclerView;
    private RecyclerView.Adapter hwAdapter;
    private RecyclerView.LayoutManager hwLayoutManager;
    private List<HwItem> hwList = new ArrayList<HwItem>();
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 11;
    private Button getLocationButton;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationButton =  findViewById(R.id.get_location_button);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        findViewById(R.id.get_sensor_list_button)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSensorList();
                }
            });

        hwRecyclerView = (RecyclerView) findViewById(R.id.hwlist);
        hwRecyclerView.setHasFixedSize(true);

        hwLayoutManager = new LinearLayoutManager(this);
        hwRecyclerView.setLayoutManager(hwLayoutManager);

        hwAdapter = new HwAdapter(hwList);
        hwRecyclerView.setAdapter(hwAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
        } else {
            getLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLastLocation();
                }
            });
        }

    }

    private class HwItem {
        public String name;
        public String value;

        private HwItem(String thisName, String thisValue) {
            name = thisName;
            value = thisValue;
        }
    }

    private class HwAdapter extends RecyclerView.Adapter<HwViewHolder> {
        private List<HwItem> items;
        private HwAdapter(List<HwItem> thisItems) {
            items = thisItems;
        }

        @Override
        public HwViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            return new HwViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(HwViewHolder holder, int position) {
            holder.setValue(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class HwViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView valueText;

        private HwViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.hw_list, parent, false));
            nameText = itemView.findViewById(R.id.name_text);
            valueText = itemView.findViewById(R.id.value_text);
        }

        public void setValue(HwItem item) {
            nameText.setText(item.name);
            valueText.setText(item.value);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getLastLocation();
                        }
                    });
                }
            }
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(this.toString(), location.toString());
                            hwList.add(new HwItem("last known location", String.format("(%f,%f)", location.getLatitude(), location.getLongitude())));
                            hwAdapter.notifyDataSetChanged();
                        }
                    }
                });
        }
    }

    private void getSensorList() {
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(this.getLocalClassName(), deviceSensors.toString());
        for (Sensor sens : deviceSensors) {
            hwList.add(new HwItem(sens.getName(), sens.getVendor()));
        }
        hwAdapter.notifyDataSetChanged();
    }
}
