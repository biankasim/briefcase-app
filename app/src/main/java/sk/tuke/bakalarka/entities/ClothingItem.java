package sk.tuke.bakalarka.entities;

import static sk.tuke.bakalarka.tools.ParseTools.removeNewline;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothingItem implements Parcelable {
    private int id;
    private String imageLink;
    private String type;
    private String colorName;
    private String colorCategory;
    private String pattern;
    private Map<String, String> care;

    private Map<String,Integer> composition;
    private String material;
    private LocalDate dateOfPurchase;
    private double purchasePrice;
    private boolean inLaundry;
    private int timesWorn;
    private int timesWashed;
    private int wearPercentile;
    private Map<String, Map<String, Integer>> timesWornSeparated;
    private boolean selfMade;
    public ClothingItem() {

    }

    protected ClothingItem(Parcel in) {
        id = in.readInt();
        imageLink = in.readString();
        type = in.readString();
        colorName = in.readString();
        colorCategory = in.readString();
        pattern = in.readString();
        care = new HashMap<>();
        in.readMap(care, getClass().getClassLoader());
        composition = new HashMap<>();
        in.readMap(composition, getClass().getClassLoader());
        material = in.readString();
        purchasePrice = in.readDouble();
        inLaundry = in.readByte() != 0;
        timesWorn = in.readInt();
        timesWashed = in.readInt();
        wearPercentile = in.readInt();
        timesWornSeparated = new HashMap<>();
    }

    public static final Creator<ClothingItem> CREATOR = new Creator<ClothingItem>() {
        @Override
        public ClothingItem createFromParcel(Parcel in) {
            return new ClothingItem(in);
        }

        @Override
        public ClothingItem[] newArray(int size) {
            return new ClothingItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(imageLink);
        dest.writeString(type);
        dest.writeString(colorName);
        dest.writeString(colorCategory);
        dest.writeString(pattern);
        dest.writeMap(care);
        dest.writeMap(composition);
        dest.writeString(material);
        dest.writeDouble(purchasePrice);
        dest.writeByte((byte) (inLaundry ? 1 : 0));
        dest.writeInt(timesWorn);
        dest.writeInt(timesWashed);
        dest.writeInt(wearPercentile);
        dest.writeMap(timesWornSeparated);
    }

    public void setSelfMade(boolean selfMade) {
        this.selfMade = selfMade;
    }

    public boolean isSelfMade() {
        return selfMade;
    }

    public Map<String, Map<String, Integer>> getTimesWornSeparated() {
        return timesWornSeparated;
    }

    public void setTimesWornSeparated(Map<String, Map<String, Integer>> timesWornSeparated) {
        this.timesWornSeparated = timesWornSeparated;
    }
    public int getTimesWornLastYear(String year){
        if(timesWornSeparated != null) {
            int sum = 0;
            for (Map.Entry<String, Integer> month : timesWornSeparated.get(year).entrySet()) {
                sum += month.getValue();
            }
            return sum;
        }
        return 0;
    }

    public int getWearPercentile() {
        return wearPercentile;
    }

    public void setWearPercentile(int wearPercentile) {
        this.wearPercentile = wearPercentile;
    }

    public void setColorCategory(String colorCategory) {
        this.colorCategory = colorCategory;
    }

    public String getColorCategory() {
        return colorCategory;
    }

    public void setTimesWashed(int timesWashed) {
        this.timesWashed = timesWashed;
    }

    public int getTimesWashed() {
        return timesWashed;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getMaterial() {
        return material;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageLink() {
        return imageLink;
    }

    public int getId() {
        return id;
    }

    public String getColorName() {
        return colorName;
    }

    public String getType() {
        return type;
    }

    public int getTimesWorn() {
        return timesWorn;
    }

    public void setTimesWorn(int timesWorn) {
        this.timesWorn = timesWorn;
    }

    public void setComposition(Map<String, Integer> composition) {
        this.composition = composition;
    }


    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getComposition() {
        return composition;
    }

    public String getPattern() {
        return pattern;
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setDateOfPurchase(LocalDate dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public void setInLaundry(boolean inLaundry) {
        this.inLaundry = inLaundry;
    }

    public void setPurchasePrice(double price) {
        this.purchasePrice = price;
    }

    public boolean isInLaundry() {
        return inLaundry;
    }

    public Map<String, String> getCare() {
        return care;
    }

    public void setCare(Map<String, String> care) {
        this.care = care;
    }

    public void wearItem() {
        this.timesWorn += 1;
    }

    public void setCare(String wash, String dry, String tumbleDry, String iron, String bleach, String dryClean, String wring) {
        if(this.care == null) {
            this.care = new HashMap<>();
        }
        this.care.put("wash",wash);
        this.care.put("dry",dry);
        this.care.put("tumble_dry",tumbleDry);
        this.care.put("iron",iron);
        this.care.put("bleach",bleach);
        this.care.put("dry_clean",dryClean);
        this.care.put("wring",wring);
    }
    public void setCare(String wash, String tumbleDry, String iron, String bleach, String dryClean) {
        if(this.care == null) {
            this.care = new HashMap<>();
        }
        this.care.put("wash",wash);
        this.care.put("tumble_dry",tumbleDry);
        this.care.put("iron",iron);
        this.care.put("bleach",bleach);
        this.care.put("dry_clean",dryClean);
    }

    public String getCareString() {
        String careString = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",care.get("wash"), care.get("tumble_dry"),
                care.get("dry"), care.get("iron"), care.get("bleach"), care.get("dry_clean"), care.get("bleach"));
        return careString;
    }
    public String getCompositionString() {
        if(composition == null) {
            return null;
        }
        String compositionString = "";
        for (Map.Entry<String, Integer> entry : composition.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            compositionString = compositionString + String.valueOf(value) + "% " + key + "\n";
        }
        return removeNewline(compositionString);
    }

    public static List<ClothingItem> getClothingByWash(List<ClothingItem> clothingItems, String washType) {
        List<ClothingItem> clothingItems1 = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            String wash = clothingItem.getCare().get("wash");
            if(wash != null) {
                if(wash.equals(washType)){
                    clothingItems1.add(clothingItem);
                }
            }
        }
        return clothingItems1;
    }

    public static List<ClothingItem> orderLaundryByColor(List<ClothingItem> clothingItems) {
        List<ClothingItem> white = new ArrayList<>();
        List<ClothingItem> black = new ArrayList<>();
        List<ClothingItem> colored = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            if(clothingItem.getColorCategory().equalsIgnoreCase("white")) {
                white.add(clothingItem);
            } else if (clothingItem.getColorCategory().equalsIgnoreCase("black")) {
                black.add(clothingItem);
            } else {
                colored.add(clothingItem);
            }
        }
        List<ClothingItem> ordered = new ArrayList<>();
        ordered.addAll(white);
        ordered.addAll(black);
        ordered.addAll(colored);
        return ordered;
    };

    public static Map<String, Map<String, List<ClothingItem>>> getClothingByWashAndColor(List<ClothingItem> clothingItems, String washType) {
        Map<String, Map<String, List<ClothingItem>>> result = new HashMap<>();

        for (ClothingItem clothingItem : clothingItems) {
            String wash = clothingItem.getCare().get("wash");
            if (wash != null && wash.equals(washType)) {
                String color = clothingItem.getColorCategory();

                // If the outer map doesn't contain the wash type, add it
                if (!result.containsKey(washType)) {
                    result.put(washType, new HashMap<>());
                }

                // Get the inner map for the current wash type
                Map<String, List<ClothingItem>> washMap = result.get(washType);

                // If the inner map doesn't contain the color, add it
                if (!washMap.containsKey(color)) {
                    washMap.put(color, new ArrayList<>());
                }

                // Add the clothing item to the list corresponding to the color
                washMap.get(color).add(clothingItem);
            }
        }

        return result;
    }


}
