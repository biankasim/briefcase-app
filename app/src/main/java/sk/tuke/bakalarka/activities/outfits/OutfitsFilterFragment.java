package sk.tuke.bakalarka.activities.outfits;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import sk.tuke.bakalarka.R;


public class OutfitsFilterFragment extends Fragment {


    public OutfitsFilterFragment() {
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
        View view = inflater.inflate(R.layout.fragment_outfits_filter, container, false);

        AppCompatButton filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAttributesAndShowOutfitsFragment();
            }
        });


        return view;
    }


    private void getAttributesAndShowOutfitsFragment() {
        Spinner occasionSpinner = (Spinner) requireView().findViewById(R.id.occasion_spinner);
        Spinner seasonSpinner = (Spinner) requireView().findViewById(R.id.season_spinner);

        String occasion = occasionSpinner.getSelectedItem().toString();
        String season = seasonSpinner.getSelectedItem().toString();

        replaceFragment(OutfitsFragment.newInstance(occasion, season, null));

    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}