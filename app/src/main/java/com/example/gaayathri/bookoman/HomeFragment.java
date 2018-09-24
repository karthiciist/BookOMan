package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gaayathri.bookoman.model.Note;
import com.example.gaayathri.bookoman.viewholder.NoteViewHorizontal;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import ss.com.bannerslider.Slider;

import static com.example.gaayathri.bookoman.EntryActivity.city;


public class HomeFragment extends Fragment implements OnLikeListener, OnAnimationEndListener {

    private static final String TAG = "HomeFragment";
    private FirebaseAuth firebaseAuth;
    private Slider slider;

    private RecyclerView recyclerView, recyclerView1, recyclerView2;
    private ListenerRegistration firestoreListener;
    private ListenerRegistration firestoreListener1;
    private ListenerRegistration firestoreListener2;
    private FirebaseFirestore firestoreDB;
    private List<Note> notesList;
    private List<Note> notesList1;
    private List<Note> notesList2;
    private FirestoreRecyclerAdapter adapter;
    private FirestoreRecyclerAdapter adapter1;
    private FirestoreRecyclerAdapter adapter2;
    Dialog myDialog;
    Dialog myDialog2;
    Dialog myDialog3;
    Dialog myDialog4;

    // For first recycler view
    String UpdateNoteTitle;
    String UpdateNoteAuthor;
    String UpdateNoteDegree;
    String UpdateNoteSpecialization;
    String UpdateNotePrice;
    String UpdateNoteLocation;
    String UpdateNoteUser;
    String UpdateNoteMrp;
    String UpdateNoteSellerMsg;
    String entryName;
    String downloadUri;

    String userNameL;
    String userPhoneL;
    String userDegreeL;
    String userSpecialL;
    String userCityL;
    String userProfilePicL;

    BookLoading bookLoading;

    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";

    public static final String city = "cityKey";
    public static final String degree = "degreeKey";

    private int count;

    private static final String SAVE_COUNT = "save_count";
    public static final String DonotShow = "false";

    private ProgressDialog progressDialog2;

    private FirebaseFirestore firestore;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view;

        view = inflater.inflate(R.layout.fragment_home, container, false);

        setRetainInstance(true);

        sharedpreferences = getActivity().getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        // Slider banner
        Slider.init(new GlideImageLoadingService(getActivity()));
        slider = view.findViewById(R.id.banner_slider_home);
        slider.setAdapter(new MainSliderAdapter());

