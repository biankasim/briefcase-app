package sk.tuke.bakalarka.activities.outfits;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.tuke.bakalarka.R;


public class PlanOutfitDialogFragment extends Fragment {
    private static final String ARG_DATE = "date";
    private String mDate;

    public PlanOutfitDialogFragment() {
        // Required empty public constructor
    }
    public static PlanOutfitDialogFragment newInstance(String date) {
        PlanOutfitDialogFragment fragment = new PlanOutfitDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mDate = getArguments().getString(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plan_outfit_dialog, container, false);

        AppCompatButton selectFromSavedBtn = view.findViewById(R.id.select_from_saved_btn);
        AppCompatButton createNewOutfitBtn = view.findViewById(R.id.create_new_outfit_btn);

        selectFromSavedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(OutfitsFragment.newInstance(null, null, mDate));
            }
        });

        createNewOutfitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(AddOutfitFragment.newInstance(mDate));
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}