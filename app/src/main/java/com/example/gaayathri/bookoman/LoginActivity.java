package com.example.gaayathri.bookoman;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.ChatUtils;
import org.chat21.android.utils.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ss.com.bannerslider.Slider;

import static org.chat21.android.core.ChatManager.Configuration.appId;

public class LoginActivity extends AppCompatActivity {

    private Button googleSignupbutton;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;

    private Slider slider;

    private static final int RC_SIGN_IN = 234;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        Slider.init(new GlideImageLoadingService(this));

        slider = findViewById(R.id.banner_slider_entry);
        slider.setAdapter(new MainSliderAdapter());

        firestore = FirebaseFirestore.getInstance();

        googleSignupbutton = findViewById(R.id.btnGoogleSignin);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        if (user != null){
            finish();
            Toast.makeText(LoginActivity.this, "Logged in as " + user.getEmail().toString(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage() + "1", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GoogleSignIn", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String email = user.getEmail();
                            Toast.makeText(LoginActivity.this, email, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);

                            String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            String user_city = "Chennai";

                            Map<String, String> userMap = new HashMap<>();

                            userMap.put("name", user_name);
                            userMap.put("email", user_email );
                            userMap.put("city", user_city);

                            firestore.collection("users").document(user_email).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    launchChat();

                                    Toast.makeText(LoginActivity.this, "User profile created successfully!!!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error = e.getMessage();
                                    Toast.makeText(LoginActivity.this, "Cannot create user profile. Error:" + error, Toast.LENGTH_SHORT).show();

                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed!!!", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void launchChat() {


        String token = FirebaseInstanceId.getInstance().getToken();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference root;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            root = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl);
        } else {
            root = FirebaseDatabase.getInstance().getReference();
        }

        DatabaseReference firebaseUsersPath = root
                .child("apps/" + "1:737796002286:android:c2960ecbf1cbd247" +
                        "/users/" + firebaseUser.getUid() + "/instances/" + token);

        Map<String, Object> device = new HashMap<>();
        device.put("device_model", ChatUtils.getDeviceModel());
        device.put("platform", "Android");
        device.put("platform_version", ChatUtils.getSystemVersion());
        device.put("language", ChatUtils.getSystemLanguage(getResources()));

        firebaseUsersPath.setValue(device);


        // validate email
        final String email = firebaseUser.getEmail();

        // validate first name
        final String firstName = firebaseUser.getDisplayName();


        // validate last name
        final String lastName = " ";

        final String userUID = firebaseUser.getUid();

        final String imageUrl = firebaseUser.getPhotoUrl().toString();


        // perform the user creation
        DatabaseReference firebaseContactsPath = root
                .child("apps/" + "1:737796002286:android:c2960ecbf1cbd247" +
                        "/contacts/" + firebaseUser.getUid());

        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("firstname", firstName);
        userMap.put("imageurl", imageUrl);
        userMap.put("lastname", lastName);
        userMap.put("timestamp", new Date().getTime());
        userMap.put("uid", userUID);

        firebaseContactsPath.setValue(userMap);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        IChatUser iChatUser = new ChatUser(user.getUid(), user.getDisplayName());

        ChatManager.start(LoginActivity.this, appId, iChatUser);

    }
}
