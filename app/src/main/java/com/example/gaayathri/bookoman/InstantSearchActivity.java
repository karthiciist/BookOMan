package com.example.gaayathri.bookoman;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.instantsearch.ui.views.SearchBox;
import com.algolia.instantsearch.utils.ItemClickSupport;
import com.algolia.instantsearch.voice.PermissionDialogFragment;
import com.algolia.instantsearch.voice.VoiceDialogFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.like.LikeButton;
import com.like.OnAnimationEndListener;
import com.like.OnLikeListener;
import com.victor.loading.book.BookLoading;

import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.algolia.instantsearch.voice.PermissionDialogFragment.ID_REQ_VOICE_PERM;


public class InstantSearchActivity extends AppCompatActivity implements VoiceDialogFragment.VoiceResultsListener, OnLikeListener, OnAnimationEndListener {
    private static final String ALGOLIA_APP_ID = "H9P3XBA9GD";
    private static final String ALGOLIA_INDEX_NAME = "books";
    private static final String ALGOLIA_API_KEY = "3058fee363b2c4b8afe53e9d9eab642f";

    //private FilterResultsWindow filterResultsWindow;
    private Drawable arrowDown;
    private Drawable arrowUp;
    private Button buttonFilter;
    private Searcher searcher;

    private FirebaseAuth firebaseAuth;

    Dialog myDialog;
    Dialog myDialog2, chatIntroDialog;
    String entryName1;
    String downloadUri;

    private FirebaseFirestore firestore;
    private PrefManager prefManager;
    private ProgressDialog progressDialog1;

    BookLoading bookLoading;

    // region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);

        myDialog = new Dialog(InstantSearchActivity.this);
        myDialog.setContentView(R.layout.dialog_expanded);

        myDialog2 = new Dialog(InstantSearchActivity.this);
        myDialog2.setContentView(R.layout.dialog_image_expanded);

