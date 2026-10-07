package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class ThreadsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvStatus;
    private Button btnStartThread;
    private Button btnStopThread;
    private Button btnTestUi;

    private Thread backgroundThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isRunning = false;
    private int uiClicksCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_threads);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.threads_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Vistas
        progressBar = findViewById(R.id.progressBar);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvStatus = findViewById(R.id.tvStatus);
        btnStartThread = findViewById(R.id.btnStartThread);
        btnStopThread = findViewById(R.id.btnStopThread);
        btnTestUi = findViewById(R.id.btnTestUi);
        Button btnBackThreads = findViewById(R.id.btnBackThreads);

        // Listeners
        btnStartThread.setOnClickListener(v -> startBackgroundTask());
        btnStopThread.setOnClickListener(v -> stopBackgroundTask());

        btnTestUi.setOnClickListener(v -> {
            uiClicksCount++;
            btnTestUi.setText(String.format(Locale.getDefault(), getString(R.string.ui_clicks_format), uiClicksCount));
        });

        btnBackThreads.setOnClickListener(v -> finish());
    }

    private void startBackgroundTask() {
        if (isRunning) return;

        isRunning = true;
        progressBar.setProgress(0);
        tvProgressPercent.setText(getString(R.string.progress_initial));
        tvStatus.setText(getString(R.string.status_thread_running));

        btnStartThread.setEnabled(false);
        btnStopThread.setEnabled(true);

        backgroundThread = new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                if (!isRunning) break;

                try {
                    // Simular tarea pesada en segundo plano
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                final int currentProgress = i;

                // Enviar la actualización de UI al Hilo Principal (Main Thread)
                mainHandler.post(() -> {
                    if (!isDestroyed() && !isFinishing()) {
                        progressBar.setProgress(currentProgress);
                        tvProgressPercent.setText(String.format(Locale.getDefault(), "Progreso: %d%%", currentProgress));

                        if (currentProgress == 100) {
                            isRunning = false;
                            tvStatus.setText(getString(R.string.status_thread_completed));
                            btnStartThread.setEnabled(true);
                            btnStopThread.setEnabled(false);
                        }
                    }
                });
            }
        });

        backgroundThread.start();
    }

    private void stopBackgroundTask() {
        if (isRunning) {
            isRunning = false;
            if (backgroundThread != null) {
                backgroundThread.interrupt();
            }
            tvStatus.setText(getString(R.string.status_thread_stopped));
            btnStartThread.setEnabled(true);
            btnStopThread.setEnabled(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopBackgroundTask();
    }
}