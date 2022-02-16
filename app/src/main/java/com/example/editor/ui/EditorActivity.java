package com.example.editor.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.editor.ImgUtil;
import com.example.editor.Prefs;
import com.example.editor.R;
import com.example.editor.databinding.ActivityEditorBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditorActivity extends AppCompatActivity {

    ActivityEditorBinding binding;
    Uri imageUri;
    Bitmap currentBitmap;

    @Inject
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditorBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setTitle(null);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageUri = getIntent().getData();
        assert imageUri != null;

        initialImageView();

    }

    private void initialImageView() {
        Glide.with(this).load(imageUri).into(binding.imagePreview);
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        currentBitmap = resource;
                        binding.imagePreview.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // currentBitmap might not be available immediately
        if (currentBitmap == null) {
            return false;
        }

        final int itemId = item.getItemId();
        if (itemId == R.id.mi_crop) {

            startSupportActionMode(new ActionCallback());

        } else if (itemId == R.id.mi_rotate) {

            currentBitmap = rotateBitmap90(currentBitmap);
            binding.imagePreview.setImageBitmap(currentBitmap);

        } else if (itemId == R.id.mi_save) {
            File outFile = ImgUtil.getOutputFile();
            try {
                FileOutputStream fos = new FileOutputStream(outFile);
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                // let gallery scan this image
                MediaScannerConnection.scanFile(this, new String[]{outFile.getAbsolutePath()},
                        null, (s, uri) -> {
                            // ignored
                        });


                prefs.prefs.edit()
                        .putString(Prefs.KEY_LAST_IMAGE_URI, Uri.fromFile(outFile).toString())
                        .apply();

                finish();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.msg_not_capture, Toast.LENGTH_SHORT).show();
            }


        } else if (itemId == R.id.mi_undo) {
            initialImageView();
        }

        return super.onOptionsItemSelected(item);
    }

    private Bitmap rotateBitmap90(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * ActionMode Callback for Cropping
     */
    private class ActionCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_menu_editor, menu);
            binding.cropImagePreview.setImageBitmap(currentBitmap);
            binding.imagePreview.setVisibility(View.GONE);
            binding.cropImagePreview.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.mi_done) {
                currentBitmap = binding.cropImagePreview.getCroppedImage();
                binding.imagePreview.setImageBitmap(currentBitmap);
            }
            // mi_close - default behavior

            binding.cropImagePreview.setVisibility(View.GONE);
            binding.imagePreview.setVisibility(View.VISIBLE);
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

}