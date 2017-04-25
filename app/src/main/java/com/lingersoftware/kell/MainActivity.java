package com.lingersoftware.kell;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_CAMERA = 0;
    private ImageButton btnSwitch;
    private Camera camera;
    private boolean isFlashOn;
    private Camera.Parameters params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void initControls() {
        boolean isCameraFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
        if (!isCameraFlash) {
            showCameraAlert();
        }
        btnSwitch.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                 toggleFlashabove6();
                                             } else if (isFlashOn && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                                 getCamera();
                                                 turnoffFlashBelow5();
                                             } else if (!isFlashOn && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                                 getCamera();
                                                 turnonFlashBelow5();
                                             }

                                         }
                                     }

        );
    }

    private void turnoffFlashBelow5() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            btnSwitch.setImageResource(R.drawable.btn_switch_off);
            isFlashOn = false;
        }
    }

    private void turnonFlashBelow5() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            btnSwitch.setImageResource(R.drawable.btn_switch_on);
            camera.startPreview();
            isFlashOn = true;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void toggleFlashabove6() {
        final CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = camManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (!isFlashOn) {
            try {
                camManager.setTorchMode(cameraId, true);   //Turn On
                isFlashOn = true;
                btnSwitch.setImageResource(R.drawable.btn_switch_on);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                camManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                btnSwitch.setImageResource(R.drawable.btn_switch_off);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                }
        }
    }


    private void showCameraAlert() {
        AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                .create();
        alert.setTitle("Error (No camera available)");
        alert.setMessage("Sorry, your device doesn't support flash light!");
        alert.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // closing the application
                finish();
            }
        });
        alert.show();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkCameraPermission();
    }

    void checkCameraPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            requestCameraPermission();
        } else {
            initControls();
        }
    }

    private void requestCameraPermission() {
        // Camera permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initControls();
            } else {
                //Permission not granted
                Toast.makeText(getApplicationContext(), "You need to grant camera permission to use camera's flashlight", Toast.LENGTH_LONG).show();
            }

        }
    }


}
