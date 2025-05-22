package sk.tuke.bakalarka.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Outfit implements Parcelable {
    private int id;
    private String imageLink;
    private HashSet<String> clothingItems;
    private String season;
    private String occasion;
    private List<String> datesWorn;
    public Outfit() {

    }

    protected Outfit(Parcel in) {
        id = in.readInt();
        imageLink = in.readString();
        clothingItems = new HashSet<>(in.createStringArrayList());
        season = in.readString();
        occasion = in.readString();
        datesWorn = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(imageLink);
        dest.writeStringList(new ArrayList<>(clothingItems));
        dest.writeString(season);
        dest.writeString(occasion);
        dest.writeStringList(datesWorn);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Outfit> CREATOR = new Creator<Outfit>() {
        @Override
        public Outfit createFromParcel(Parcel in) {
            return new Outfit(in);
        }

        @Override
        public Outfit[] newArray(int size) {
            return new Outfit[size];
        }
    };


    public List<String> getDatesWorn() {
        if(datesWorn == null) {
            datesWorn = new ArrayList<>();
        }
        return datesWorn;
    }
    public int getTimesWorn() {
        if(datesWorn == null) {
            return 0;
        }
        return this.datesWorn.size();
    }
    public List<String> getClothingItemsIds() {
        List<String> ids = new ArrayList<>(clothingItems);
        return ids;
    }

    public void setDatesWorn(List<String> datesWorn) {
        this.datesWorn = datesWorn;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setClothingItems(HashSet<String> clothingItems) {
        this.clothingItems = clothingItems;
    }

    public HashSet<String> getClothingItems() {
        return clothingItems;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getOccasion() {
        return occasion;
    }

    public String getSeason() {
        return season;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
