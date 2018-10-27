package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gaayathri.bookoman.model.Note;
import com.example.gaayathri.bookoman.viewholder.NoteViewHorizontal;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import ss.com.bannerslider.Slider;

public class HomeFragment extends Fragment implements OnLikeListener, OnAnimationEndListener {

    private static final String TAG = "HomeFragment";
    private FirebaseAuth firebaseAuth;
    private Slider slider;

    private RecyclerView recyclerView, recyclerView1, recyclerView2;
    private ListenerRegistration firestoreListener, firestoreListener1, firestoreListener2;
    private List<Note> notesList, notesList1, notesList2;
    private FirestoreRecyclerAdapter adapter, adapter1, adapter2;

    Dialog myDialog, myDialog2, userDataDialog, chatIntroDialog;

    // For first recycler view
    String UpdateNoteTitle, UpdateNoteAuthor, UpdateNoteDegree, UpdateNoteSpecialization, UpdateNotePrice, UpdateNoteLocation, UpdateNoteUser, UpdateNoteMrp, UpdateNoteSellerMsg, entryName, downloadUri;

    String userNameL, userPhoneL, userBackgroundL, userCityL;

    BookLoading bookLoading;

    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";

    private int count;
    private static final String SAVE_COUNT = "save_count";
    public static final String DonotShow = "false";
    private ProgressDialog progressDialog1, progressDialog2;
    private FirebaseFirestore firestore;

    String cityL, backgroundL;

    private PrefManager prefManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View view;

        view = inflater.inflate(R.layout.fragment_home, container, false);

        setRetainInstance(true);

        getActivity().setTitle("Book O Man");

        setDialogs();

        setHomeScreeenButtons(view);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String mail = firebaseAuth.getCurrentUser().getEmail();

        sharedpreferences = getActivity().getSharedPreferences("mypref", 0);

