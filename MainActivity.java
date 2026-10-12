package com.example.flashlight;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isOn = false;
    private Button toggleButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        buildInterface();

        try {
            if (cameraManager == null) {
                showUnavailable();
                return;
            }
            for (String id : cameraManager.getCameraIdList()) {
                Boolean hasFlash = cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(hasFlash)) {
                    cameraId = id;
                    break;
                }
            }
            if (cameraId == null) {
                showUnavailable();
            }
        } catch (CameraAccessException | SecurityException e) {
            showUnavailable();
        }

        updateInterface();
    }

    private void buildInterface() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView title = new TextView(this);
        title.setText("چراغ‌قوه");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        layout.addView(title, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        statusText = new TextView(this);
        statusText.setTextSize(20);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 32, 0, 24);
        layout.addView(statusText, statusParams);

        toggleButton = new Button(this);
        toggleButton.setTextSize(22);
        toggleButton.setMinHeight(64);
        layout.addView(toggleButton, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        toggleButton.setOnClickListener(v -> toggleFlashlight());
        setContentView(layout);
    }

    private void toggleFlashlight() {
        if (cameraId == null) {
            showUnavailable();
            return;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }

        try {
            isOn = !isOn;
            cameraManager.setTorchMode(cameraId, isOn);
            updateInterface();
        } catch (CameraAccessException | SecurityException | IllegalArgumentException e) {
            isOn = false;
            updateInterface();
            Toast.makeText(this, "چراغ‌قوه قابل دسترسی نیست؛ برنامه دیگری ممکن است از دوربین استفاده کند.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateInterface() {
        if (toggleButton == null || statusText == null) return;
        statusText.setText(isOn ? "وضعیت: روشن" : "وضعیت: خاموش");
        toggleButton.setText(isOn ? "خاموش کردن چراغ" : "روشن کردن چراغ");
    }

    private void showUnavailable() {
        if (statusText != null) statusText.setText("این گوشی فلاش قابل کنترل ندارد.");
        if (toggleButton != null) toggleButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleFlashlight();
            } else {
                Toast.makeText(this, "برای کنترل فلاش، اجازه دوربین لازم است.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // چراغ عمداً با خروج از برنامه خاموش نمی‌شود؛ وضعیت دکمه در اجرای بعدی ممکن است با وضعیت واقعی فلاش متفاوت باشد.
    }
}
