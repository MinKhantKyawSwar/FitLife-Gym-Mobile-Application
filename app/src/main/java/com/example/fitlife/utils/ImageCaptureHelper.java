package com.example.fitlife.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCaptureHelper {
    
    public interface ImageCaptureCallback {
        void onImageCaptured(String imagePath);
        void onImageCaptureFailed(String error);
    }
    
    // Request codes
    public static final int REQUEST_CAMERA = 2001;
    public static final int REQUEST_GALLERY = 2002;
    public static final int REQUEST_CAMERA_PERMISSION = 2003;
    
    private Context context;
    private ImageCaptureCallback callback;
    private File currentPhotoFile;
    
    public ImageCaptureHelper(Context context) {
        this.context = context;
    }
    
    public void setCallback(ImageCaptureCallback callback) {
        this.callback = callback;
    }
    
    // Show image source selection dialog
    public void showImageSourceDialog(Fragment fragment) {
        String[] options = {"Take Photo", "Choose from Gallery"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Add Exercise Image")
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0:
                           captureImageFromCamera(fragment);
                           break;
                       case 1:
                           selectImageFromGallery(fragment);
                           break;
                   }
               })
               .show();
    }
    
    // Capture image from camera
    public void captureImageFromCamera(Fragment fragment) {
        if (!PermissionManager.hasCameraPermissions(context)) {
            PermissionManager.requestCameraPermissions(fragment);
            return;
        }
        
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                currentPhotoFile = createImageFile();
                
                if (currentPhotoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.fitlife.fileprovider",
                        currentPhotoFile);
                    
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    fragment.startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }
            } catch (IOException ex) {
                if (callback != null) {
                    callback.onImageCaptureFailed("Error creating image file: " + ex.getMessage());
                }
            }
        } else {
            if (callback != null) {
                callback.onImageCaptureFailed("No camera app available");
            }
        }
    }
    
    // Select image from gallery
    public void selectImageFromGallery(Fragment fragment) {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK);
        pickPhotoIntent.setType("image/*");
        
        if (pickPhotoIntent.resolveActivity(context.getPackageManager()) != null) {
            fragment.startActivityForResult(pickPhotoIntent, REQUEST_GALLERY);
        } else {
            if (callback != null) {
                callback.onImageCaptureFailed("No gallery app available");
            }
        }
    }
    
    // Handle activity result
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (callback != null) {
                callback.onImageCaptureFailed("Image capture cancelled");
            }
            return;
        }
        
        switch (requestCode) {
            case REQUEST_CAMERA:
                handleCameraResult();
                break;
            case REQUEST_GALLERY:
                handleGalleryResult(data);
                break;
        }
    }
    
    // Handle camera capture result
    private void handleCameraResult() {
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            try {
                // Compress and optimize the image
                String optimizedPath = compressAndSaveImage(currentPhotoFile.getAbsolutePath());
                
                // Delete the original file if compression created a new one
                if (!optimizedPath.equals(currentPhotoFile.getAbsolutePath())) {
                    currentPhotoFile.delete();
                }
                
                if (callback != null) {
                    callback.onImageCaptured(optimizedPath);
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onImageCaptureFailed("Error processing image: " + e.getMessage());
                }
            }
        } else {
            if (callback != null) {
                callback.onImageCaptureFailed("Image file not found");
            }
        }
    }
    
    // Handle gallery selection result
    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            
            try {
                // Copy and compress the selected image
                String imagePath = copyAndCompressImage(selectedImageUri);
                
                if (callback != null) {
                    callback.onImageCaptured(imagePath);
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onImageCaptureFailed("Error processing selected image: " + e.getMessage());
                }
            }
        } else {
            if (callback != null) {
                callback.onImageCaptureFailed("No image selected");
            }
        }
    }
    
    // Create image file with unique name
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FITLIFE_" + timeStamp + "_";
        File storageDir = getExerciseImagesDirectory();
        
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
    }
    
    // Generate filename for exercise image
    public static String generateExerciseImageFileName(int exerciseId) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "exercise_" + exerciseId + "_" + timeStamp + ".jpg";
    }
    
    // Get exercise images directory
    public File getExerciseImagesDirectory() {
        File imagesDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ExerciseImages");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        return imagesDir;
    }
    
    // Compress and save image
    private String compressAndSaveImage(String originalPath) throws IOException {
        Bitmap originalBitmap = BitmapFactory.decodeFile(originalPath);
        
        if (originalBitmap == null) {
            throw new IOException("Unable to decode image");
        }
        
        // Correct image orientation
        Bitmap correctedBitmap = correctImageOrientation(originalPath, originalBitmap);
        
        // Resize if too large (max 1024x1024)
        Bitmap resizedBitmap = resizeImageIfNeeded(correctedBitmap, 1024, 1024);
        
        // Save compressed image
        String compressedPath = originalPath.replace(".jpg", "_compressed.jpg");
        File compressedFile = new File(compressedPath);
        
        FileOutputStream out = new FileOutputStream(compressedFile);
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.flush();
        out.close();
        
        // Clean up bitmaps
        if (correctedBitmap != originalBitmap) {
            originalBitmap.recycle();
        }
        if (resizedBitmap != correctedBitmap) {
            correctedBitmap.recycle();
        }
        resizedBitmap.recycle();
        
        return compressedFile.getAbsolutePath();
    }
    
    // Copy and compress image from URI (for gallery selection)
    private String copyAndCompressImage(Uri imageUri) throws IOException {
        // Create destination file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "FITLIFE_gallery_" + timeStamp + ".jpg";
        File destFile = new File(getExerciseImagesDirectory(), fileName);
        
        // Decode and compress the image
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        
        if (bitmap == null) {
            throw new IOException("Unable to decode selected image");
        }
        
        // Resize if needed
        Bitmap resizedBitmap = resizeImageIfNeeded(bitmap, 1024, 1024);
        
        // Save compressed image
        FileOutputStream out = new FileOutputStream(destFile);
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.flush();
        out.close();
        
        // Clean up
        if (resizedBitmap != bitmap) {
            bitmap.recycle();
        }
        resizedBitmap.recycle();
        
        return destFile.getAbsolutePath();
    }
    
    // Correct image orientation based on EXIF data
    private Bitmap correctImageOrientation(String imagePath, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap; // No rotation needed
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            return bitmap; // Return original if EXIF reading fails
        }
    }
    
    // Resize image if it exceeds maximum dimensions
    private Bitmap resizeImageIfNeeded(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap; // No resizing needed
        }
        
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    // Delete image file
    public static boolean deleteImageFile(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            return imageFile.exists() && imageFile.delete();
        }
        return false;
    }
    
    // Check if image file exists
    public static boolean imageFileExists(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            return imageFile.exists();
        }
        return false;
    }
    
    // Get image file size in bytes
    public static long getImageFileSize(String imagePath) {
        if (imageFileExists(imagePath)) {
            return new File(imagePath).length();
        }
        return 0;
    }
}