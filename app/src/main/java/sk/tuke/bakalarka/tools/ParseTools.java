package sk.tuke.bakalarka.tools;

import static sk.tuke.bakalarka.tools.StatisticsCalculator.roundToTwoDecimal;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.swap.recycler_view.SwapModel;
import sk.tuke.bakalarka.entities.SwapItem;
import sk.tuke.bakalarka.recycler_view.PhotoItemModel;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.Outfit;

public class ParseTools {
    public static String parseDateToString(int y, int m, int d) {
        String year = String.valueOf(y);
        String month = String.valueOf(m);
        String day = String.valueOf(d);
        if(month.length() < 2) {
            month = "0"+month;
        }
        if(day.length() < 2) {
            day = "0"+day;
        }
        String dateString = year+"/"+month+"/"+day;
        return dateString;
    }
    public static LocalDate parseStringToLocalDate(String dateString) {
        if(dateString == null || dateString.equals("null") || dateString.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate localDate = LocalDate.parse(dateString,formatter);
        return localDate;
    }
    public static String parseLocalDateToString(LocalDate localDate) {
        if(localDate == null) {
            return null;
        }
        return parseDateToString(localDate.getYear(),localDate.getMonthValue(), localDate.getDayOfMonth());
    }

    public static Map<String, Integer> parseFabricString(String fabricString) {
        if(fabricString != null) {
            Map<String, Integer> fabricMap = new HashMap<>();

            //using regex to extract percentages and fabric names
            Pattern pattern = Pattern.compile("(\\d+)%\\s+(\\w+)");
            Matcher matcher = pattern.matcher(fabricString);

            //iterate through matches
            while (matcher.find()) {
                String fabricName = matcher.group(2).toLowerCase();
                int percentage = Integer.parseInt(matcher.group(1));
                fabricMap.put(fabricName, percentage);
            }

            return fabricMap;
        }
        return null;
    }

    public static List<PhotoItemModel> getClothingItemModels(List<ClothingItem> clothingItems) {
        List<PhotoItemModel>  clothingItemModels = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            PhotoItemModel clothingItemModel = new PhotoItemModel(clothingItem.getImageLink());
            clothingItemModels.add(clothingItemModel);
        }
        return clothingItemModels;
    }


    public static List<PhotoItemModel> getOutfitModels(List<Outfit> outfits) {
        List<PhotoItemModel>  clothingItemModels = new ArrayList<>();
        for(Outfit outfit : outfits) {
            PhotoItemModel clothingItemModel = new PhotoItemModel(outfit.getImageLink());
            clothingItemModels.add(clothingItemModel);
        }
        return clothingItemModels;
    }

