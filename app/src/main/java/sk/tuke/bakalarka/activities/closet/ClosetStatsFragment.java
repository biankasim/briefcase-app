package sk.tuke.bakalarka.activities.closet;

import static sk.tuke.bakalarka.entities.WebColors.getColorValueByName;
import static sk.tuke.bakalarka.tools.DbTools.getUserClothingItems;
import static sk.tuke.bakalarka.tools.ResourcesTools.getResourcesValueFromPosition;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.parseStatisticsToString;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateField;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.tools.ChartVisualizer;
import sk.tuke.bakalarka.tools.DbTools;

public class ClosetStatsFragment extends Fragment {
    private String userId;
    private long clothingCount;
    private ChartVisualizer chartVisualizer;
    public ClosetStatsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_closet_stats, container, false);

        Spinner spinner = view.findViewById(R.id.type_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] types = getResources().getStringArray(R.array.type_array);

                LinearLayout colorsLayout = requireView().findViewById(R.id.colors_layout);
                colorsLayout.removeAllViewsInLayout();

                showStatsForType(getResourcesValueFromPosition(types,position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chartVisualizer = new ChartVisualizer();
        chartVisualizer.setView(view);
        chartVisualizer.setSpendingsChart(view.findViewById(R.id.bar_chart));
        chartVisualizer.setUsageChart(view.findViewById(R.id.closet_usage_chart));
    }

    private void showStatsForType(String type) {
        if(userId == null) {
            return;
        }
        Query query = createQuery(type);
        getItemCount(query);
        getClosetWorth(query);
        chartVisualizer.getSpendingByMonths(query);
        getClothingItems(query);
    }

    private void getItemCount(Query query) {
        AggregateQuery countQuery = query.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    TextView textView = requireView().findViewById(R.id.clothing_count);
                    String text = parseStatisticsToString("Clothing Item Count",String.valueOf(snapshot.getCount()));
                    textView.setText(text);
                    clothingCount = snapshot.getCount();
                    getClosetUsage(query);
                }
            }
        });
    }


    private void getClosetUsage(Query query) {
        query = query.whereNotEqualTo("timesWorn",0);
        AggregateQuery sumQuery = query.count();
        sumQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    int percentage = (int) ((float) snapshot.getCount() / clothingCount * 100);
                    chartVisualizer.showUsageChart(percentage);
                }
            }
        });
    }




    private void getClosetWorth(Query query) {
        AggregateQuery sumQuery = query.aggregate(AggregateField.sum("purchasePrice"));
        sumQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    double sum = snapshot.getDouble(AggregateField.sum("purchasePrice"));
                    String roundedValue = String.format("%.2f", sum);
                    TextView textView = requireView().findViewById(R.id.closet_worth);
                    String text = parseStatisticsToString("Closet Worth", roundedValue);
                    textView.setText(text);
                }

            }
        });
    }



    private Query createQuery(String type) {
        if(userId == null) {
            return null;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference clothesRef = db.collection("user_clothes").document(userId).collection("clothes");

        if (!type.equals("-")) {
            return clothesRef.whereEqualTo("type", type);
        } else {
            return clothesRef;
        }
    }

/*
    private void getLeastWornItems(List<ClothingItem> clothingItems) {
        clothingItems.sort(Comparator.comparingDouble(ClothingItem::getPurchasePrice));
        TextView textView = requireView().findViewById(R.id.least_worn_title);
        LinearLayout layout = requireView().findViewById(R.id.least_worn_layout);
        layout.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        if(clothingItems.size() < 3) {
            layout.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            return;
        }
        List<ClothingItem> leastWorn = new ArrayList<>(clothingItems.subList(0, 3));
        ImageView imageView1 = requireView().findViewById(R.id.image_view_least_1);
        ImageView imageView2 = requireView().findViewById(R.id.image_view_least_2);
        ImageView imageView3 = requireView().findViewById(R.id.image_view_least_3);
        showItems(leastWorn, imageView1, imageView2, imageView3);

    }

    private void getMostWornItems(List<ClothingItem> clothingItems) {
        clothingItems.sort(Comparator.comparingDouble(ClothingItem::getPurchasePrice).reversed());
        TextView textView = requireView().findViewById(R.id.most_worn_title);
        LinearLayout layout = requireView().findViewById(R.id.most_worn_layout);
        layout.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        if(clothingItems.size() < 3) {
            layout.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            return;
        }
        List<ClothingItem> mostWorn = new ArrayList<>(clothingItems.subList(0, 3));
        ImageView imageView1 = requireView().findViewById(R.id.image_view_most_1);
        ImageView imageView2 = requireView().findViewById(R.id.image_view_most_2);
        ImageView imageView3 = requireView().findViewById(R.id.image_view_most_3);
        showItems(mostWorn, imageView1, imageView2, imageView3);

    }



 */

    private void getClothingItems(Query query) {
        getUserClothingItems(query, new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                showMedianPurchasePrice(clothingItems);
                //getLeastWornItems(clothingItems);
                //getMostWornItems(clothingItems);
                showColors(clothingItems);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private List<Double> getPurchasePrices(List<ClothingItem> clothingItems) {
        List<Double> purchasePrices = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            purchasePrices.add(clothingItem.getPurchasePrice());
        }
        return purchasePrices;
    }
    private double getMedianPurchasePrice(List<Double> purchasePrices) {
        Collections.sort(purchasePrices);
        int size = purchasePrices.size();
        if (size == 0) {
            return 0;
        }

        if (size % 2 == 0) {//list size is even, take the average of the two middle elements
            int mid = size / 2;
            return (purchasePrices.get(mid - 1) + purchasePrices.get(mid)) / 2.0;
        } else {//list size is odd, return the middle element
            return purchasePrices.get(size / 2);
        }
    }
    private void showMedianPurchasePrice(List<ClothingItem> clothingItems) {
        List<Double> purchasePrices = getPurchasePrices(clothingItems);
        double median = getMedianPurchasePrice(purchasePrices);
        TextView textView = requireView().findViewById(R.id.median_price);
        String text = parseStatisticsToString("Median Purchase Price",String.valueOf(median));
        textView.setText(text);
    }

    private Map<String,Integer> getColors(List<ClothingItem> clothingItems) {
        Map<String,Integer> colorsCount = new TreeMap<>();
        for (ClothingItem clothingItem : clothingItems) {
            String color = clothingItem.getColorName();
            if(color == null) {//color name not given
                color = clothingItem.getColorCategory();
            }
            if(color != null) {//color name or category given
                if (colorsCount.containsKey(color)) {//color is already in map
                    int colorCount = colorsCount.get(color);
                    colorsCount.put(color, colorCount + 1);
                } else {
                    colorsCount.put(color, 1);
                }
            }
        }
        return colorsCount;
    }

    private void showColors(List<ClothingItem> clothingItems) {
        Map<String,Integer> colorsCount = getColors(clothingItems);
        LinearLayout colorsLayout = requireView().findViewById(R.id.colors_layout);

        int total = 0;
        for (Integer value : colorsCount.values()) {
            total += value;
        }

        //divide linear layout to color blocks
        for (Map.Entry<String, Integer> entry : colorsCount.entrySet()) {
            String colorName = entry.getKey();
            int count = entry.getValue();

            float percentage = ((float) count / total) * 100;

            View colorView = new View(requireContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, percentage / 100);
            colorView.setLayoutParams(layoutParams);
            colorView.setBackgroundColor(getColorValueByName(colorName));
            colorsLayout.addView(colorView);
        }
    }
/*

    private void showItems(List<ClothingItem> clothingItems, ImageView imageView1, ImageView imageView2, ImageView imageView3) {
        if (clothingItems.size() >= 3) {
            Glide
                    .with(requireContext())
                    .load(clothingItems.get(0).getImageLink())
                    .into(imageView1);
            Glide
                    .with(requireContext())
                    .load(clothingItems.get(1).getImageLink())
                    .into(imageView2);
            Glide
                    .with(requireContext())
                    .load(clothingItems.get(2).getImageLink())
                    .into(imageView3);
        }
    }



 */
}