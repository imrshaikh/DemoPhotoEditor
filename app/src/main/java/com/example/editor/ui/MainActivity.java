package com.example.editor.ui;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.editor.R;
import com.example.editor.databinding.ActivityMainBinding;

import dagger.hilt.android.AndroidEntryPoint;

import static com.example.editor.ImgUtil.getOutputFile;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult> {

    final int REQ_CODE_READ_WRITE = 334;
    ActivityMainBinding binding;
    Uri currentOutputFile;
    MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        ActivityCompat.requestPermissions(this, permissions, REQ_CODE_READ_WRITE);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ActivityResultLauncher<Intent> resultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        binding.buttonCamera.setOnClickListener(view -> {
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

            currentOutputFile = Uri.fromFile(getOutputFile());
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentOutputFile);
            resultLauncher.launch(captureIntent);
//            Camera c = openFrontFacingCameraGingerbread();


        });

        binding.buttonGallery.setOnClickListener(view -> {
            final Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            resultLauncher.launch(galleryIntent);
        });

        model = new ViewModelProvider(this).get(MainActivityViewModel.class);
        model.lastProcessedImageUri.observe(this, this::setImageUri);

    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
//                    Log.e(TAG, "Camera failed to open: " + e.toString());
                }
            }
        }

        return cam;
    }

    private void setImageUri(@Nullable Uri uri) {
        if (uri != null) {
            binding.imagePreview.setImageURI(uri);
        } else {
            binding.imagePreview.setImageResource(R.drawable.ic_default_image);
        }
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data == null)
                launchEditor(currentOutputFile);
            else launchEditor(data.getData());

        }
    }

    private void launchEditor(Uri uri) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }


}