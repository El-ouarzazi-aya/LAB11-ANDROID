package com.example.localisationsmartphone;
import android.provider.Settings;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.localisationsmartphone.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://192.168.1.107/localisation/";
    private static final int PERMISSION_CODE = 10;
    private static final long MIN_TIME_MS = 60000;
    private static final float MIN_DIST_M = 150f;

    private ActivityMainBinding binding;
    private LocationManager locationManager;
    private ApiService apiService;
    private boolean tracking = false;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            displayLocation(location);
            sendToServer(location);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            binding.tvStatus.setText("GPS actif");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            binding.tvStatus.setText("GPS désactivé");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        apiService = buildApiService();

        binding.btnToggle.setOnClickListener(v -> {
            if (tracking) {
                stopTracking();
            } else {
                requestPermissionsIfNeeded();
            }
        });
    }

    private void requestPermissionsIfNeeded() {
        String[] perms = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };

        boolean needsRequest = false;
        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                needsRequest = true;
                break;
            }
        }

        if (needsRequest) {
            ActivityCompat.requestPermissions(this, perms, PERMISSION_CODE);
        } else {
            startTracking();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startTracking();
            } else {
                binding.tvServerResponse.setText(getString(R.string.permission_denied));
                binding.tvServerResponse.setTextColor(
                        getResources().getColor(R.color.accent_red, getTheme()));
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startTracking() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DIST_M,
                locationListener
        );
        tracking = true;
        binding.tvStatus.setText("Actif — en écoute GPS");
        binding.tvStatus.setTextColor(getResources().getColor(R.color.accent_green, getTheme()));
        binding.btnToggle.setText(getString(R.string.btn_stop));
        binding.btnToggle.setBackgroundTintList(getColorStateList(R.color.accent_red));
        binding.btnToggle.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
    }

    private void stopTracking() {
        locationManager.removeUpdates(locationListener);
        tracking = false;
        binding.tvStatus.setText("Inactif");
        binding.tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        binding.btnToggle.setText(getString(R.string.btn_start));
        binding.btnToggle.setBackgroundTintList(getColorStateList(R.color.accent_green));
        binding.btnToggle.setTextColor(getResources().getColor(R.color.black, getTheme()));
    }

    private void displayLocation(Location location) {
        binding.tvLatitude.setText(
                String.format(Locale.US, "%.5f°", location.getLatitude()));
        binding.tvLongitude.setText(
                String.format(Locale.US, "%.5f°", location.getLongitude()));
        binding.tvAltitude.setText(
                String.format(Locale.US, "%.1f m", location.getAltitude()));
        binding.tvAccuracy.setText(
                String.format(Locale.US, "%.0f m", location.getAccuracy()));
    }

    private void sendToServer(Location location) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, String> fields = new HashMap<>();
        fields.put("latitude", String.valueOf(location.getLatitude()));
        fields.put("longitude", String.valueOf(location.getLongitude()));
        fields.put("date_position", timestamp);
        fields.put("imei", fetchDeviceId());

        binding.tvServerResponse.setText("Envoi en cours…");
        binding.tvServerResponse.setTextColor(
                getResources().getColor(R.color.text_secondary, getTheme()));

        apiService.sendPosition(fields).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,
                                   @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvServerResponse.setText(
                            timestamp + " — " + response.body().trim());
                    binding.tvServerResponse.setTextColor(
                            getResources().getColor(R.color.accent_green, getTheme()));
                } else {
                    binding.tvServerResponse.setText("Erreur HTTP " + response.code());
                    binding.tvServerResponse.setTextColor(
                            getResources().getColor(R.color.accent_red, getTheme()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                binding.tvServerResponse.setText("Réseau indisponible");
                binding.tvServerResponse.setTextColor(
                        getResources().getColor(R.color.accent_red, getTheme()));
            }
        });
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String fetchDeviceId() {
        return Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    private ApiService buildApiService() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        return new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tracking) {
            locationManager.removeUpdates(locationListener);
        }
    }
}