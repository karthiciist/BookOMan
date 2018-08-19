package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gaayathri.bookoman.model.Note;
import com.example.gaayathri.bookoman.viewholder.NoteViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.like.LikeButton;
import com.like.OnAnimationEndListener;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntireLibraryActivity extends AppCompatActivity implements OnLikeListener, OnAnimationEndListener {

    private static final String TAG = "SecondActivity";

    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private ListenerRegistration firestoreListener;
    private List<Note> notesList;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;

    Dialog myDialog;
    Dialog myDialog2;
    String entryName;
    String downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entire_library);

        // Initiating dialog windows
        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.expandeddialog);

        myDialog2 = new Dialog(this);
        myDialog2.setContentView(R.layout.imageexpanded);


        // Initiating views in dialogs
        TextView userName = myDialog.findViewById(R.id.userExp);

        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        Button btnCallSeller = myDialog.findViewById(R.id.btnCallSeller);
        Button btnChatSeller = myDialog.findViewById(R.id.btnChatSeller);

        btnCallSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EntireLibraryActivity.this, "Calling seller!!!", Toast.LENGTH_SHORT).show();
            }
        });

        btnChatSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EntireLibraryActivity.this, "Chatting seller!!!", Toast.LENGTH_SHORT).show();
            }
        });

        progressDialog = new ProgressDialog(this);


        // Initialize firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String name = user.getEmail();
        firestoreDB = FirebaseFirestore.getInstance();


        // FAB setup
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClicked();
            }
        });


        // Setting up recycler view
        recyclerView = findViewById(R.id.rvNoteList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        loadNotesList();

        firestoreListener = firestoreDB.collection("books").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Listen failed!", e);
                            return;
                        }

                        notesList = new ArrayList<>();

                        for (DocumentSnapshot doc : documentSnapshots) {
                            Note note = doc.toObject(Note.class);
                            note.setId(doc.getId());
                            notesList.add(note);
                        }

                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

    }

    private void fabClicked() {

        //Intent intent = new Intent(EntireLibraryActivity.this, SellActivity.class);
        //startActivity(intent);

    }

    private void loadNotesList() {

        Query query = firestoreDB.collection("books").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(response) {
            @Override
            protected void onBindViewHolder(NoteViewHolder holder, int position, Note model) {
                final Note note = notesList.get(position);

                holder.title.setText(note.getTitle());
                holder.author.setText(note.getAuthor());
                holder.degree.setText(note.getDegree());
                holder.specialization.setText(note.getSpecialization());
                holder.mrp.setText(note.getMrp());
                holder.price.setText(note.getPrice());
                holder.location.setText(note.getLocation());

                Picasso.with(EntireLibraryActivity.this).load(note.getDownloadUri()).fit().centerCrop().into(holder.bookpic);


                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        progressDialog.setMessage("Loading your favorite material...");
                        progressDialog.show();

                        TextView titleExp = myDialog.findViewById(R.id.titleExp);
                        TextView authExp = myDialog.findViewById(R.id.authorExp);
                        TextView degreeExp = myDialog.findViewById(R.id.degreeExp);
                        TextView specialExp = myDialog.findViewById(R.id.specialExp);
                        TextView locationExp = myDialog.findViewById(R.id.locationExp);
                        TextView mrpExp = myDialog.findViewById(R.id.mrpPriceExp);
                        TextView priceExp = myDialog.findViewById(R.id.priceExp);
                        TextView userExp = myDialog.findViewById(R.id.userExp);
                        TextView sellerMsgExp = myDialog.findViewById(R.id.sellerMsgExp);
                        ImageView bookpic = myDialog.findViewById(R.id.ivBookPic);

                        titleExp.setText(note.getTitle());
                        authExp.setText(note.getAuthor());
                        degreeExp.setText(note.getDegree());
                        specialExp.setText(note.getSpecialization());
                        locationExp.setText(note.getLocation());
                        mrpExp.setText(note.getMrp());
                        priceExp.setText(note.getPrice());
                        userExp.setText(note.getuser());
                        sellerMsgExp.setText(note.getSellerMsg());

                        final String uid = note.getUid().toString();

                        Button chatSeller = myDialog.findViewById(R.id.btnChatSeller);

                        chatSeller.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                launchOneToOneChat(uid, "Bommi");

                            }
                        });

                        Picasso.with(EntireLibraryActivity.this).load(note.getDownloadUri()).fit().centerCrop().into(bookpic);

                        entryName = note.getEntryName();
                        downloadUri = note.getDownloadUri();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        checkFavorited();

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Picasso.with(EntireLibraryActivity.this).load(note.getDownloadUri()).fit().into(ivExpandedPic);
                                myDialog2.show();
                            }
                        });

                        ImageView tvClose = myDialog2.findViewById(R.id.tvClose);

                        tvClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDialog2.dismiss();
                            }
                        });

                        progressDialog.dismiss();

                    }
                });
            }

            @Override
            public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_note, parent, false);

                return new NoteViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void launchOneToOneChat(String uid, String name){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        IChatUser iChatUser = new ChatUser(user.getUid(), user.getDisplayName());

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.google_app_id))
                        .firebaseUrl("https://login-demo-3c273.firebaseio.com/")
                        .storageBucket("gs://login-demo-3c273.appspot.com")
                        .build();

        ChatManager.start(this, mChatConfiguration, iChatUser);

        ChatUI.getInstance().setContext(this);

        ChatUI.getInstance().openConversationMessagesActivity(uid, name);

    }

    private void checkFavorited() {

        String email = firebaseAuth.getCurrentUser().getEmail();

        final LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        DocumentReference docRef = firestoreDB.collection("users").document(email).collection("favorites").document(entryName);
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

        Log.d("ExpandedView", "Animation End for %s" + likeButton);

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
        ImageView bookpic = myDialog.findViewById(R.id.ivBookPic);



        final Map<String, String> bookMap = new HashMap<>();

        bookMap.put("liked", entryName);
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

        firestoreDB.collection("users").document(email).collection("favorites").document(entryName).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EntireLibraryActivity.this, "Added to favorites!!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EntireLibraryActivity.this, "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put(uuid, true);

        firestoreDB.collection("books").document(entryName)
                .set(data, SetOptions.merge());


    }

    @Override
    public void unLiked(LikeButton likeButton) {

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uuid = firebaseAuth.getCurrentUser().getUid();

        firestoreDB.collection("users").document(email).collection("favorites").document(entryName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EntireLibraryActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EntireLibraryActivity.this, "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                    }
                });

        Map<String, Object> data = new HashMap<>();
        data.put(uuid, false);

        firestoreDB.collection("books").document(entryName)
                .set(data, SetOptions.merge());

    }
}
