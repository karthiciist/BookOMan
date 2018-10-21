package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.victor.loading.book.BookLoading;

import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.auth.FirebaseAuth.*;


public class CatagoriesExpandedFragment extends Fragment implements OnLikeListener, OnAnimationEndListener {

    private static final String TAG = "EntireLibraryFragment";

    private EmptyRecyclerView recyclerView;
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

    BookLoading bookLoading;

    String value;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_catagories_expanded, container, false);

        value = getArguments().getString("category");

        setRetainInstance(true);

        // Initiating dialog windows
        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.dialog_expanded);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.dialog_image_expanded);

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
                Toast.makeText(getActivity(), "Calling seller!!!", Toast.LENGTH_SHORT).show();
            }
        });

        btnChatSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Chatting seller!!!", Toast.LENGTH_SHORT).show();
            }
        });

        progressDialog = new ProgressDialog(getActivity());


        // Initialize firebase
        firebaseAuth = getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String name = user.getEmail();
        firestoreDB = FirebaseFirestore.getInstance();


        // FAB setup
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClicked();
            }
        });


        // Setting up recycler view
        recyclerView = view.findViewById(R.id.rvNoteList);

        LinearLayout tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView.setEmptyView(tvEmpty);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        loadNotesList(value);

        String question = null;
        String degree = null;

        if (value.equals("All Architecture books")) {
            question = "Architecture";
            degree = "degree";
        } else if ((value.equals("All Arts Books"))) {
            question = "Arts";
            degree = "degree";
        } else if ((value.equals("All Commerce Books"))) {
            question = "Commerce";
            degree = "degree";
        } else if ((value.equals("All Computer Applications Books"))) {
            question = "Computer Applications";
            degree = "degree";
        } else if ((value.equals("All Education Books"))) {
            question = "Education";
            degree = "degree";
        } else if ((value.equals("All Engineering books"))) {
            question = "Engineering";
            degree = "degree";
        } else if ((value.equals("All Law Books"))) {
            question = "Law";
            degree = "degree";
        } else if ((value.equals("All Literature books"))) {
            question = "Literature";
            degree = "degree";
        } else if ((value.equals("All Medicine Books"))) {
            question = "Medicine";
            degree = "degree";
        } else if ((value.equals("All Science Books"))) {
            question = "Science";
            degree = "degree";
        } else if ((value.equals("All Other Books"))) {
            question = "Others";
            degree = "degree";
        } else if ((value.equals("Landscape Architecture"))) {
            question = "Landscape Architecture";
            degree = "specialization";
        } else if ((value.equals("Urban Planner"))) {
            question = "Urban Planner";
            degree = "specialization";
        } else if ((value.equals("Restoration Architecture"))) {
            question = "Restoration Architecture";
            degree = "specialization";
        } else if ((value.equals("Research Architecture"))) {
            question = "Research Architecture";
            degree = "specialization";
        } else if ((value.equals("Lighting Architecture"))) {
            question = "Lighting Architecture";
            degree = "specialization";
        } else if ((value.equals("Political Architecture"))) {
            question = "Political Architecture";
            degree = "specialization";
        } else if ((value.equals("CSE"))) {
            question = "CSE";
            degree = "specialization";
        } else if ((value.equals("ECE"))) {
            question = "ECE";
            degree = "specialization";
        } else if ((value.equals("EEE"))) {
            question = "EEE";
            degree = "specialization";
        } else if ((value.equals("E&I"))) {
            question = "E&I";
            degree = "specialization";
        } else if ((value.equals("Information Technology"))) {
            question = "Information Technology";
            degree = "specialization";
        } else if ((value.equals("ICE"))) {
            question = "ICE";
            degree = "specialization";
        } else if ((value.equals("Mechanical Engineering"))) {
            question = "Mechanical Engineering";
            degree = "specialization";
        } else if ((value.equals("Mechatronics"))) {
            question = "Mechatronics";
            degree = "specialization";
        } else if ((value.equals("Production Engineering"))) {
            question = "Production Engineering";
            degree = "specialization";
        } else if ((value.equals("English"))) {
            question = "English";
            degree = "specialization";
        } else if ((value.equals("Tamil"))) {
            question = "Tamil";
            degree = "specialization";
        } else if ((value.equals("Telugu"))) {
            question = "Telugu";
            degree = "specialization";
        } else if ((value.equals("Malayalam"))) {
            question = "Malayalam";
            degree = "specialization";
        } else if ((value.equals("Others"))) {
            question = "Others";
            degree = "specialization";
        } else if ((value.equals("General Medicine"))) {
            question = "General Medicine";
            degree = "specialization";
        } else if ((value.equals("Dental"))) {
            question = "Dental";
            degree = "specialization";
        } else if ((value.equals("Pharmacy"))) {
            question = "Pharmacy";
            degree = "specialization";
        } else if ((value.equals("Nursing"))) {
            question = "Nursing";
            degree = "specialization";
        } else if ((value.equals("Siddha & Ayurvedha"))) {
            question = "Siddha & Ayurvedha";
            degree = "specialization";
        } else if ((value.equals("Physics"))) {
            question = "Physics";
            degree = "specialization";
        } else if ((value.equals("Chemistry"))) {
            question = "Chemistry";
            degree = "specialization";
        } else if ((value.equals("Mathematics"))) {
            question = "Mathematics";
            degree = "specialization";
        } else if ((value.equals("Life Science"))) {
            question = "Life Science";
            degree = "specialization";
        }

        firestoreListener = firestoreDB.collection("books").whereEqualTo(degree, question)
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



        return view;
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
                Toast.makeText(getActivity(), "Added to favorites!!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                    }
                });

        Map<String, Object> data = new HashMap<>();
        data.put(uuid, false);

        firestoreDB.collection("books").document(entryName)
                .set(data, SetOptions.merge());
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        progressDialog.dismiss();

        value = "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        firestoreListener.remove();
        value = "";
    }

    private void fabClicked() {

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");

        Intent intent = new Intent(getActivity(), SellTestActivity.class);
        startActivity(intent);

    }

    private void loadNotesList(String value) {

        String question = null;
        String degree = null;

        if (value.equals("All Architecture books")) {
            question = "Architecture";
            degree = "degree";
        } else if ((value.equals("All Arts Books"))) {
            question = "Arts";
            degree = "degree";
        } else if ((value.equals("All Commerce Books"))) {
            question = "Commerce";
            degree = "degree";
        } else if ((value.equals("All Computer Applications Books"))) {
            question = "Computer Applications";
            degree = "degree";
        } else if ((value.equals("All Education Books"))) {
            question = "Education";
            degree = "degree";
        } else if ((value.equals("All Engineering books"))) {
            question = "Engineering";
            degree = "degree";
        } else if ((value.equals("All Law Books"))) {
            question = "Law";
            degree = "degree";
        } else if ((value.equals("All Literature books"))) {
            question = "Literature";
            degree = "degree";
        } else if ((value.equals("All Medicine Books"))) {
            question = "Medicine";
            degree = "degree";
        } else if ((value.equals("All Science Books"))) {
            question = "Science";
            degree = "degree";
        } else if ((value.equals("All Other Books"))) {
            question = "Others";
            degree = "degree";
        } else if ((value.equals("Landscape Architecture"))) {
            question = "Landscape Architecture";
            degree = "specialization";
        } else if ((value.equals("Urban Planner"))) {
            question = "Urban Planner";
            degree = "specialization";
        } else if ((value.equals("Restoration Architecture"))) {
            question = "Restoration Architecture";
            degree = "specialization";
        } else if ((value.equals("Research Architecture"))) {
            question = "Research Architecture";
            degree = "specialization";
        } else if ((value.equals("Lighting Architecture"))) {
            question = "Lighting Architecture";
            degree = "specialization";
        } else if ((value.equals("Political Architecture"))) {
            question = "Political Architecture";
            degree = "specialization";
        } else if ((value.equals("CSE"))) {
            question = "CSE";
            degree = "specialization";
        } else if ((value.equals("ECE"))) {
            question = "ECE";
            degree = "specialization";
        } else if ((value.equals("EEE"))) {
            question = "EEE";
            degree = "specialization";
        } else if ((value.equals("E&I"))) {
            question = "E&I";
            degree = "specialization";
        } else if ((value.equals("Information Technology"))) {
            question = "Information Technology";
            degree = "specialization";
        } else if ((value.equals("ICE"))) {
            question = "ICE";
            degree = "specialization";
        } else if ((value.equals("Mechanical Engineering"))) {
            question = "Mechanical Engineering";
            degree = "specialization";
        } else if ((value.equals("Mechatronics"))) {
            question = "Mechatronics";
            degree = "specialization";
        } else if ((value.equals("Production Engineering"))) {
            question = "Production Engineering";
            degree = "specialization";
        } else if ((value.equals("English"))) {
            question = "English";
            degree = "specialization";
        } else if ((value.equals("Tamil"))) {
            question = "Tamil";
            degree = "specialization";
        } else if ((value.equals("Telugu"))) {
            question = "Telugu";
            degree = "specialization";
        } else if ((value.equals("Malayalam"))) {
            question = "Malayalam";
            degree = "specialization";
        } else if ((value.equals("Others"))) {
            question = "Others";
            degree = "specialization";
        } else if ((value.equals("General Medicine"))) {
            question = "General Medicine";
            degree = "specialization";
        } else if ((value.equals("Dental"))) {
            question = "Dental";
            degree = "specialization";
        } else if ((value.equals("Pharmacy"))) {
            question = "Pharmacy";
            degree = "specialization";
        } else if ((value.equals("Nursing"))) {
            question = "Nursing";
            degree = "specialization";
        } else if ((value.equals("Siddha & Ayurvedha"))) {
            question = "Siddha & Ayurvedha";
            degree = "specialization";
        } else if ((value.equals("Physics"))) {
            question = "Physics";
            degree = "specialization";
        } else if ((value.equals("Chemistry"))) {
            question = "Chemistry";
            degree = "specialization";
        } else if ((value.equals("Mathematics"))) {
            question = "Mathematics";
            degree = "specialization";
        } else if ((value.equals("Life Science"))) {
            question = "Life Science";
            degree = "specialization";
        }

        Query query = firestoreDB.collection("books").whereEqualTo(degree, question);

        Toast.makeText(getActivity(), question, Toast.LENGTH_SHORT).show();

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

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(note.getDownloadUri()).apply(options).into(holder.bookpic);

                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        progressDialog.setMessage("Loading...");
                        progressDialog.show();

                        bookLoading = myDialog.findViewById(R.id.bookloading);
                        bookLoading.start();

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
                        Button callSeller = myDialog.findViewById(R.id.btnCallSeller);

                        callSeller.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                dialIntent.setData(Uri.parse("tel:" + note.getPhone()));
                                startActivity(dialIntent);
                            }
                        });

                        chatSeller.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                launchOneToOneChat(uid, note.getuser());

                            }
                        });

                        Glide.with(getActivity()).load(note.getDownloadUri()).apply(options).into(bookpic);

                        entryName = note.getEntryName();
                        downloadUri = note.getDownloadUri();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        checkFavorited();

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(note.getDownloadUri()).apply(options).into(ivExpandedPic);
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

        IChatUser iChatUserRecepient = new ChatUser(uid, name);

        ChatUI.getInstance().openConversationMessagesActivity(iChatUserRecepient);

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

}
