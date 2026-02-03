package com.example.fitlife.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class for managing exercise images
 * Handles saving and loading images from internal storage
 */
public class ImageHelper {
    private static final String IMAGE_DIR = "exercise_images";

    /**
     * Saves bitmap image to internal storage and returns the file path
     */
    public static String saveImage(Context context, Bitmap bitmap, String exerciseName) {
        File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        String fileName = "exercise_" + exerciseName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imageDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads bitmap from file path
     */
    public static Bitmap loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes image file from storage
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        try {
            File imageFile = new File(imagePath);
            return imageFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
