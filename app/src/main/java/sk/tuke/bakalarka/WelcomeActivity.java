package sk.tuke.bakalarka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        intent = new Intent(WelcomeActivity.this, MainActivity.class);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {//new user
            setContentView(R.layout.activity_welcome);
            Button signInBtn = findViewById(R.id.sign_in_btn);
            signInBtn.setOnClickListener(v -> startActivity(intent));
        }else{//signed in user
            startActivity(intent);
            finish();
        }
    }

}