package sk.tuke.bakalarka.activities.settings;

import static sk.tuke.bakalarka.tools.DbTools.setUserPreferences;
import static sk.tuke.bakalarka.tools.ResourcesTools.getResourcesValueFromPosition;
import static sk.tuke.bakalarka.tools.ParseTools.parseColorSeason;
import static sk.tuke.bakalarka.tools.ResourcesTools.setResourcesArrayPosition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import sk.tuke.bakalarka.R;

public class SetUserPreferencesFragment extends Fragment {
    private static final String ARG_COLOR_SEASON = "colorSeason";
    private static final String ARG_BODY_TYPE = "bodyType";
    private static final String ARG_STYLES = "styles";

    private String mColorSeason;
    private String mBodyType;
    private String mStyles;
    public SetUserPreferencesFragment() {
        // Required empty public constructor
    }

    public static SetUserPreferencesFragment newInstance(String colorSeason, String bodyType, String styles) {
        SetUserPreferencesFragment fragment = new SetUserPreferencesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLOR_SEASON, colorSeason);
        args.putString(ARG_BODY_TYPE, bodyType);
        args.putString(ARG_STYLES, styles);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColorSeason = getArguments().getString(ARG_COLOR_SEASON);
            mBodyType = getArguments().getString(ARG_BODY_TYPE);
            mStyles = getArguments().getString(ARG_STYLES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_user_preferences, container, false);

        //set spinner position
        Spinner colorSeasonSpinner = view.findViewById(R.id.color_season_spinner);
        Spinner bodyTypeSpinner = view.findViewById(R.id.body_type_spinner);
        ImageView colorSeasonImageView = view.findViewById(R.id.color_season_image_view);
        EditText editText = view.findViewById(R.id.body_type_styles);


        String[] colorSeasons = getResources().getStringArray(R.array.color_season_types);
        String[] bodyTypes = getResources().getStringArray(R.array.kibbe_body_types);

        setResourcesArrayPosition(colorSeasons,colorSeasonSpinner,mColorSeason);
        setResourcesArrayPosition(bodyTypes,bodyTypeSpinner,mBodyType);
        if(!mStyles.equalsIgnoreCase("null")) {
            editText.setText(mStyles);
        }


        colorSeasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mColorSeason = getResourcesValueFromPosition(colorSeasons, position);
                Glide
                        .with(requireContext())
                        .load("")
                        .placeholder(getImage(parseColorSeason(mColorSeason)))
                        .into(colorSeasonImageView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mColorSeason = null;
            }
        });

        bodyTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBodyType = getResourcesValueFromPosition(bodyTypes, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBodyType = null;
            }
        });


        AppCompatButton doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStyles = String.valueOf(editText.getText());
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                setUserPreferences(userId,mColorSeason,mBodyType, mStyles);
                saveUserPreferences();
                replaceFragment(new SettingsFragment());
            }
        });


        return view;
    }
    private int getImage(String imageName) {
        int drawableResourceId = requireContext().getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
        return drawableResourceId;
    }

    private void saveUserPreferences(){
        //save into shared preferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(!mColorSeason.equalsIgnoreCase("-")) {
            editor.putString("colorPalette", mColorSeason);
        }
        if(!mBodyType.equalsIgnoreCase("-")) {
            editor.putString("bodyType", mBodyType);
        }
        if(!mStyles.isEmpty()) {
            editor.putString("styles",mStyles);
        }

        editor.apply();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}