package sk.tuke.bakalarka.tools;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.Map;

import sk.tuke.bakalarka.R;

public class CareDrawableFinder {
    private static final Map<String, String> stringNameMap;

    static {
        stringNameMap = new HashMap<>();
        stringNameMap.put("Wash regularly", "care_washing");
        stringNameMap.put("Wash gentle", "care_washing_gentle");
        stringNameMap.put("Wash very gentle", "care_washing_gentle_very");
        stringNameMap.put("Hand-wash", "care_washing_hand");
        stringNameMap.put("Do not wash", "care_washing_no");
        stringNameMap.put("Wash on high temperature", "care_washing_temp_high");
        stringNameMap.put("Wash on medium temperature", "care_washing_temp_medium");
        stringNameMap.put("Wash on low temperature", "care_washing_temp_low");
        stringNameMap.put("Wash on 60 degrees Celsius", "care_washing_60");
        stringNameMap.put("Wash on 40 degrees Celsius", "care_washing_40");
        stringNameMap.put("Wash on 30 degrees Celsius", "care_washing_30");
        stringNameMap.put("Tumble dry", "care_tumble_dry");
        stringNameMap.put("Tumble dry on high heat", "care_tumble_dry_high_heat");
        stringNameMap.put("Tumble dry on medium heat", "care_tumble_dry_medium_heat");
        stringNameMap.put("Tumble dry on low heat", "care_tumble_dry_low_heat");
        stringNameMap.put("Do not tumble dry", "care_tumble_dry_no");
        stringNameMap.put("Do not tumble dry on heat", "care_tumble_dry_no_heat");
        stringNameMap.put("Dry naturally", "care_drying_natural");
        stringNameMap.put("Dry by hanging while dripping wet", "care_drying_drip");
        stringNameMap.put("Dry by hanging while dripping wet in shade", "care_drying_drip_shade");
        stringNameMap.put("Dry by laying on a flat surface", "care_drying_flat");
        stringNameMap.put("Dry by laying on a flat surface in shade", "care_drying_flat_shade");
        stringNameMap.put("Dry by hanging on a washing line", "care_drying_line");
        stringNameMap.put("Dry by hanging on a washing line in shade", "care_drying_line_shade");
        stringNameMap.put("Dry in shade", "care_drying_shade");
        stringNameMap.put("Iron regularly", "care_ironing");
        stringNameMap.put("Do not iron", "care_ironing_no");
        stringNameMap.put("Iron on high temperature", "care_ironing_temp_high");
        stringNameMap.put("Iron on medium temperature", "care_ironing_temp_medium");
        stringNameMap.put("Iron on low temperature", "care_ironing_temp_low");
        stringNameMap.put("Iron with steam", "care_ironing_steam");
        stringNameMap.put("Do not iron with steam", "care_ironing_steam_no");
        stringNameMap.put("Regular bleach", "care_bleach");
        stringNameMap.put("Do not bleach", "care_bleach_no");
        stringNameMap.put("Non-clorine bleach", "care_bleach_non_clorine");
        stringNameMap.put("Dry clean", "care_dry_clean");
        stringNameMap.put("Do not dry clean", "care_dry_clean_no");
        stringNameMap.put("Wring when washing", "care_wring");
        stringNameMap.put("Do not wring when washing", "care_wring_no");
    }

    public static String findStringName(String key) {
        return stringNameMap.get(key);

    }
    public static int findDrawableId(Context context, String value) {
        String stringName = findStringName(value);
        if (stringName != null) {
            return context.getResources().getIdentifier(stringName, "drawable", context.getPackageName());
        }
        return -1;
    }

    public static Drawable getDrawableFromId(Context context, String value) {
        if(value != null) {
            return ResourcesCompat.getDrawable(context.getResources(),findDrawableId(context,value),null);
        }
        return null;
    }

}
