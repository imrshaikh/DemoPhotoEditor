package com.example.editor;

import android.os.Environment;

import java.io.File;

public class ImgUtil {

    public static File getOutputFile() {
        final File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + "Edited" + File.separator);
        root.mkdirs();
        final String fname = System.currentTimeMillis() + ".jpeg";
        final File sdImageMainDirectory = new File(root, fname);
        return sdImageMainDirectory;
    }

}
