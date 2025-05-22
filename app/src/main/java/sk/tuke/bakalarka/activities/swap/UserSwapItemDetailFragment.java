package sk.tuke.bakalarka.activities.swap;

import static sk.tuke.bakalarka.activities.swap.SwapItemDetailFragment.showSwapItem;
import static sk.tuke.bakalarka.tools.DbTools.moveImageInFirebaseStorage;
import static sk.tuke.bakalarka.tools.DbTools.removeSwapItemFromDatabase;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.roundToTwoDecimal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.SwapItem;


public class UserSwapItemDetailFragment extends Fragment {

    private static final String ARG_SWAP_ITEM = "swapItem";
    private SwapItem mSwapItem;
    private String userId;

    public UserSwapItemDetailFragment() {
        // Required empty public constructor
    }

    public static UserSwapItemDetailFragment newInstance(String swapItem) {
        UserSwapItemDetailFragment fragment = new UserSwapItemDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SWAP_ITEM, swapItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSwapItem = getArguments().getParcelable(ARG_SWAP_ITEM);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_swap_item_detail, container, false);

        AppCompatButton deleteBtn = view.findViewById(R.id.delete_swap_item_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSwapItemFromDatabase(userId,String.valueOf(mSwapItem.getSwapId()),String.valueOf(mSwapItem.getId()));
                replaceFragment(new SwapFragment());
            }
        });

        AppCompatButton giveAwayBtn = view.findViewById(R.id.give_away_btn);
        giveAwayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("swapItem", mSwapItem);
                GiveAwaySwapItemFragment giveAwaySwapItemFragment = new GiveAwaySwapItemFragment();
                giveAwaySwapItemFragment.setArguments(bundle);
                replaceFragment(giveAwaySwapItemFragment);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mSwapItem != null) {
            showSwapItem(requireContext(),view,mSwapItem);
        }
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}