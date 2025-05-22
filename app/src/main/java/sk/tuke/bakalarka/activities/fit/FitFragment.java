package sk.tuke.bakalarka.activities.fit;

import static android.view.View.GONE;
import static sk.tuke.bakalarka.tools.ColorCategorizer.categorizeColor;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserColorPalette;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.getWearingFrequency;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.fit.recycler_view.ClothingItemAdapter;
import sk.tuke.bakalarka.activities.fit.recycler_view.ClothingItemHolder;
import sk.tuke.bakalarka.activities.fit.recycler_view.ClothingItemModel;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.tools.DbTools;


public class FitFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_COLOR = "color";
    private static final String ARG_COLOR_IN_PALETTE = "color_in_palette";

    private String mColor;
    private String mType;
    private boolean mColorInPalette;
    private List<ClothingItem> clothingItems;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter<ClothingItemHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    public FitFragment() {
        // Required empty public constructor
    }

    public static FitFragment newInstance(String type, String color, boolean colorInPalette) {
        FitFragment fragment = new FitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_COLOR, color);
        args.putBoolean(ARG_COLOR_IN_PALETTE, colorInPalette);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mColor = getArguments().getString(ARG_COLOR);
            mColorInPalette = getArguments().getBoolean(ARG_COLOR_IN_PALETTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_fit, container, false);

        //set recycler view
        recyclerView = view.findViewById(R.id.rv_clothing_item);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ClothingItemAdapter(new ArrayList<>(),new WeakReference<>(getContext()));
        recyclerView.setAdapter(adapter);


        LinearLayout linearLayout = view.findViewById(R.id.evaluation_layout);
        TextView textView = linearLayout.findViewById(R.id.text_view);
        ImageView imageView = linearLayout.findViewById(R.id.image_view);
        Drawable drawable = null;
        if(!getUserColorPalette(requireContext()).equals("null")) {
            if(mColorInPalette) {
                drawable = ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_tick,null);
                textView.setText("color IS in your color palette");
                imageView.setBackground(drawable);
                imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BBFF00")));
            }else{
                drawable = ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_cross,null);
                textView.setText("color IS NOT in your color palette");
                imageView.setBackground(drawable);
                imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E60000")));
            }

        }else{
            linearLayout.setVisibility(GONE);
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //get user clothes
        Query query = createQuery();
        DbTools.getUserClothingItems(query, new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {

                FitFragment.this.clothingItems = clothingItems;
                if(clothingItems == null || clothingItems.isEmpty()) {
                    //no similar items found
                    LinearLayout linearLayout = requireView().findViewById(R.id.linear_layout_title);
                    TextView textView = (TextView) linearLayout.getChildAt(1);
                    linearLayout.getChildAt(0).setVisibility(GONE);
                    textView.setText("No similar items found");
                }else {
                    //show clothes images in recycler view
                    List<ClothingItemModel> data = getClothingItemFitModels(clothingItems);
                    adapter = new ClothingItemAdapter(data, new WeakReference<>(getContext()));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Query createQuery() {
        Query query = FirebaseFirestore.getInstance().collection("user_clothes")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("clothes")
                .whereEqualTo("type",mType.toLowerCase())
                .whereEqualTo("colorCategory",categorizeColor(mColor));
        return query;
    }

    private static List<ClothingItemModel> getClothingItemFitModels(List<ClothingItem> clothingItems) {
        List<ClothingItemModel>  clothingItemModels = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            ClothingItemModel clothingItemModel = new ClothingItemModel(clothingItem.getImageLink(),getWearingFrequency(clothingItem));
            clothingItemModels.add(clothingItemModel);
        }
        return clothingItemModels;
    }

}