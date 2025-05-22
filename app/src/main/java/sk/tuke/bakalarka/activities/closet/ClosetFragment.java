package sk.tuke.bakalarka.activities.closet;

import static sk.tuke.bakalarka.tools.ParseTools.getClothingItemModels;
import static sk.tuke.bakalarka.tools.ResourcesTools.calculateNumberOfColumns;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.tools.DbTools;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.recycler_view.PhotoItemAdapter;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.PhotoItemModel;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;

public class ClosetFragment extends Fragment implements RecyclerViewInterface {
    private static final String ARG_TYPE = "type";
    private static final String ARG_COLOR = "color";
    private static final String ARG_PATTERN = "pattern";
    private static final String ARG_MATERIAL = "material";
    private static final String ARG_ORIGIN = "origin";
    private static final String ARG_SORT_BY = "sort_by";
    private static final String ARG_DESCENDING = "descending";
    private static final String ARG_INCLUDE_LAUNDRY = "include_laundry";

    private String mType;
    private String mColor;
    private String mPattern;
    private String mMaterial;
    private String mOrigin;
    private String mSortBy;
    private boolean mDescending;
    private boolean mIncludeLaundry;
    private List<ClothingItem> clothingItems;
    private String userId;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<PhotoItemHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    public ClosetFragment() {
        // Required empty public constructor
    }

    public static ClosetFragment newInstance(String type, String color, String pattern, String material, String origin, String sortBy, boolean descending, boolean includeLaundry) {
        ClosetFragment fragment = new ClosetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_COLOR, color);
        args.putString(ARG_PATTERN, pattern);
        args.putString(ARG_MATERIAL, material);
        args.putString(ARG_ORIGIN, origin);
        args.putString(ARG_SORT_BY, sortBy);
        args.putBoolean(ARG_DESCENDING, descending);
        args.putBoolean(ARG_INCLUDE_LAUNDRY, includeLaundry);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mColor = getArguments().getString(ARG_COLOR);
            mPattern = getArguments().getString(ARG_PATTERN);
            mMaterial = getArguments().getString(ARG_MATERIAL);
            mOrigin = getArguments().getString(ARG_ORIGIN);
            mSortBy = getArguments().getString(ARG_SORT_BY);
            mDescending = getArguments().getBoolean(ARG_DESCENDING);
            mIncludeLaundry = getArguments().getBoolean(ARG_INCLUDE_LAUNDRY);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) {
            Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
            return;
        }
        userId = user.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_closet, container, false);

        if(userId==null) {
            return view;
        }
        ClosetFragment.this.clothingItems = new ArrayList<>();

        setRecyclerView(view);

        TextView laundryBtn = view.findViewById(R.id.laundry_btn);
        laundryBtn.setOnClickListener(v -> replaceFragment(new LaundryFragment()));

        LinearLayout filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(v -> replaceFragment(new ClosetFilterFragment()));

        AppCompatButton addClothesBtn = view.findViewById(R.id.sign_in_btn);
        addClothesBtn.setOnClickListener(v -> replaceFragment(new AddClothingItemFragment()));

        AppCompatButton closetStatsBtn = view.findViewById(R.id.closet_stats_btn);
        closetStatsBtn.setOnClickListener(v -> replaceFragment(new ClosetStatsFragment()));
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
                ClosetFragment.this.clothingItems = clothingItems;
                if(clothingItems.isEmpty()) {
                    TextView noItemsTextView = view.findViewById(R.id.no_items_text_view);
                    noItemsTextView.setText("No items found");
                }else{
                    showItemsInRecyclerView();
            }
        }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showItemsInRecyclerView() {
        List<PhotoItemModel> data = getClothingItemModels(clothingItems);
        adapter = new PhotoItemAdapter(data,new WeakReference<>(getContext()),ClosetFragment.this,0);
        recyclerView.setAdapter(adapter);
    }



    private void setRecyclerView(View view){
        recyclerView = view.findViewById(R.id.rv_clothing_item);
        int numberOfColumns = calculateNumberOfColumns(requireContext(),60);
        layoutManager = new GridLayoutManager(getContext(),numberOfColumns);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoItemAdapter(new ArrayList<>(),new WeakReference<>(getContext()),ClosetFragment.this,0);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, int recyclerViewId) {
        Log.i("clothing id",String.valueOf(clothingItems.get(position).getId()));

        //pass clicked clothing item to new fragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("clothingItem", clothingItems.get(position));

        ClothingItemFragment clothingItemFragment = new ClothingItemFragment();
        clothingItemFragment.setArguments(bundle);

        replaceFragment(clothingItemFragment);

    }



    private Query createQuery() {
        if(userId == null) {
            return null;
        }
        Query query = FirebaseFirestore.getInstance().collection("user_clothes")
                .document(userId).collection("clothes");

        if(getArguments() != null) {
            if(!mIncludeLaundry) {
                query = query.whereEqualTo("inLaundry",mIncludeLaundry);
            }
            Query.Direction direction = Query.Direction.ASCENDING;
            if(mDescending) {
                direction = Query.Direction.DESCENDING;
            }
            if(!mType.equals("-")) {
                query = query.whereEqualTo("type",mType);
            }
            if(!mColor.equals("-")) {
                query = query.whereEqualTo("colorCategory",mColor);
            }
            if(!mPattern.equals("-")) {
                query = query.whereEqualTo("pattern",mPattern);
            }
            if(!mMaterial.equals("-")) {
                query = query.whereEqualTo("material",mMaterial);
            }
            if(!mOrigin.equals("-")) {
                if(mOrigin.equalsIgnoreCase("self-made")) {
                    query = query.whereEqualTo("selfMade",true);
                }else{
                    query = query.whereEqualTo("selfMade",false);
                }
            }
            if(mSortBy.equals("color")) {
                query = query.orderBy("colorCategory",direction);
            }
            if(mSortBy.equals("times worn")) {
                query = query.orderBy("timesWorn",direction);
            }
            if(mSortBy.equals("purchase price")) {
                query = query.orderBy("purchasePrice",direction);
            }
            if(mSortBy.equals("date of purchase")) {
                query = query.orderBy("date_of_purchase",direction);
            }
            return query;
        }
        //arguments not given, show all clothes
        return query;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("closet");
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}