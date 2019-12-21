package com.kenansoylu.socialmap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.kenansoylu.socialmap.R;
import com.kenansoylu.socialmap.data.UserData;
import com.kenansoylu.socialmap.services.DBService;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = null;

    Button signOutButton;
    Button settingsButton;
    Button mapButton;
    TextView nameTxt;

    private final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signOutButton = findViewById(R.id.signOutBtn);
        settingsButton = findViewById(R.id.settingsBtn);
        mapButton = findViewById(R.id.mapBtn);
        nameTxt = findViewById(R.id.nameTxt);

        if(this.auth != null) {
            user = auth.getCurrentUser();
            if(user != null) {
                // If signed in
                nameTxt.setVisibility(View.VISIBLE);
                nameTxt.setText(user.getDisplayName());
                mapButton.setText("Map");
                settingsButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.VISIBLE);
            } else {
                mapButton.setText("Sign In");
            }
        }

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user == null) {
                    signIn();
                } else {
                    Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                    mapIntent.putExtra("user_id", user.getUid());
                    MainActivity.this.startActivity(mapIntent);
                }
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(settingsIntent);
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auth != null) {
                    auth.signOut();
                    user = null;
                    nameTxt.setVisibility(View.GONE);
                    nameTxt.setText("");
                    mapButton.setText("Sign In");
                    settingsButton.setVisibility(View.GONE);
                    signOutButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null) {
                    final UserData userData = new UserData(user.getUid(), user.getDisplayName());
                    new DBService().addUser(userData, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            mapButton.setText("Map");
                            nameTxt.setVisibility(View.VISIBLE);
                            nameTxt.setText(userData.getName());
                            settingsButton.setVisibility(View.VISIBLE);
                            signOutButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } else {
                // Sign in failed.
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
