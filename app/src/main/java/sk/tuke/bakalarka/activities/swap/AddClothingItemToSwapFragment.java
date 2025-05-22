package sk.tuke.bakalarka.activities.swap;

import static sk.tuke.bakalarka.tools.DbTools.addOutfitToDatabase;
import static sk.tuke.bakalarka.tools.DbTools.addSwapItemToDatabase;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.SwapItem;

public class AddClothingItemToSwapFragment extends Fragment {

    private static final String ARG_CLOTHING_ITEM = "clothingItem";
    private ClothingItem mClothingItem;
    private String userId;
    private SwapItem swapItem;
    public AddClothingItemToSwapFragment() {
        // Required empty public constructor
    }

    public static AddClothingItemToSwapFragment newInstance(String clothingItem) {
        AddClothingItemToSwapFragment fragment = new AddClothingItemToSwapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLOTHING_ITEM, clothingItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClothingItem = getArguments().getParcelable(ARG_CLOTHING_ITEM);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_clothing_item_to_swap, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        Glide
                .with(requireContext())
                .load(mClothingItem.getImageLink())
                .into(imageView);

        AppCompatButton addBtn = view.findViewById(R.id.add_to_swap_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapItem = getSwapItem();
                addToDatabaseAsync();
            }
        });

        return view;
    }

    private void addToDatabaseAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                addSwapItemToDatabase(userId,swapItem,requireContext());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        replaceFragment(new SwapFragment());
                    }
                });
            }
        });
    }

    private SwapItem getSwapItem() {
        SwapItem swapItem = new SwapItem();
        EditText priceEditText = requireView().findViewById(R.id.priceEditText);
        EditText detailsEditText = requireView().findViewById(R.id.detailsEditText);

        double price = 0.0;
        if(!priceEditText.getText().toString().isEmpty()) {
            price = Double.parseDouble(String.valueOf(priceEditText.getText()));
        }
        swapItem.setSwapPrice(price);
        swapItem.setDetails(String.valueOf(detailsEditText.getText()));
        if(mClothingItem.getTimesWorn() == 0) {
            swapItem.setCondition("new");
        }else{
            swapItem.setCondition("worn");
        }

        swapItem.setId(mClothingItem.getId());
        swapItem.setUserId(userId);
        swapItem.setImageLink(mClothingItem.getImageLink());
        swapItem.setType(mClothingItem.getType());
        swapItem.setColorCategory(mClothingItem.getColorCategory());
        swapItem.setColorName(mClothingItem.getColorName());
        swapItem.setPattern(mClothingItem.getPattern());
        swapItem.setCare(mClothingItem.getCare());
        swapItem.setMaterial(mClothingItem.getMaterial());
        swapItem.setComposition(mClothingItem.getComposition());


        swapItem.setSwapId(swapItem.hashCode());

        return swapItem;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}