package com.example.gaayathri.bookoman;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
    Dialog myDialog2;
    String entryName;
    String downloadUri;

    private FirebaseFirestore firestore;

    BookLoading bookLoading;

    // region Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecommerce);

        myDialog = new Dialog(InstantSearchActivity.this);
        myDialog.setContentView(R.layout.expandeddialog);

        myDialog2 = new Dialog(InstantSearchActivity.this);
        myDialog2.setContentView(R.layout.imageexpanded);

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

                Toast.makeText(InstantSearchActivity.this, entryName, Toast.LENGTH_SHORT).show();

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

                                Toast.makeText(InstantSearchActivity.this, titleL, Toast.LENGTH_SHORT).show();

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

                                chatSeller.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        launchOneToOneChat(uid, userL);

                                    }
                                });

                                bookpic.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                        Glide.with(InstantSearchActivity.this).load(downloadUriL).apply(options).into(ivExpandedPic);
                                        myDialog2.show();
                                    }
                                });

                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(InstantSearchActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            Toast.makeText(InstantSearchActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mrp.setPaintFlags(mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                checkFavorited();

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
        //filterResultsWindow.dismiss();
        //toggleArrow(buttonFilter, false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //filterResultsWindow.dismiss();
        searcher.destroy();
        //toggleArrow(buttonFilter, false);
        super.onDestroy();
        //RefWatcher refWatcher = EcommerceApplication.getRefWatcher(this);
        //refWatcher.watch(this);
        //refWatcher.watch(findViewById(R.id.hits));
    }
    // endregion

    // region Permission handling
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

    private void checkFavorited() {

        //String email = firebaseAuth.getCurrentUser().getEmail();

       /* String email = "contact.bookoman@gmail.com";

        final LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        DocumentReference docRef = firestore.collection("users").document(email).collection("favorites").document(entryName);
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
        });*/
    }

    @Override
    public void onAnimationEnd(LikeButton likeButton) {

    }

    @Override
    public void liked(LikeButton likeButton) {

    }

    @Override
    public void unLiked(LikeButton likeButton) {

    }

}