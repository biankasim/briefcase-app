package sk.tuke.bakalarka.activities.fit;

import static sk.tuke.bakalarka.tools.ParseTools.parseColorSeason;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserStylePref;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.tools.DbTools;


public class BodyTypeFragment extends Fragment {
    private String userId;
    private TextView bodyType;
    private TextView bodyTypeStyles;

    public BodyTypeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_body_type, container, false);

        bodyType = view.findViewById(R.id.body_type);
        bodyTypeStyles = view.findViewById(R.id.styles_description);


        if(userId != null) {
            String bodyTypeName = getUserStylePref(requireContext(),"bodyType");
            String bodyTypeStylesString = getUserStylePref(requireContext(),"styles");
            if(bodyTypeName != null) {
                bodyType.setText(String.format("Kibbe: %s", bodyTypeName));
            }
            if(bodyTypeStylesString != null) {
                bodyTypeStyles.setText(String.format("Styles: %s", bodyTypeStylesString));
            }
            if(bodyTypeName == null || bodyTypeStylesString == null) {
                DbTools.getUserPreferences(userId, new DbTools.OnUserPreferencesCallback() {
                    @Override
                    public void onUserPreferencesLoaded(HashMap<String, String> preferences) {
                        String bodyTypeName = preferences.get("bodyType");
                        bodyType.setText(String.format("Kibbe: %s", bodyTypeName));
                        bodyTypeStyles.setText(preferences.get("styles"));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

        return view;
    }
}