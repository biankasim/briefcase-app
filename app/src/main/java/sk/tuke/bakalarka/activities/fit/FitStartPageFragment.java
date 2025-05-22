package sk.tuke.bakalarka.activities.fit;

import static sk.tuke.bakalarka.tools.ResourcesTools.getUserColorPalette;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserStylePref;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sk.tuke.bakalarka.R;

public class FitStartPageFragment extends Fragment {
    private String userId;

    public FitStartPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fit_start_page, container, false);

        AppCompatButton findSimilarsBtn = view.findViewById(R.id.find_similars_btn);
        AppCompatButton myColorsBtn = view.findViewById(R.id.my_colors_btn);
        AppCompatButton myBodyTypeBtn = view.findViewById(R.id.my_body_type_btn);

        findSimilarsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userId == null) {
                    Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                    return;
                }
                //replaceFragment(new FitClothingItemUploadFragment());
                Toast.makeText(getActivity(),"Feature disabled",Toast.LENGTH_SHORT).show();
            }
        });

        myColorsBtn.setOnClickListener(v -> {
            if(userId == null) {
                Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                return;
            }
            if(getUserColorPalette(requireContext()) == null) {
                Toast.makeText(getActivity(),"You did not set your color palette",Toast.LENGTH_SHORT).show();
                return;
            }
            replaceFragment(new ColorPaletteFragment());
        });

        myBodyTypeBtn.setOnClickListener(v -> {
            if(userId == null) {
                Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                return;
            }
            if(getUserStylePref(requireContext(),"bodyType") == null && getUserStylePref(requireContext(),"styles") == null) {
                Toast.makeText(getActivity(),"You did not set your styles",Toast.LENGTH_SHORT).show();
                return;
            }
            replaceFragment(new BodyTypeFragment());
        });


        return view;
    }





    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.addToBackStack("fitStartPage");
        fragmentTransaction.commit();
    }
}