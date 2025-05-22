package sk.tuke.bakalarka.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class SwapItem extends ClothingItem implements Parcelable {
    private int swapId;
    private String userId;
    private double swapPrice;
    private String details;
    private String condition;
    private HashMap<String,String> requests;
    private String acceptedRequest;

    public SwapItem() {
        // Required empty constructor
    }

    protected SwapItem(Parcel in) {
        super(in);
        swapId = in.readInt();
        userId = in.readString();
        swapPrice = in.readDouble();
        details = in.readString();
        condition = in.readString();
        requests = new HashMap<>();
        acceptedRequest = in.readString();
    }

    public HashMap<String, String> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, String> requests) {
        this.requests = requests;
    }

    public double getSwapPrice() {
        return swapPrice;
    }

    public String getDetails() {
        return details;
    }

    public String getCondition() {
        return condition;
    }

    public void setSwapPrice(double swapPrice) {
        this.swapPrice = swapPrice;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(swapId);
        dest.writeString(userId);
        dest.writeDouble(swapPrice);
        dest.writeString(details);
        dest.writeString(condition);
        dest.writeString(acceptedRequest);
        dest.writeMap(requests);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SwapItem> CREATOR = new Creator<SwapItem>() {
        @Override
        public SwapItem createFromParcel(Parcel in) {
            return new SwapItem(in);
        }

        @Override
        public SwapItem[] newArray(int size) {
            return new SwapItem[size];
        }
    };

    public int getSwapId() {
        return swapId;
    }

    public void setSwapId(int swapId) {
        this.swapId = swapId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAcceptedRequest() {
        return acceptedRequest;
    }

    public void setAcceptedRequest(String acceptedRequest) {
        this.acceptedRequest = acceptedRequest;
    }
}
