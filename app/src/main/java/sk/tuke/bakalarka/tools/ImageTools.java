package sk.tuke.bakalarka.tools;

import static sk.tuke.bakalarka.tools.ResourcesTools.getScreenWidth;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.TypedValue;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageTools {

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri == null) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    public static MultipartBody.Part getImagePart(Context context, Uri imageUri) {
        if(imageUri == null) {
            return null;
        }
        //create a File object from the imageUri
        File imageFile = new File(Objects.requireNonNull(getRealPathFromURI(context, imageUri)));

        //create a request body using the image file
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        //create a MultipartBody.Part from the request body
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestBody);
        return imagePart;
    }


    public static Bitmap resizeBitmap(Context context, Bitmap originalBitmap, int targetWidthDp, int targetHeightDp) {
        if(originalBitmap == null) {
            return null;
        }
        //convert dp to pixels
        int targetWidthPx = dpToPx(context, targetWidthDp);
        int targetHeightPx = dpToPx(context, targetHeightDp);
        //resize original bitmap
        return Bitmap.createScaledBitmap(originalBitmap, targetWidthPx, targetHeightPx, true);
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encodedBitmap;
    }

    public static Bitmap base64ToBitmap(String base64) {
        if(base64 == null) {
            return null;
        }
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedBitmap;
    }


    public static int getResizedHeightToScreenWidth(int originalWidth, int originalHeight, int screenWidth) {
        return originalHeight * screenWidth/originalWidth;
    }
}

