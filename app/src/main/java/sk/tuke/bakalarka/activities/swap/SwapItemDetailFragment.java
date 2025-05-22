package sk.tuke.bakalarka.activities.swap;


import static sk.tuke.bakalarka.activities.closet.ClothingItemFragment.setDetailInLayout;
import static sk.tuke.bakalarka.tools.DbTools.sendSwapRequest;
import static sk.tuke.bakalarka.tools.ParseTools.removeNewline;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.roundToTwoDecimal;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.SwapItem;

public class SwapItemDetailFragment extends Fragment {
    private static final String ARG_SWAP_ITEM = "swapItem";
    private SwapItem mSwapItem;
    private String userId;

    public SwapItemDetailFragment() {
        // Required empty public constructor
    }

    public static SwapItemDetailFragment newInstance(String swapItem) {
        SwapItemDetailFragment fragment = new SwapItemDetailFragment();
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_swap_item_detail, container, false);

        AppCompatButton getBtn = view.findViewById(R.id.get_btn);
        getBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userId == null) {
                    Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendSwapRequest(requireContext(),
                        userId,
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        String.valueOf(mSwapItem.getSwapId()));
                replaceFragment(new SwapFragment());
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

    public static void showSwapItem(Context context, View view, SwapItem mSwapItem) {
        TextView condition = view.findViewById(R.id.condition);
        TextView details = view.findViewById(R.id.details);
        ImageView imageView = view.findViewById(R.id.imageView);

        Glide
                .with(context)
                .load(mSwapItem.getImageLink())
                .into(imageView);

        setDetailInLayout(R.id.price_layout, R.drawable.ic_money, roundToTwoDecimal(mSwapItem.getSwapPrice()),context, view);
        setDetailInLayout(R.id.materials_layout, R.drawable.ic_material, mSwapItem.getCompositionString(),context, view);
        condition.setText(String.format("Condition: %s", mSwapItem.getCondition()));
        details.setText(String.format("Details:\n%s", mSwapItem.getDetails()));

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

}