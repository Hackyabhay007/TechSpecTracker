package com.hackydesk.techspecstracker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.hackydesk.techspecstracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private ActivityMainBinding binding;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor barometerSensor;
    private Sensor accelerometerSensor;
    private Sensor rotationVectorSensor;
    private Sensor proximitySensor;
    private Sensor ambientLightSensor;

    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        String fcamerapx = "";


        DeviceInfo deviceInfo = new DeviceInfo(getApplicationContext());
        ActivityCompat.requestPermissions((Activity) this,
                new String[]{android.Manifest.permission.READ_PHONE_STATE},
                4);

        try {
            fcamerapx = deviceInfo.getCamerasMegaPixel();
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }


        binding.modelname.setText(deviceInfo.getModelName());
        binding.modelno.setText(deviceInfo.getModelNumber());
        binding.MANUFACTURERname.setText(deviceInfo.getManufacturer());
        binding.ram.setText(String.valueOf((deviceInfo.getTotalRam())));
        binding.storage.setText(String.valueOf(deviceInfo.getTotalStorage()));
        binding.gpuinfo.setText(deviceInfo.getGpuInfo());
        binding.cpuinfo.setText(deviceInfo.getCpuInfo());
        binding.batterylevel.setText(String.valueOf(deviceInfo.getBatteryLevel()));
        binding.androidversion.setText(deviceInfo.getAndroidVersion());
        binding.imei.setText(deviceInfo.getImei());
        binding.frontmp.setText(fcamerapx);
        binding.brand.setText(deviceInfo.getBrandName());
        binding.availstorage.setText(String.valueOf(deviceInfo.getAvailableStorage()));
//        binding.frontcammp.setText(String.valueOf(deviceInfo.getFrontCameraMegapixels()));
//        binding.backcammp.setText(String.valueOf(deviceInfo.getBackCameraMegapixels()));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {

             double lat =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
             double lng =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
            binding.gps.setText( "GPS - " +(lat) + " , " + (lng));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        switch (sensorType) {

            case Sensor.TYPE_GYROSCOPE:
                binding.gyroscopeReading.setText("Gyroscope: " + values[0] + ", " + values[1] + ", " + values[2]);
                break;
            case Sensor.TYPE_PRESSURE:
                binding.barometre.setText("Barometer: " + values[0] + " hPa");
                break;
            case Sensor.TYPE_ACCELEROMETER:
                binding.accelerometerReading.setText("Accelerometer: " + values[0] + ", " + values[1] + ", " + values[2]);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                binding.rotationVectorReading.setText("Rotation Vector: " + values[0] + ", " + values[1] + ", " + values[2] + ", " + values[3]);
                break;
            case Sensor.TYPE_PROXIMITY:
                binding.proximityReading.setText("Proximity: " + values[0]);
                break;
            case Sensor.TYPE_LIGHT:
                binding.ambientLightReading.setText("Ambient Light: " + values[0] + " lux");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, ambientLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        binding.gps.setText( "GPS - " +(latitude) + " , " + (longitude));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}