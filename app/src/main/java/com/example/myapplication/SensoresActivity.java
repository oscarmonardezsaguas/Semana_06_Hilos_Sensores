package com.example.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

public class SensoresActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor lightSensor;

    private TextView tvAccelX;
    private TextView tvAccelY;
    private TextView tvAccelZ;
    private TextView tvAccelStatus;

    private TextView tvLightValue;
    private TextView tvLightStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensores);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sensores_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Vistas
        tvAccelX = findViewById(R.id.tvAccelX);
        tvAccelY = findViewById(R.id.tvAccelY);
        tvAccelZ = findViewById(R.id.tvAccelZ);
        tvAccelStatus = findViewById(R.id.tvAccelStatus);

        tvLightValue = findViewById(R.id.tvLightValue);
        tvLightStatus = findViewById(R.id.tvLightStatus);

        TextView tvSensorsCount = findViewById(R.id.tvSensorsCount);
        Button btnBackSensores = findViewById(R.id.btnBackSensores);

        btnBackSensores.setOnClickListener(v -> finish());

        // Inicializar SensorManager y Sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            // Obtener lista completa de sensores del dispositivo
            List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            tvSensorsCount.setText(String.format(Locale.getDefault(),
                    "Este dispositivo cuenta con %d sensores físicos/virtuales instalados.", allSensors.size()));
        } else {
            tvSensorsCount.setText(getString(R.string.sensor_manager_unavailable));
        }

        if (accelerometerSensor == null) {
            tvAccelStatus.setText(getString(R.string.accel_unavailable));
        }

        if (lightSensor == null) {
            tvLightStatus.setText(getString(R.string.light_unavailable));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar listeners de sensores cuando la actividad está visible
        if (sensorManager != null) {
            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar listeners para ahorrar batería cuando la actividad pasa a segundo plano
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                tvAccelX.setText(String.format(Locale.getDefault(), "X: %.2f", x));
                tvAccelY.setText(String.format(Locale.getDefault(), "Y: %.2f", y));
                tvAccelZ.setText(String.format(Locale.getDefault(), "Z: %.2f", z));

                // Evaluar orientación básica del dispositivo
                if (Math.abs(x) < 2.0 && Math.abs(y) < 2.0 && z > 8.0) {
                    tvAccelStatus.setText("Estado: Posición Plana / Horizontal 📱");
                } else if (Math.abs(x) > 5.0) {
                    tvAccelStatus.setText("Estado: Inclinado Horizontalmente (Lado) 🔄");
                } else if (Math.abs(y) > 5.0) {
                    tvAccelStatus.setText("Estado: Inclinado Verticalmente (Pie) 📱");
                } else {
                    tvAccelStatus.setText("Estado: Movimiento / Inclinación detectada ⚡");
                }
                break;

            case Sensor.TYPE_LIGHT:
                float lux = event.values[0];
                tvLightValue.setText(String.format(Locale.getDefault(), "Luz: %.1f lux", lux));

                // Clasificar nivel de iluminación ambiental
                if (lux < 10) {
                    tvLightStatus.setText("Nivel: 🌙 Muy Oscuro");
                } else if (lux < 100) {
                    tvLightStatus.setText("Nivel: 💡 Luz Tenue / Interior");
                } else if (lux < 1000) {
                    tvLightStatus.setText("Nivel: ☀️ Luz Normal / Oficina");
                } else {
                    tvLightStatus.setText("Nivel: 🌞 Mucha Luz / Exterior");
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Método de la interfaz SensorEventListener para cambios de precisión
    }
}