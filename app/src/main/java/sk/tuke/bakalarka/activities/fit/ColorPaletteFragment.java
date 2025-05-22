package sk.tuke.bakalarka.activities.fit;

import static sk.tuke.bakalarka.tools.ParseTools.parseColorSeason;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserColorPalette;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserStylePref;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.tools.DbTools;


public class ColorPaletteFragment extends Fragment {
    private String userId;
    private TextView colorSeason;
    private ImageView colorPalette;
    public ColorPaletteFragment() {
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
        View view = inflater.inflate(R.layout.fragment_color_palette, container, false);

        colorSeason = view.findViewById(R.id.color_season);
        colorPalette = view.findViewById(R.id.color_palette);

        if(userId != null) {
            String colorSeasonName = getUserStylePref(requireContext(),"colorPalette");
            if(colorSeasonName != null) {
                colorSeason.setText(String.format("Color Season: %s", colorSeasonName));
                Glide
                        .with(requireContext())
                        .load("")
                        .placeholder(getImage(parseColorSeason(colorSeasonName)))
                        .into(colorPalette);
                return view;
            }


            DbTools.getUserPreferences(userId, new DbTools.OnUserPreferencesCallback() {
                @Override
                public void onUserPreferencesLoaded(HashMap<String, String> preferences) {
                    String colorSeasonName = preferences.get("colorSeason");
                    colorSeason.setText(String.format("Color Season: %s", colorSeasonName));
                    Glide
                            .with(requireContext())
                            .load("")
                            .placeholder(getImage(parseColorSeason(colorSeasonName)))
                            .into(colorPalette);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }


        return view;
    }

    private int getImage(String imageName) {
        int drawableResourceId = requireContext().getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
        return drawableResourceId;
    }

}