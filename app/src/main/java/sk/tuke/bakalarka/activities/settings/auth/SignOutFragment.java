package sk.tuke.bakalarka.activities.settings.auth;

import static sk.tuke.bakalarka.tools.DbTools.getUserPreferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.settings.SetUserPreferencesFragment;
import sk.tuke.bakalarka.tools.DbTools;


public class SignOutFragment extends Fragment {
    private FirebaseAuth mAuth;


    public SignOutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_out, container, false);

        AppCompatButton signOutButton = view.findViewById(R.id.btnSignOut);
        signOutButton.setOnClickListener(v -> signOut());

        AppCompatButton changePrefsButton = view.findViewById(R.id.change_prefs_btn);
        changePrefsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get user preferences and pass them to next fragment
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                getUserPreferences(userId, new DbTools.OnUserPreferencesCallback() {
                    @Override
                    public void onUserPreferencesLoaded(HashMap<String, String> preferences) {
                        replaceFragment(SetUserPreferencesFragment.newInstance(preferences.get("colorSeason"), preferences.get("bodyType"), preferences.get("styles")));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        return view;
    }
    private void signOut() {
        mAuth.signOut();
        replaceFragment(new SignInFragment());
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("signOut");
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}