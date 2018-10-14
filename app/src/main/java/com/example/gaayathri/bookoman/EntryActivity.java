package com.example.gaayathri.bookoman;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

import static org.chat21.android.core.ChatManager.Configuration.appId;

public class EntryActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;

    private static final int RC_SIGN_IN = 234;
    GoogleSignInClient mGoogleSignInClient;

    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";

    public static final String name = "nameKey";
    public static final String phone = "phoneKey";
    public static final String background = "backgroundKey";
    public static final String city = "cityKey";
    public static final String profilePicUrl = "profilePicUrlKey";
    public static final String email = "emailKey";

    String userName;
    String userPhone;
    String userBackground;
    String userCity;
    String userProfilePic;

    Button btnGoogleSignin;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        btnGoogleSignin = findViewById(R.id.btnGoogleSignin);

        btnGoogleSignin.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (user == null) {

            btnGoogleSignin.setVisibility(View.VISIBLE);

            btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });

        }

        LogoLauncher logoLauncher = new LogoLauncher();
        logoLauncher.start();

    }

    private class LogoLauncher extends Thread{

        public void run(){
            try{
                sleep(1000);

                sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

                // Firebase Auth
                firebaseAuth = FirebaseAuth.getInstance();

                firestore = FirebaseFirestore.getInstance();

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                sharedPreferences = EntryActivity.this.getSharedPreferences(mypreference, Context.MODE_PRIVATE);

                if((currentUser != null) & (!(sharedPreferences.contains(city)) || !(sharedPreferences.contains(background)))) {

                    populateSharedPrefs();

                } else if ((currentUser != null) & (sharedPreferences.contains(city)) & (sharedPreferences.contains(background))) {

                    Intent homeintent = new Intent(EntryActivity.this, WelcomeActivity.class);
                    startActivity(homeintent);

                    finish();
                }

            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
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
                            Toast.makeText(EntryActivity.this, email, Toast.LENGTH_SHORT).show();

                            String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                            Map<String, String> userMap = new HashMap<>();

                            userMap.put("name", user_name);
                            userMap.put("email", user_email );

                            firestore.collection("users").document(user_email).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    populateSharedPrefs();

                                    Toast.makeText(EntryActivity.this, "User profile created successfully!!!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }


                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    String error = e.getMessage();
                                    Toast.makeText(EntryActivity.this, "Cannot create user profile. Error:" + error, Toast.LENGTH_SHORT).show();

                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                            Toast.makeText(EntryActivity.this, "Authentication failed!!!", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    private void populateSharedPrefs() {

        final String mail = firebaseAuth.getCurrentUser().getEmail();

        DocumentReference docRef = firestore.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public static final String TAG = "SplashScreen";

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //String userCityL;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        userName = document.getString("name");
                        userPhone = document.getString("phone");
                        userBackground = document.getString("background");
                        userCity = document.getString("city");
                        userProfilePic = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
                        String userEmail = firebaseAuth.getCurrentUser().getEmail();

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(name, userName);
                        editor.putString(phone, userPhone);
                        editor.putString(background, userBackground);
                        editor.putString(city, userCity);
                        editor.putString(profilePicUrl, userProfilePic);
                        editor.putString(email, userEmail);
                        editor.commit();

                        Toast.makeText(EntryActivity.this, userCity, Toast.LENGTH_SHORT).show();

                        launchChat();

                        Intent homeintent = new Intent(EntryActivity.this, WelcomeActivity.class);
                        startActivity(homeintent);

                        finish();

                    } else {
                        Log.d(TAG, "No such document");
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
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

        ChatManager.start(EntryActivity.this, appId, iChatUser);

    }

}