        recyclerView = view.findViewById(R.id.rvNoteList1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView1 = view.findViewById(R.id.rvNoteList2);
        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView1.setLayoutManager(mLayoutManager1);
        recyclerView1.setItemAnimator(new DefaultItemAnimator());

        recyclerView2 = view.findViewById(R.id.rvNoteList3);
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(mLayoutManager2);
        recyclerView2.setItemAnimator(new DefaultItemAnimator());

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.expandeddialog);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.imageexpanded);

        myDialog3 = new Dialog(getActivity());
        myDialog3.setContentView(R.layout.fillprofiledialog);

        myDialog4 = new Dialog(getActivity());
        myDialog4.setContentView(R.layout.edit_profile_dialog);

        loadNotesList();

        firestoreListener = firestoreDB.collection("books").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notesList = new ArrayList<>();

                        for (DocumentSnapshot doc : documentSnapshots) {
                            Note noteHorizontal = doc.toObject(Note.class);
                            noteHorizontal.setEntryName(doc.getId());
                            notesList.add(noteHorizontal);
                        }

                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }
                });

        if ((sharedpreferences.contains(city)) & (sharedpreferences.contains(degree))) {

            userCityL = sharedpreferences.getString(city, "");
            TextView userCity = view.findViewById(R.id.userCity);
            userCity.setText(userCityL);

            userDegreeL = sharedpreferences.getString(degree, "");
            TextView userDegree = view.findViewById(R.id.userDegree);
            userDegree.setText(userDegreeL);

        } else {

            SharedPreferences sharedpreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

            String showDialog = sharedpreferences.getString(DonotShow, "");

            if (!showDialog.equals("true") ) {

                showFillProfileDialog();

            }

        }

        loadNotesList1();

        firestoreListener1 = firestoreDB.collection("books").whereEqualTo("location", userCityL)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notesList1 = new ArrayList<>();

                        for (DocumentSnapshot doc : documentSnapshots) {
                            Note noteHorizontal = doc.toObject(Note.class);
                            noteHorizontal.setEntryName(doc.getId());
                            notesList1.add(noteHorizontal);
                        }

                        adapter1.notifyDataSetChanged();
                        recyclerView1.setAdapter(adapter1);
                    }
                });

        loadNotesList2();

        firestoreListener2 = firestoreDB.collection("books").whereEqualTo("degree", userDegreeL)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Check your internet connection!!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notesList2 = new ArrayList<>();

                        for (DocumentSnapshot doc : documentSnapshots) {
                            Note noteHorizontal = doc.toObject(Note.class);
                            noteHorizontal.setEntryName(doc.getId());
                            notesList2.add(noteHorizontal);
                        }

                        adapter2.notifyDataSetChanged();
                        recyclerView2.setAdapter(adapter2);
                    }
                });

        return view;

    }

    private void loadNotesList() {

        final Query query = firestoreDB.collection("books").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Note, NoteViewHorizontal>(response) {
            @Override
            protected void onBindViewHolder(final NoteViewHorizontal holder, int position, Note model) {

                final Note noteHorizontal = notesList.get(position);

                holder.title.setText(noteHorizontal.getTitle());
                holder.author.setText(noteHorizontal.getAuthor());

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(holder.bookpic);

                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

                        UpdateNoteTitle = noteHorizontal.getTitle();
                        titleExp.setText(UpdateNoteTitle);

                        UpdateNoteAuthor = noteHorizontal.getAuthor();
                        authExp.setText(UpdateNoteAuthor);

                        UpdateNoteDegree = noteHorizontal.getDegree();
                        degreeExp.setText(UpdateNoteDegree);

                        UpdateNoteSpecialization = noteHorizontal.getSpecialization();
                        specialExp.setText(UpdateNoteSpecialization);

                        UpdateNoteLocation = noteHorizontal.getLocation();
                        locationExp.setText(UpdateNoteLocation);

                        UpdateNoteMrp = noteHorizontal.getMrp();
                        mrpExp.setText(UpdateNoteMrp);

                        UpdateNotePrice = noteHorizontal.getPrice();
                        priceExp.setText(UpdateNotePrice);

                        UpdateNoteSellerMsg = noteHorizontal.getSellerMsg();
                        sellerMsgExp.setText(UpdateNoteSellerMsg);

                        UpdateNoteUser = noteHorizontal.getuser();
                        userExp.setText(UpdateNoteUser);

                        entryName = noteHorizontal.getEntryName();
                        final String uid = noteHorizontal.getUid().toString();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(bookpic);

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(ivExpandedPic);
                                //Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().into(ivExpandedPic);
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

                        Toast.makeText(getActivity(), entryName, Toast.LENGTH_SHORT).show();

                        Button call = myDialog.findViewById(R.id.btnCallSeller);
                        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);

                        Button chat = myDialog.findViewById(R.id.btnChatSeller);

                        chat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                launchOneToOneChat(uid, noteHorizontal.getuser());

                            }
                        });

                        entryName = noteHorizontal.getEntryName();
                        downloadUri = noteHorizontal.getDownloadUri();

                    }
                });



            }

            @Override
            public NoteViewHorizontal onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.horizontal_item, parent, false);

                return new NoteViewHorizontal(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void loadNotesList1() {

        final Query query = firestoreDB.collection("books").whereEqualTo("degree", userDegreeL);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter1 = new FirestoreRecyclerAdapter<Note, NoteViewHorizontal>(response) {
            @Override
            protected void onBindViewHolder(final NoteViewHorizontal holder, int position, Note model) {

                final Note noteHorizontal1 = notesList1.get(position);

                holder.title.setText(noteHorizontal1.getTitle());
                holder.author.setText(noteHorizontal1.getAuthor());

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(noteHorizontal1.getDownloadUri()).apply(options).into(holder.bookpic);

                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

                        UpdateNoteTitle = noteHorizontal1.getTitle();
                        titleExp.setText(UpdateNoteTitle);

                        UpdateNoteAuthor = noteHorizontal1.getAuthor();
                        authExp.setText(UpdateNoteAuthor);

                        UpdateNoteDegree = noteHorizontal1.getDegree();
                        degreeExp.setText(UpdateNoteDegree);

                        UpdateNoteSpecialization = noteHorizontal1.getSpecialization();
                        specialExp.setText(UpdateNoteSpecialization);

                        UpdateNoteLocation = noteHorizontal1.getLocation();
                        locationExp.setText(UpdateNoteLocation);

                        UpdateNoteMrp = noteHorizontal1.getMrp();
                        mrpExp.setText(UpdateNoteMrp);

                        UpdateNotePrice = noteHorizontal1.getPrice();
                        priceExp.setText(UpdateNotePrice);

                        UpdateNoteSellerMsg = noteHorizontal1.getSellerMsg();
                        sellerMsgExp.setText(UpdateNoteSellerMsg);

                        UpdateNoteUser = noteHorizontal1.getuser();
                        userExp.setText(UpdateNoteUser);

                        entryName = noteHorizontal1.getEntryName();
                        final String uid = noteHorizontal1.getUid().toString();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        Glide.with(getActivity()).load(noteHorizontal1.getDownloadUri()).apply(options).into(bookpic);

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(noteHorizontal1.getDownloadUri()).apply(options).into(ivExpandedPic);
                                //Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().into(ivExpandedPic);
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

                        Toast.makeText(getActivity(), entryName, Toast.LENGTH_SHORT).show();

                        Button call = myDialog.findViewById(R.id.btnCallSeller);
                        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);

                        Button chat = myDialog.findViewById(R.id.btnChatSeller);

                        chat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                launchOneToOneChat(uid, noteHorizontal1.getuser());

                            }
                        });

                        entryName = noteHorizontal1.getEntryName();
                        downloadUri = noteHorizontal1.getDownloadUri();

                    }
                });

            }

            @Override
            public NoteViewHorizontal onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.horizontal_item, parent, false);

                return new NoteViewHorizontal(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter1.notifyDataSetChanged();
        recyclerView1.setAdapter(adapter1);
    }

    private void loadNotesList2() {

        final Query query = firestoreDB.collection("books").whereEqualTo("location", userCityL);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter2 = new FirestoreRecyclerAdapter<Note, NoteViewHorizontal>(response) {
            @Override
            protected void onBindViewHolder(final NoteViewHorizontal holder, int position, Note model) {

                final Note noteHorizontal2 = notesList2.get(position);

                holder.title.setText(noteHorizontal2.getTitle());
                holder.author.setText(noteHorizontal2.getAuthor());

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(noteHorizontal2.getDownloadUri()).apply(options).into(holder.bookpic);

                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

                        UpdateNoteTitle = noteHorizontal2.getTitle();
                        titleExp.setText(UpdateNoteTitle);

                        UpdateNoteAuthor = noteHorizontal2.getAuthor();
                        authExp.setText(UpdateNoteAuthor);

                        UpdateNoteDegree = noteHorizontal2.getDegree();
                        degreeExp.setText(UpdateNoteDegree);

                        UpdateNoteSpecialization = noteHorizontal2.getSpecialization();
                        specialExp.setText(UpdateNoteSpecialization);

                        UpdateNoteLocation = noteHorizontal2.getLocation();
                        locationExp.setText(UpdateNoteLocation);

                        UpdateNoteMrp = noteHorizontal2.getMrp();
                        mrpExp.setText(UpdateNoteMrp);

                        UpdateNotePrice = noteHorizontal2.getPrice();
                        priceExp.setText(UpdateNotePrice);

                        UpdateNoteSellerMsg = noteHorizontal2.getSellerMsg();
                        sellerMsgExp.setText(UpdateNoteSellerMsg);

                        UpdateNoteUser = noteHorizontal2.getuser();
                        userExp.setText(UpdateNoteUser);

                        entryName = noteHorizontal2.getEntryName();
                        final String uid = noteHorizontal2.getUid().toString();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        Glide.with(getActivity()).load(noteHorizontal2.getDownloadUri()).apply(options).into(bookpic);

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(noteHorizontal2.getDownloadUri()).apply(options).into(ivExpandedPic);
                                //Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().into(ivExpandedPic);
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

                        Toast.makeText(getActivity(), entryName, Toast.LENGTH_SHORT).show();

                        Button call = myDialog.findViewById(R.id.btnCallSeller);
                        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);

                        Button chat = myDialog.findViewById(R.id.btnChatSeller);

                        chat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                launchOneToOneChat(uid, noteHorizontal2.getuser());

                            }
                        });

                        entryName = noteHorizontal2.getEntryName();
                        downloadUri = noteHorizontal2.getDownloadUri();

                    }
                });

            }

            @Override
            public NoteViewHorizontal onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.horizontal_item, parent, false);

                return new NoteViewHorizontal(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter2.notifyDataSetChanged();
        recyclerView2.setAdapter(adapter2);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        adapter1.startListening();
        adapter2.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        adapter1.stopListening();
        adapter2.stopListening();
    }

    private void launchOneToOneChat(String uid, String name){

        IChatUser iChatUserRecepient = new ChatUser(uid, name);

        ChatUI.getInstance().openConversationMessagesActivity(iChatUserRecepient);

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
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(SAVE_COUNT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putInt(SAVE_COUNT, count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        ((AppCompatActivity)getActivity()).setTitle(getActivity().getTitle());
        super.onDetach();
    }

    private void showFillProfileDialog() {

        Button btnDismiss = myDialog3.findViewById(R.id.btnDismiss);
        Button btnUpdate = myDialog3.findViewById(R.id.btnUpdate);

        final CheckBox simpleCheckBox = myDialog3.findViewById(R.id.cbDonotShow);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean checkBoxState = simpleCheckBox.isChecked();

                if (checkBoxState) {

                    SharedPreferences sharedpreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(DonotShow, "true");
                    editor.commit();

                }

                myDialog3.dismiss();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDialog3.dismiss();

                progressDialog2 = new ProgressDialog(getActivity());

                progressDialog2.setMessage("Updating profile...");
                progressDialog2.show();

                final EditText et_name = myDialog4.findViewById(R.id.et_name);
                final EditText et_email = myDialog4.findViewById(R.id.et_email);
                final EditText et_phone = myDialog4.findViewById(R.id.et_phone);
                final EditText et_city = myDialog4.findViewById(R.id.et_city);
                final EditText et_degree = myDialog4.findViewById(R.id.et_degree);
                final EditText et_special = myDialog4.findViewById(R.id.et_special);

                final String mail = firebaseAuth.getCurrentUser().getEmail();

                et_name.setText(userNameL);
                et_email.setText(mail);
                et_phone.setText(userPhoneL);
                et_city.setText(userCityL);
                et_degree.setText(userDegreeL);
                et_special.setText(userSpecialL);

                Button btnCancel = myDialog4.findViewById(R.id.btnCancel);
                Button btnUpdate = myDialog4.findViewById(R.id.btnUpdate);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog4.dismiss();
                    }
                });

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String name = et_name.getText().toString();
                        String email = et_email.getText().toString();
                        String phone = et_phone.getText().toString();
                        String city = et_city.getText().toString();
                        String degree = et_degree.getText().toString();
                        String specialization = et_special.getText().toString();

                        Map<String, String> userMap = new HashMap<>();

                        userMap.put("name", name);
                        userMap.put("email", email );
                        userMap.put("phone", phone);
                        userMap.put("city", city);
                        userMap.put("degree", degree);
                        userMap.put("specialization", specialization);

                        firestore.collection("users").document(email).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                progressDialog2.dismiss();

                                myDialog4.dismiss();

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(HomeFragment.this).attach(HomeFragment.this).commit();


                                Toast.makeText(getActivity(), "Profile updated successfully!!!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog2.dismiss();

                                String error = e.getMessage();
                                Toast.makeText(getActivity(), "Cannot update profile. Error:" + error, Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                });

                myDialog4.show();
            }
        });

        myDialog3.show();

    }
}