        if (!(sharedpreferences.contains("city")) || (!(sharedpreferences.contains("background")))) {

            DocumentReference docRef = firestore.collection("users").document(mail);
            final FirebaseAuth finalFirebaseAuth = firebaseAuth;
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                public static final String TAG = "SplashScreen";

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //String userCityL;
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                            String name = document.getString("name");
                            String phone = document.getString("phone");
                            backgroundL = document.getString("background");
                            cityL = document.getString("city");
                            String userProfilePic = finalFirebaseAuth.getCurrentUser().getPhotoUrl().toString();
                            String email = finalFirebaseAuth.getCurrentUser().getEmail();

                            TextView userCity = view.findViewById(R.id.userCity);
                            userCity.setText(cityL);

                            TextView userBackground = view.findViewById(R.id.userBackground);
                            userBackground.setText(backgroundL);

                            sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("city", cityL);
                            editor.putString("background", backgroundL);
                            editor.apply();

                            if ((cityL == null) || (backgroundL == null)) {

                                sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
                                String showDialog = sharedpreferences.getString(DonotShow, "false");

                                if (!showDialog.equals("true") ) {
                                    showFillProfileDialog();
                                }

                            }

                        } else {
                            Log.d(TAG, "No such document");
                        }

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        } else if ((sharedpreferences.contains("city")) & ((sharedpreferences.contains("background")))) {

            TextView userCity = view.findViewById(R.id.userCity);
            userCity.setText(sharedpreferences.getString("city", "Chennai"));

            TextView userBackground = view.findViewById(R.id.userBackground);
            userBackground.setText(sharedpreferences.getString("background", "Engineering"));

        }

        Spinner citySpinner = userDataDialog.findViewById(R.id.spinnerCity);
        Spinner degreeSpinner = userDataDialog.findViewById(R.id.spinnerBackground);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cities_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> spinnerAdapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.degrees_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        citySpinner.setAdapter(spinnerAdapter);
        degreeSpinner.setAdapter(spinnerAdapter2);

        // Slider banner
        Slider.init(new GlideImageLoadingService(getActivity()));
        slider = view.findViewById(R.id.banner_slider_home);
        slider.setAdapter(new MainSliderAdapter());

        recyclerView = view.findViewById(R.id.rvNoteList1);
        setHorizontalRecyclerView(recyclerView);

        recyclerView1 = view.findViewById(R.id.rvNoteList2);
        setHorizontalRecyclerView(recyclerView1);

        recyclerView2 = view.findViewById(R.id.rvNoteList3);
        setHorizontalRecyclerView(recyclerView2);

        loadNotesList();

        firestoreListener = firestore.collection("books").orderBy("timestamp", Query.Direction.DESCENDING)
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

        sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
        String city = sharedpreferences.getString("city", "Chennai");
        String background = sharedpreferences.getString("background", "Engineering");

        loadNotesList1();

        firestoreListener1 = firestore.collection("books").whereEqualTo("location", city)
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

        firestoreListener2 = firestore.collection("books").whereEqualTo("degree", background)
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

        final DocumentReference docRef = firestore.collection("users").document(firebaseAuth.getCurrentUser().getEmail());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    String cityLL = snapshot.getString("city");
                    String backgroundLL = snapshot.getString("background");

                    sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("city", cityLL);
                    editor.putString("background", backgroundLL);
                    editor.apply();

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }

        });

        return view;


    }

    private void setHorizontalRecyclerView(RecyclerView recyclerView) {

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void setHomeScreeenButtons(View view) {

        Button sell1 = view.findViewById(R.id.sell1);
        Button sell2 = view.findViewById(R.id.sell2);

        progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setMessage("Loading...");

        sell1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog1.show();
                callSellActivity();
            }
        });

        sell2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog1.show();
                callSellActivity();
            }
        });

    }

    private void setDialogs() {

        userDataDialog = new Dialog(getActivity());
        userDataDialog.setContentView(R.layout.dialog_userdata_entry);

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.dialog_expanded);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.dialog_image_expanded);

    }

    public void callSellActivity() {

        startActivity(new Intent(getActivity(), SellTestActivity.class));

    }

    private void loadNotesList() {

        final Query query = firestore.collection("books").orderBy("timestamp", Query.Direction.DESCENDING);

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

                        populateMydialog(noteHorizontal);

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

        final Query query = firestore.collection("books").whereEqualTo("location", sharedpreferences.getString("city", "Chennai"));

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

                        populateMydialog(noteHorizontal1);

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

        final Query query = firestore.collection("books").whereEqualTo("degree", sharedpreferences.getString("background", "Engineering"));

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

                        populateMydialog(noteHorizontal2);

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

        progressDialog1.dismiss();
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

        firestore.collection("users").document(email).collection("favorites").document(entryName).set(bookMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        firestore.collection("books").document(entryName)
                .set(data, SetOptions.merge());

    }

    @Override
    public void unLiked(LikeButton likeButton) {

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uuid = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("users").document(email).collection("favorites").document(entryName)
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

        firestore.collection("books").document(entryName)
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
        progressDialog1.dismiss();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        (getActivity()).setTitle(getActivity().getTitle());
        progressDialog1.dismiss();
        super.onDetach();
    }

    private void showFillProfileDialog() {

        Button btnDismiss = userDataDialog.findViewById(R.id.btnDismiss);
        Button btnUpdate = userDataDialog.findViewById(R.id.btnUpdate);

        final CheckBox simpleCheckBox = userDataDialog.findViewById(R.id.cbDonotShow);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean checkBoxState = simpleCheckBox.isChecked();

                if (checkBoxState) {

                    sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(DonotShow, "true");
                    editor.apply();

                }

                userDataDialog.dismiss();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText et_phone = userDataDialog.findViewById(R.id.et_phone);
                final Spinner et_city = userDataDialog.findViewById(R.id.spinnerCity);
                final Spinner et_background = userDataDialog.findViewById(R.id.spinnerBackground);

                String phoneL = et_phone.getText().toString();
                String cityL = et_city.getSelectedItem().toString();
                String backgroundL = et_background.getSelectedItem().toString();

                if ((cityL.equals("Select a city")) || (backgroundL.equals("Select a degree"))) {

                    Toast.makeText(getActivity(), "Choose a valid city and degree", Toast.LENGTH_SHORT).show();

                } else if (!(cityL.equals("Select a city")) & !(backgroundL.equals("Select a degree"))) {

                    progressDialog2 = new ProgressDialog(getActivity());

                    progressDialog2.setMessage("Updating profile...");
                    progressDialog2.show();

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    String email = firebaseAuth.getCurrentUser().getEmail();

                    sharedpreferences = getActivity().getSharedPreferences("mypref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("city", cityL);
                    editor.putString("background", backgroundL);
                    editor.apply();

                    Map<String, String> userMap = new HashMap<>();

                    userMap.put("phone", phoneL);
                    userMap.put("city", cityL);
                    userMap.put("background", backgroundL);

                    firestore.collection("users").document(email).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progressDialog2.dismiss();

                            userDataDialog.dismiss();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.frm,new HomeFragment()).addToBackStack(null).commit();

                            Toast.makeText(getActivity(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog2.dismiss();

                            String error = e.getMessage();
                            Toast.makeText(getActivity(), "Cannot update profile. Error:" + error, Toast.LENGTH_SHORT).show();

                        }
                    });

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(HomeFragment.this).attach(HomeFragment.this).commit();

                }


            }
        });

        userDataDialog.show();

    }

    private void populateMydialog(final Note note){

        final RequestOptions options = new RequestOptions();
        options.centerCrop();

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

        UpdateNoteTitle = note.getTitle();
        titleExp.setText(UpdateNoteTitle);

        UpdateNoteAuthor = note.getAuthor();
        authExp.setText(UpdateNoteAuthor);

        UpdateNoteDegree = note.getDegree();
        degreeExp.setText(UpdateNoteDegree);

        UpdateNoteSpecialization = note.getSpecialization();
        specialExp.setText(UpdateNoteSpecialization);

        UpdateNoteLocation = note.getLocation();
        locationExp.setText(UpdateNoteLocation);

        UpdateNoteMrp = note.getMrp();
        mrpExp.setText(UpdateNoteMrp);

        UpdateNotePrice = note.getPrice();
        priceExp.setText(UpdateNotePrice);

        UpdateNoteSellerMsg = note.getSellerMsg();
        sellerMsgExp.setText(UpdateNoteSellerMsg);

        UpdateNoteUser = note.getuser();
        userExp.setText(UpdateNoteUser);

        entryName = note.getEntryName();
        final String uid = note.getUid().toString();

        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        Glide.with(getActivity()).load(note.getDownloadUri()).apply(options).into(bookpic);

        myDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
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

        //Toast.makeText(getActivity(), entryName, Toast.LENGTH_SHORT).show();

        Button call = myDialog.findViewById(R.id.btnCallSeller);
        Button chat = myDialog.findViewById(R.id.btnChatSeller);

        String emailCheck = note.getEmail();
        String emailCheck1 = firebaseAuth.getCurrentUser().getEmail();

        if (emailCheck.equals(emailCheck1)) {

            call.setVisibility(View.GONE);
            chat.setVisibility(View.GONE);

            userExp.setText("you");

        } else {

            call.setVisibility(View.VISIBLE);
            chat.setVisibility(View.VISIBLE);

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    progressDialog1 = new ProgressDialog(getActivity());
                    progressDialog1.setMessage("Loading...");
                    progressDialog1.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                    progressDialog1.show();

                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + note.getPhone()));
                    startActivity(dialIntent);
                }
            });

            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    prefManager = new PrefManager(getActivity());
                    if (prefManager.isChatFirstTimeLaunch()) {

                        chatIntroDialog = new Dialog(getActivity());
                        chatIntroDialog.setContentView(R.layout.dialog_chat);
                        chatIntroDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                        chatIntroDialog.show();

                        Button btnGotIt = chatIntroDialog.findViewById(R.id.btnGotIt);
                        btnGotIt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressDialog1 = new ProgressDialog(getActivity());
                                progressDialog1.setMessage("Loading...");
                                progressDialog1.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                                progressDialog1.show();

                                launchOneToOneChat(uid, note.getuser());
                                chatIntroDialog.dismiss();
                                prefManager.chatSetFirstTimeLaunch(false);
                            }
                        });

                    } else {
                        progressDialog1 = new ProgressDialog(getActivity());
                        progressDialog1.setMessage("Loading...");
                        progressDialog1.show();

                        launchOneToOneChat(uid, note.getuser());

                    }

                }
            });

        }

        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        checkFavorited();

    }

    private void checkFavorited() {

        String email = firebaseAuth.getCurrentUser().getEmail();

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
        });
    }
}
