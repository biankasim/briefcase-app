package sk.tuke.bakalarka.activities.swap;

import static android.view.View.GONE;
import static sk.tuke.bakalarka.tools.ParseTools.getSwapItemModels;
import static sk.tuke.bakalarka.tools.ResourcesTools.calculateNumberOfColumns;


import android.os.Bundle;


import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.swap.recycler_view.SwapAdapter;
import sk.tuke.bakalarka.activities.swap.recycler_view.SwapHolder;
import sk.tuke.bakalarka.activities.swap.recycler_view.SwapModel;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.SwapItem;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;
import sk.tuke.bakalarka.tools.DbTools;

public class SwapFragment extends Fragment implements RecyclerViewInterface {


    private RecyclerView recyclerView;
    private RecyclerView.Adapter<SwapHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ClothingItem> clothingItems;
    private String userId;
    public SwapFragment() {
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
        View view = inflater.inflate(R.layout.fragment_swap, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) {
            Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
            TextView mySwapItemsBtn = view.findViewById(R.id.my_swap_items);
            mySwapItemsBtn.setVisibility(GONE);
            return view;
        }
        userId = user.getUid();

        recyclerView = view.findViewById(R.id.rv_swap_item);
        int numberOfColumns = calculateNumberOfColumns(requireContext(),240);
        layoutManager = new GridLayoutManager(getContext(),numberOfColumns);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SwapAdapter(new ArrayList<>(),new WeakReference<>(getContext()), SwapFragment.this);
        recyclerView.setAdapter(adapter);

        TextView mySwapItemsBtn = view.findViewById(R.id.my_swap_items);
        mySwapItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null) {
                    return;
                }
                DbTools.getUserSwapItems(user.getUid(), new DbTools.OnUserClothingItemsCallback() {
                    @Override
                    public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                        SwapFragment.this.clothingItems = clothingItems;
                        showItemsInRecyclerView();
                        TextView frameTitleTextView = requireView().findViewById(R.id.frame_title);
                        frameTitleTextView.setText("My Items");
                        mySwapItemsBtn.setVisibility(GONE);
                        TextView noItemsTextView = view.findViewById(R.id.no_items_text_view);
                        noItemsTextView.setVisibility(GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        DbTools.getSwapItems(userId,new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                SwapFragment.this.clothingItems = clothingItems;
                showItemsInRecyclerView();
                if(clothingItems.isEmpty()) {
                    TextView noItemsTextView = view.findViewById(R.id.no_items_text_view);
                    noItemsTextView.setText("No items found");
                }

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
    private void showItemsInRecyclerView() {
        List<SwapModel> swapModels = getSwapItemModels(clothingItems);
        adapter = new SwapAdapter(swapModels,new WeakReference<>(getContext()), SwapFragment.this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(int position, int recyclerViewId) {
        SwapItem clickedSwapItem = (SwapItem) clothingItems.get(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable("swapItem", clickedSwapItem);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            if(String.valueOf(clickedSwapItem.getUserId()).equals(user.getUid())) {
                //user's own swap item
                UserSwapItemDetailFragment userSwapItemDetailFragment = new UserSwapItemDetailFragment();
                userSwapItemDetailFragment.setArguments(bundle);
                replaceFragment(userSwapItemDetailFragment);
            }else{
                //other user wants to view swap item of other user
                SwapItemDetailFragment swapItemDetailFragment = new SwapItemDetailFragment();
                swapItemDetailFragment.setArguments(bundle);
                replaceFragment(swapItemDetailFragment);
            }
        }
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("swap");
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}