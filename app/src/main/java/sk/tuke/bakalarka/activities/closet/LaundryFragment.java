package sk.tuke.bakalarka.activities.closet;

import static sk.tuke.bakalarka.tools.DbTools.toggleInLaundry;
import static sk.tuke.bakalarka.tools.DbTools.washClothingItem;
import static sk.tuke.bakalarka.tools.ParseTools.getClothingItemModels;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.recycler_view.PhotoItemAdapter;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.tools.DbTools;


public class LaundryFragment extends Fragment implements RecyclerViewInterface {
    private String userId;
    private List<List<ClothingItem>> clothingItems;

    public LaundryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_laundry, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }
        clothingItems = new ArrayList<>();

        TextView closetTitle = view.findViewById(R.id.closet_title);
        closetTitle.setOnClickListener(v -> replaceFragment(ClosetFragment.newInstance("-","-","-","-","-","-",false,false)));

        DbTools.getUserClothingItems(userId, "colorCategory", true, new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                LinearLayout linearLayout = requireView().findViewById(R.id.linear_layout);
                if(!clothingItems.isEmpty()) {
                    //get all kinds of washing
                    HashSet<String> washingTypes = new HashSet<>();
                    for (ClothingItem clothingItem : clothingItems) {
                        washingTypes.add(clothingItem.getCare().get("wash"));
                    }

                    if(!washingTypes.isEmpty()) {
                        //show washing type and clothes
                        int i = 0;
                        for(String washingType : washingTypes) {
                            //define textview and switch
                            TextView textView1 = new TextView(requireContext());
                            textView1.setText(washingType);
                            SwitchMaterial switchMaterial1 = new SwitchMaterial(requireContext());

                            switchMaterial1.setTag(i);
                            switchMaterial1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    int position = (int) buttonView.getTag(); //get the position from the tag
                                    //change laundry attributes
                                    for(ClothingItem clothingItem : LaundryFragment.this.clothingItems.get(position)){
                                        toggleInLaundry(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                String.valueOf(clothingItem.getId()),false);
                                        washClothingItem(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                String.valueOf(clothingItem.getId()));
                                    }
                                    Toast.makeText(requireContext(),"Wash Recorded",Toast.LENGTH_SHORT).show();
                                }
                            });
                            //add text and switch to horizontal layout
                            LinearLayout horizontalLayout = new LinearLayout(requireContext());
                            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                            horizontalLayout.addView(textView1);
                            horizontalLayout.addView(switchMaterial1);

                            //add horizontal layout to vertical linear layout
                            linearLayout.addView(horizontalLayout);


                            //set recyclerview
                            List<ClothingItem> clothingItems1 = ClothingItem.getClothingByWash(clothingItems, washingType);
                            clothingItems1 = ClothingItem.orderLaundryByColor(clothingItems1);
                            LaundryFragment.this.clothingItems.add(clothingItems1);
                            RecyclerView recyclerView1 = new RecyclerView(requireContext());
                            recyclerView1.setHasFixedSize(true);
                            recyclerView1.setId(i);
                            RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getContext(),5);

                            recyclerView1.setLayoutManager(layoutManager1);
                            RecyclerView.Adapter<PhotoItemHolder> adapter1 = new PhotoItemAdapter(
                                    getClothingItemModels(clothingItems1),
                                    new WeakReference<>(getContext()),
                                    LaundryFragment.this, i);
                            recyclerView1.setAdapter(adapter1);
                            linearLayout.addView(recyclerView1);
                            i++;
                        }

                    }

                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int position, int recyclerViewId) {
        showClothingItemDetail(clothingItems.get(recyclerViewId).get(position));
    }

    private void showClothingItemDetail(ClothingItem clothingItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clothingItem", clothingItem);

        ClothingItemFragment clothingItemFragment = new ClothingItemFragment();
        clothingItemFragment.setArguments(bundle);

        replaceFragment(clothingItemFragment);
    }
}