        progressDialog1 = new ProgressDialog(InstantSearchActivity.this);
        progressDialog1.setMessage("Loading...");

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_API_KEY, ALGOLIA_INDEX_NAME);
        new InstantSearch(this, searcher); // Initialize InstantSearch in this activity with searcher
        searcher.search(getIntent()); // Show results for empty query (on app launch) / voice query (from intent)

        ((SearchBox) findViewById(R.id.searchBox)).disableFullScreen(); // disable fullscreen input UI on landscape

        final Hits hits = findViewById(R.id.hits);

        hits.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            public static final String TAG = "InstantSearchActivity";

            @Override
            public void onItemClick(RecyclerView recyclerView, int position, View v) {

                JSONObject hit = hits.get(position);

                String entryName = null;

                try {
                    entryName = hit.getString("entryName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Toast.makeText(InstantSearchActivity.this, entryName, Toast.LENGTH_SHORT).show();

                bookLoading = myDialog.findViewById(R.id.bookloading);
                bookLoading.start();

                final TextView title = myDialog.findViewById(R.id.titleExp);
                final TextView author = myDialog.findViewById(R.id.authorExp);
                final TextView degree = myDialog.findViewById(R.id.degreeExp);
                final TextView special = myDialog.findViewById(R.id.specialExp);
                final TextView location = myDialog.findViewById(R.id.locationExp);
                final TextView mrp = myDialog.findViewById(R.id.mrpPriceExp);
                final TextView price = myDialog.findViewById(R.id.priceExp);
                final TextView user = myDialog.findViewById(R.id.userExp);
                final TextView sellerMsg = myDialog.findViewById(R.id.sellerMsgExp);
                final ImageView bookpic = myDialog.findViewById(R.id.ivBookPic);

                DocumentReference docRef = firestore.collection("books").document(entryName);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                String titleL = document.getString("title");
                                String authorL = document.getString("author");
                                String degreeL = document.getString("degree");
                                String specialL = document.getString("specialization");
                                String locationL = document.getString("location");
                                String mrpL = document.getString("mrp");
                                String priceL = document.getString("price");
                                final String userL = document.getString("user");
                                String sellerMsgL = document.getString("sellerMsg");
                                final String downloadUriL = document.getString("downloadUri");
                                final String uid = document.getString("uid");
                                final String phoneL = document.getString("phone");
                                final String emailL = document.getString("email");
                                entryName1 = document.getString("entryName");

                                //Toast.makeText(InstantSearchActivity.this, titleL, Toast.LENGTH_SHORT).show();

                                title.setText(titleL);
                                author.setText(authorL);
                                degree.setText(degreeL);
                                special.setText(specialL);
                                location.setText(locationL);
                                mrp.setText(mrpL);
                                price.setText(priceL);
                                sellerMsg.setText(sellerMsgL);
                                user.setText(userL);

                                final RequestOptions options = new RequestOptions();
                                options.centerCrop();

                                Glide.with(InstantSearchActivity.this).load(downloadUriL).apply(options).into(bookpic);

                                Button chatSeller = myDialog.findViewById(R.id.btnChatSeller);
                                Button callSeller = myDialog.findViewById(R.id.btnCallSeller);

                                //String emailCheck = emailL;
                                String emailCheck1 = firebaseAuth.getCurrentUser().getEmail();

                                if (emailL.equals(emailCheck1)) {

                                    callSeller.setVisibility(View.GONE);
                                    chatSeller.setVisibility(View.GONE);

                                    user.setText("you");

                                } else {

                                    callSeller.setVisibility(View.VISIBLE);
                                    chatSeller.setVisibility(View.VISIBLE);

                                    callSeller.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                            dialIntent.setData(Uri.parse("tel:" + phoneL));
                                            startActivity(dialIntent);
                                        }
                                    });

                                    chatSeller.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            prefManager = new PrefManager(InstantSearchActivity.this);
                                            if (prefManager.isChatFirstTimeLaunch()) {

                                                chatIntroDialog = new Dialog(InstantSearchActivity.this);
                                                chatIntroDialog.setContentView(R.layout.dialog_chat);
                                                chatIntroDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                                                chatIntroDialog.show();

                                                Button btnGotIt = chatIntroDialog.findViewById(R.id.btnGotIt);
                                                btnGotIt.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        progressDialog1.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                                                        progressDialog1.show();

                                                        launchOneToOneChat(uid, userL);
                                                        chatIntroDialog.dismiss();
                                                        prefManager.chatSetFirstTimeLaunch(false);
                                                    }
                                                });

                                            } else {

                                                progressDialog1.show();
                                                launchOneToOneChat(uid, userL);
                                            }

                                        }
                                    });

                                }

                                bookpic.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                        Glide.with(InstantSearchActivity.this).load(downloadUriL).apply(options).into(ivExpandedPic);
                                        myDialog2.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                                        myDialog2.show();
                                    }
                                });

                                checkFavorited(entryName1);

                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(InstantSearchActivity.this, "No such book available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(InstantSearchActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mrp.setPaintFlags(mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                //checkFavorited();

                myDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                myDialog.show();

                ImageView tvClose = myDialog2.findViewById(R.id.tvClose);

                tvClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog2.dismiss();
                    }
                });

            }
        });

        hits.addItemDecoration(new SimpleDividerItemDecoration(InstantSearchActivity.this));

    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        searcher.search(intent);
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(InstantSearchActivity.this, HomeActivity.class));
        super.onBackPressed();  // optional depending on your needs
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressDialog1.dismiss();
    }

    @Override
    protected void onDestroy() {
        searcher.destroy();
        progressDialog1.dismiss();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isRecordPermissionWithResults(requestCode, grantResults)) {
            if (isPermissionGranted(grantResults)) showVoiceOverlay();
            else if (shouldExplainPermission()) showPermissionRationale();
            else showPermissionManualInstructions();
        }
    }

    private void showPermissionRationale() {
        Snackbar.make(findViewById(R.id.mic), R.string.voice_search_rationale, Snackbar.LENGTH_LONG)
                .setAction(R.string.voice_search_button_again, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(InstantSearchActivity.this, new String[]{RECORD_AUDIO}, ID_REQ_VOICE_PERM);
                    }
                }).show();

    }

    private void showPermissionManualInstructions() {
        final View micView = findViewById(R.id.mic);
        Snackbar snackbar = Snackbar.make(micView, R.string.voice_search_disabled_rationale, Snackbar.LENGTH_LONG)
                .setAction(R.string.voice_search_button_enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(micView, R.string.voice_search_disabled_instructions, Snackbar.LENGTH_SHORT)
                                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .setData(Uri.fromParts("package", getPackageName(), null)));
                                    }
                                }).show();
                    }
                });
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(2);
        snackbar.show();
    }
    // endregion

    // region UI
    private void toggleArrow(Button b, boolean isUp) {
        final Drawable[] currentDrawables = b.getCompoundDrawables();
        b.setCompoundDrawablesWithIntrinsicBounds(currentDrawables[0], currentDrawables[1], getArrowDrawable(isUp), currentDrawables[3]);
    }

    private Drawable getArrowDrawable(boolean isUp) {
        if (isUp) arrowUp = ContextCompat.getDrawable(this, R.drawable.arrow_up_flat);
        else arrowDown = ContextCompat.getDrawable(this, R.drawable.arrow_down_flat);
        return isUp ? arrowUp : arrowDown;
    }

    public void onTapMic(@NonNull View view) {
        if (hasRecordPermission()) {
            showPermissionOverlay();
        } else {
            showVoiceOverlay();
        }
    }

    private void showPermissionOverlay() {
        PermissionDialogFragment frag = new PermissionDialogFragment();
        frag.setTitle("You can use voice search to find products");
        frag.show(getSupportFragmentManager(), "permission");
    }

    private void showVoiceOverlay() {
        VoiceDialogFragment frag = new VoiceDialogFragment();
        frag.setSuggestions("Biochemistry in Chennai", "Thermodynamics in Coimbatore"); //TODO: use query suggestions?
        frag.setVoiceResultsListener(this);
        frag.show(getSupportFragmentManager(), "voice");
    }

    @Override
    public void onVoiceResults(@NonNull ArrayList<String> matches) {
        searcher.search(matches.get(0));
    }
    // endregion

    // region Helpers
    //TODO: Move helpers to Voice lib?
    private boolean isPermissionGranted(int[] grantResult) {
        return grantResult[0] == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isRecordPermissionWithResults(int requestCode, @NonNull int[] grantResults) {
        return requestCode == ID_REQ_VOICE_PERM && grantResults.length > 0;
    }

    private boolean hasRecordPermission() {
        return ContextCompat.checkSelfPermission(getBaseContext(), RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldExplainPermission() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO);
    }

    private void launchOneToOneChat(String uid, String name){

        IChatUser iChatUserRecepient = new ChatUser(uid, name);

        ChatUI.getInstance().openConversationMessagesActivity(iChatUserRecepient);

    }

    private void checkFavorited(String entryName1) {

        firebaseAuth = FirebaseAuth.getInstance();
        String email = firebaseAuth.getCurrentUser().getEmail();

        final LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        DocumentReference docRef = firestore.collection("users").document(email).collection("favorites").document(entryName1);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    likeButton.setLiked(true);

                } else {
                    likeButton.setLiked(false);
                }
            }
        });
    }

    @Override
    public void onAnimationEnd(LikeButton likeButton) {

    }

    @Override
    public void liked(LikeButton likeButton) {

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uuid = firebaseAuth.getCurrentUser().getUid();

        TextView titleExp = myDialog.findViewById(R.id.titleExp);
        TextView authExp = myDialog.findViewById(R.id.authorExp);
        TextView degreeExp = myDialog.findViewById(R.id.degreeExp);
        TextView specialExp = myDialog.findViewById(R.id.specialExp);
        TextView locationExp = myDialog.findViewById(R.id.locationExp);
        TextView mrpExp = myDialog.findViewById(R.id.mrpPriceExp);
        TextView priceExp = myDialog.findViewById(R.id.priceExp);
        TextView userExp = myDialog.findViewById(R.id.userExp);
        TextView sellerMsgExp = myDialog.findViewById(R.id.sellerMsgExp);

        final Map<String, String> bookMap = new HashMap<>();

        bookMap.put("liked", entryName1);
        bookMap.put("title", titleExp.getText().toString());
        bookMap.put("author", authExp.getText().toString());
        bookMap.put("degree", degreeExp.getText().toString());
        bookMap.put("specialization", specialExp.getText().toString());
        bookMap.put("location", locationExp.getText().toString());
        bookMap.put("price", priceExp.getText().toString());
        bookMap.put("mrp", mrpExp.getText().toString());
        bookMap.put("user", userExp.getText().toString());
        bookMap.put("sellerMsg", sellerMsgExp.getText().toString());
        bookMap.put("downloadUri", downloadUri);

        firestore.collection("users").document(email).collection("favorites").document(entryName1).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(InstantSearchActivity.this, "Added to favorites!!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InstantSearchActivity.this, "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put(uuid, true);

        firestore.collection("books").document(entryName1)
                .set(data, SetOptions.merge());
    }

    @Override
    public void unLiked(LikeButton likeButton) {

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uuid = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("users").document(email).collection("favorites").document(entryName1)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InstantSearchActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InstantSearchActivity.this, "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                    }
                });

        Map<String, Object> data = new HashMap<>();
        data.put(uuid, false);

        firestore.collection("books").document(entryName1)
                .set(data, SetOptions.merge());
    }

}