    public static List<SwapModel> getSwapItemModels(List<ClothingItem> clothingItems) {
        List<SwapModel> swapModels = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            SwapItem swapItem = (SwapItem) clothingItem;
            String swapPrice = roundToTwoDecimal((double) swapItem.getSwapPrice());
            SwapModel swapModel = new SwapModel(swapItem.getImageLink(),swapItem.getCondition(),swapPrice);
            swapModels.add(swapModel);
        }
        return swapModels;
    }


    public static String parseToImageReference(String userId, String folder, String imageName) {
        return "images/" + userId + "/" + folder + "/" + imageName;
    }

    public static String parseColorSeason(String colorSeason) {
        if(colorSeason != null) {
            return colorSeason.toLowerCase().replace(" ", "_");
        }
        return null;
    }

    public static Outfit parseOutfit(QueryDocumentSnapshot document) {
        HashSet<String> clothingItems = new HashSet<String>((Collection<? extends String>) document.get("clothingItems"));
        String occasion = String.valueOf(document.get("occasion"));
        String season = String.valueOf(document.get("season"));
        String imageLink = String.valueOf(document.get("imageLink"));
        String id = String.valueOf(document.get("id"));
        List<String> datesWorn = (List<String>) document.get("datesWorn");
        Outfit outfit = new Outfit();
        outfit.setSeason(season);
        outfit.setOccasion(occasion);
        outfit.setImageLink(imageLink);
        outfit.setId(Integer.parseInt(id));
        outfit.setClothingItems(clothingItems);
        outfit.setDatesWorn(datesWorn);
        return outfit;
    }
    public static Outfit parseOutfit(DocumentSnapshot document) {
        HashSet<String> clothingItems = new HashSet<String>((Collection<? extends String>) document.get("clothingItems"));
        String occasion = String.valueOf(document.get("occasion"));
        String season = String.valueOf(document.get("season"));
        String imageLink = String.valueOf(document.get("imageLink"));
        String id = String.valueOf(document.get("id"));
        List<String> datesWorn = (List<String>) document.get("datesWorn");
        Outfit outfit = new Outfit();
        outfit.setSeason(season);
        outfit.setOccasion(occasion);
        outfit.setImageLink(imageLink);
        outfit.setId(Integer.parseInt(id));
        outfit.setClothingItems(clothingItems);
        outfit.setDatesWorn(datesWorn);
        return outfit;
    }

    public static ClothingItem parseClothingItem(QueryDocumentSnapshot document) {
        String dateOfPurchase = String.valueOf(document.get("date_of_purchase"));
        ClothingItem clothingItem = document.toObject(ClothingItem.class);
        clothingItem.setDateOfPurchase(parseStringToLocalDate(dateOfPurchase));
        return clothingItem;
    }
    public static HashMap<String,Object> parseClothingItemToHashMap(ClothingItem clothingItem) {
        HashMap<String, Object> clothingItemMap = new HashMap<>();

        clothingItemMap.put("id",clothingItem.getId());
        clothingItemMap.put("imageLink",clothingItem.getImageLink());
        clothingItemMap.put("type",clothingItem.getType());
        clothingItemMap.put("colorName",clothingItem.getColorName());
        clothingItemMap.put("colorCategory",clothingItem.getColorCategory());
        clothingItemMap.put("pattern",clothingItem.getPattern());
        clothingItemMap.put("care",clothingItem.getCare());
        clothingItemMap.put("composition",clothingItem.getComposition());
        clothingItemMap.put("material",clothingItem.getMaterial());
        clothingItemMap.put("selfMade",clothingItem.isSelfMade());
        clothingItemMap.put("date_of_purchase",parseLocalDateToString(clothingItem.getDateOfPurchase()));
        if(!clothingItem.isSelfMade()) {
            clothingItemMap.put("purchasePrice",clothingItem.getPurchasePrice());
        }
        clothingItemMap.put("inLaundry",false);
        //clothingItemMap.put("timesWorn",0);
        //clothingItemMap.put("wearPercentile",0);
        //clothingItemMap.put("timesWorn",clothingItem.getTimesWorn());
        //clothingItemMap.put("wearPercentile",clothingItem.getWearPercentile());


        return clothingItemMap;
    }
    public static HashMap<String,Object> parseSwapItemToHashMap(SwapItem swapItem) {
        HashMap<String, Object> swapItemMap = new HashMap<>();

        swapItemMap.put("id",swapItem.getId());
        swapItemMap.put("swapId",swapItem.getSwapId());
        swapItemMap.put("userId",swapItem.getUserId());
        swapItemMap.put("imageLink",swapItem.getImageLink());
        swapItemMap.put("condition", swapItem.getCondition());
        swapItemMap.put("details", swapItem.getDetails());
        swapItemMap.put("swapPrice",swapItem.getSwapPrice());
        swapItemMap.put("type",swapItem.getType());
        swapItemMap.put("colorName",swapItem.getColorName());
        swapItemMap.put("colorCategory",swapItem.getColorCategory());
        swapItemMap.put("pattern",swapItem.getPattern());
        swapItemMap.put("care",swapItem.getCare());
        swapItemMap.put("composition",swapItem.getComposition());
        swapItemMap.put("material",swapItem.getMaterial());
        swapItemMap.put("acceptedRequest","");

        return swapItemMap;
    }

    public static Map<String,Object> parseOutfitToHashMap(Outfit outfit) {
        Map<String, Object> outfitMap = new HashMap<>();

        outfitMap.put("id",outfit.getId());
        outfitMap.put("imageLink",outfit.getImageLink());
        outfitMap.put("occasion",outfit.getOccasion());
        outfitMap.put("season",outfit.getSeason());
        outfitMap.put("clothingItems",new ArrayList<>(outfit.getClothingItems()));
        return outfitMap;
    }

    public static ClothingItem parseClothingItem(DocumentSnapshot document) {
        String dateOfPurchase = String.valueOf(document.get("date_of_purchase"));
        ClothingItem clothingItem = document.toObject(ClothingItem.class);
        assert clothingItem != null;
        clothingItem.setDateOfPurchase(parseStringToLocalDate(dateOfPurchase));
        return clothingItem;
    }


    public static ClothingItem getClothingItem(Context context, View view, String imageUrl, String id, String colorName) {

        //getting values from views and radio buttons and putting them into ClothingItem class

        Spinner typeSpinner = (Spinner) view.findViewById(R.id.type_spinner);
        Spinner colorSpinner = (Spinner) view.findViewById(R.id.color_spinner);
        Spinner patternSpinner = (Spinner) view.findViewById(R.id.pattern_spinner);
        SwitchCompat selfMadeSwitch = view.findViewById(R.id.self_made_switch);
        EditText compositionEditText = view.findViewById(R.id.composition);
        EditText priceEditText = view.findViewById(R.id.edit_text_price);
        TextView dateTextView = view.findViewById(R.id.date);
        String dateString = String.valueOf(dateTextView.getText());
        LocalDate date = parseStringToLocalDate(dateString);

        String type = typeSpinner.getSelectedItem().toString().toLowerCase();
        String color = colorSpinner.getSelectedItem().toString().toLowerCase();
        String pattern = patternSpinner.getSelectedItem().toString().toLowerCase();
        String composition = compositionEditText.getText().toString();
        boolean selfMade = selfMadeSwitch.isChecked();

        double price = 0.0;
        if(!priceEditText.getText().toString().isEmpty()) {
            price = Double.parseDouble(String.valueOf(priceEditText.getText()));
        }

        //get care from radio buttons
        RadioGroup washingGroup = view.findViewById(R.id.washing_group);
        String wash = getOptionFromRadioGroup(view, washingGroup);

        RadioGroup tumbleDryingGroup = view.findViewById(R.id.tumble_dry_group);
        String tumbleDry = getOptionFromRadioGroup(view,tumbleDryingGroup);

        RadioGroup ironingGroup = view.findViewById(R.id.ironing_group);
        String iron = getOptionFromRadioGroup(view, ironingGroup);

        RadioGroup bleachingGroup = view.findViewById(R.id.bleaching_group);
        String bleach = getOptionFromRadioGroup(view, bleachingGroup);

        RadioGroup dryCleaningGroup = view.findViewById(R.id.dry_cleaning_group);
        String dryClean = getOptionFromRadioGroup(view, dryCleaningGroup);

        Map<String, Integer> compositionMap = parseFabricString(composition);

        ClothingItem clothingItem = new ClothingItem();
        clothingItem.setType(type);
        clothingItem.setColorName(colorName);
        clothingItem.setColorCategory(color);
        clothingItem.setPattern(pattern);
        clothingItem.setComposition(compositionMap);
        clothingItem.setMaterial(ResourcesTools.getMaterial(compositionMap, context));
        clothingItem.setCare(wash,tumbleDry,iron,bleach,dryClean);
        clothingItem.setPurchasePrice(price);
        clothingItem.setDateOfPurchase(date);
        clothingItem.setImageLink(imageUrl);
        clothingItem.setSelfMade(selfMade);

        if(id == null) {
            clothingItem.setId(clothingItem.hashCode());
        }else {
            //function came from editing clothing item
            clothingItem.setId(Integer.parseInt(id));
        }
        return clothingItem;
    }

    public static String getOptionFromRadioGroup(View view, RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        String selectedString = "";
        if (selectedId != -1) { // some button was selected
            RadioButton radioButton = view.findViewById(selectedId);
            selectedString = radioButton.getContentDescription().toString();
        }
        return selectedString;
    }

    public static String removeNewline(String input) {
        if (input != null && !input.isEmpty()) {
            if (input.charAt(input.length() - 1) == '\n') {
                return input.substring(0, input.length() - 1);
            }
        }
        return input;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
