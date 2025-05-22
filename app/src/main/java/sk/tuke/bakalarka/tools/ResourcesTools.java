package sk.tuke.bakalarka.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.widget.Spinner;

import java.util.Map;

import sk.tuke.bakalarka.R;

public class ResourcesTools {
    public static String getMaterial(Map<String, Integer> composition, Context context) {
        boolean natural = false;
        boolean synthetic = false;

        String[] naturalFabrics = context.getResources().getStringArray(R.array.natural_fabrics_array);
        String[] syntheticFabrics = context.getResources().getStringArray(R.array.synthetic_fabrics_array);

        //iterate over fabric types
        for (Map.Entry<String, Integer> entry : composition.entrySet()) {
            //check if fabric is natural
            for(String fabricName : naturalFabrics) {
                String fabric = entry.getKey();
                if(fabric.equals(fabricName.toLowerCase())) {
                    natural = true;
                    break;
                }
            }
            //check if fabric is synthetic
            for(String fabricName : syntheticFabrics) {
                String fabric = entry.getKey();
                if(fabric.equals(fabricName.toLowerCase())) {
                    synthetic = true;
                    break;
                }
            }
        }
        if(natural && !synthetic) {
            return "natural";
        }
        if(synthetic && !natural) {
            return "synthetic";
        }
        return "blend";
    }

    public static String getResourcesValueFromPosition(String[] arr, int position) {
        return arr[position];
    }



    public static void setResourcesArrayPosition(String[] arr, Spinner spinner, String value) {
        int position = getResourcesPositionFromValue(arr,value);
        if (position != -1) {
            spinner.setSelection(position);
        }
    }

    public static int getResourcesPositionFromValue(String[] arr, String value) {
        int position = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(value)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static String getUserColorPalette(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("colorPalette")) {
            String colorPalette = sharedPreferences.getString("colorPalette", null);
            colorPalette = ParseTools.parseColorSeason(colorPalette);
            return colorPalette;
        }
        return null;
    }


    public static String getUserStylePref(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getString(key, null);
        }
        return null;
    }

    public static boolean getAlertDialogPref(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("alertDialog")) {
            boolean alertDialog = sharedPreferences.getBoolean("alertDialog", true);

            return alertDialog;
        }
        return true;
    }

    public static void setAlertDialogPref(Context context, boolean alertDialog){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("alertDialog", alertDialog);
        editor.apply();
    }

    public static int calculateNumberOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int numberOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int
        return numberOfColumns;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) screenWidthDp;
    }
}
