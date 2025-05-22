package sk.tuke.bakalarka.activities.closet;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.google.android.material.switchmaterial.SwitchMaterial;

import sk.tuke.bakalarka.R;


public class ClosetFilterFragment extends Fragment {


    public ClosetFilterFragment() {
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
        View view = inflater.inflate(R.layout.fragment_closet_filter, container, false);


        AppCompatButton filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAttributesAndShowClosetFragment();
            }
        });

        return view;
    }


    private void getAttributesAndShowClosetFragment() {
        Spinner typeSpinner = (Spinner) requireView().findViewById(R.id.type_spinner);
        Spinner colorSpinner = (Spinner) requireView().findViewById(R.id.color_spinner);
        Spinner patternSpinner = (Spinner) requireView().findViewById(R.id.pattern_spinner);
        Spinner materialSpinner = (Spinner) requireView().findViewById(R.id.material_spinner);
        Spinner originSpinner = (Spinner) requireView().findViewById(R.id.origin_spinner);
        Spinner sortBySpinner = (Spinner) requireView().findViewById(R.id.sort_spinner);
        SwitchMaterial sortSwitch = requireView().findViewById(R.id.sort_switch);
        SwitchMaterial includeLaundrySwitch = requireView().findViewById(R.id.laundry_switch);

        String type = typeSpinner.getSelectedItem().toString();
        String color = colorSpinner.getSelectedItem().toString();
        String pattern = patternSpinner.getSelectedItem().toString();
        String material = materialSpinner.getSelectedItem().toString();
        String origin = originSpinner.getSelectedItem().toString();
        String sortBy = sortBySpinner.getSelectedItem().toString();
        boolean descending = false;
        if(sortSwitch.isChecked()) {
            descending = true;
        }

        boolean includeLaundry = false;
        if(includeLaundrySwitch.isChecked()) {
            includeLaundry = true;
        }


        replaceFragment(ClosetFragment.newInstance(type,color,pattern,material,origin,sortBy,descending,includeLaundry));

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("closetFilter");
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}