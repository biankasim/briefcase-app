package sk.tuke.bakalarka.activities.settings.auth;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import sk.tuke.bakalarka.R;

public class SignInFragment extends Fragment {
    private FirebaseAuth mAuth;
    private EditText email_input;
    private EditText password_input;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(getActivity());
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.google_auth_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sing_in, container, false);

        email_input = view.findViewById(R.id.editTextEmail);
        password_input = view.findViewById(R.id.editTextPassword);

        Button signUpButton = view.findViewById(R.id.btnSignUp);
        Button signInButton = view.findViewById(R.id.btnSignIn);
        signUpButton.setOnClickListener(v -> signUp());
        signInButton.setOnClickListener(v -> signIn());

        Button googleSignInButton = view.findViewById(R.id.btnSignInWithGoogle);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        return view;
    }

    private void signUp() {
        String email = email_input.getText().toString();
        String password = password_input.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Successfully signed up", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn() {
        String email = email_input.getText().toString();
        String password = password_input.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        replaceFragment(new SignOutFragment());
                        Toast.makeText(getActivity(), "Successfully signed in", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(getActivity(), result -> {
                    try {
                        result.getPendingIntent().send();
                    } catch (PendingIntent.CanceledException e) {
                        Toast.makeText(getActivity(), "Google Sign-In was canceled", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Google Sign-In", "Error: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });}

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                            mAuth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener(getActivity(), task -> {
                                        if (task.isSuccessful()) {
                                            replaceFragment(new SignOutFragment());
                                            Toast.makeText(getActivity(), "Successfully signed in", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } catch (ApiException e) {
                        Toast.makeText(getActivity(), "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
