package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyProfileFragment extends Fragment {

    private static final String TAG = "SecondActivity";
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private ListenerRegistration firestoreListener;
    private List<Note> notesList;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    private ProgressDialog progressDialog2;

    Dialog myDialog, myDialog2, myDialog3, myDialog4;
    String entryName;
    String downloadUri;

    String UpdateNoteTitle, UpdateNoteAuthor, UpdateNoteDegree, UpdateNoteSpecialization, UpdateNotePrice, UpdateNoteLocation, UpdateNoteUser, UpdateNoteMrp, UpdateNoteSellerMsg;

    String nameL, emailL, uidL;

    String userNameL, userPhoneL, userBackgroundL, userCityL, userProfilePicL;

    LinearLayout llAds, llFavs;

    TextView noAdsPlaced, noLikes, userNametv, userCity, userBackground, userSpecial, userPhoneNo, userEmailId;

    CircularImageView profilePic;

    private FirebaseFirestore firestore;


    public static final String mypreference = "mypref";
    public static final String DonotShow = "false";

    SharedPreferences sharedpreferences;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        final View view;

        view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        setRetainInstance(true);

        getActivity().setTitle("My profile");

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading your profile...");
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uidL = user.getUid();
        emailL = user.getEmail();
        nameL = user.getDisplayName();

        firestore = FirebaseFirestore.getInstance();

        ImageView editProfile = view.findViewById(R.id.profile_edit);

        userNametv = view.findViewById(R.id.tv_name);
        userCity = view.findViewById(R.id.tv_city);
        userBackground = view.findViewById(R.id.tv_background);
        userPhoneNo = view.findViewById(R.id.tv_phone);
        userEmailId = view.findViewById(R.id.tv_email);

        userEmailId.setText(emailL);
        userNametv.setText(nameL);

        profilePic = view.findViewById(R.id.profile_pic);

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.dialog_expanded);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.dialog_image_expanded);

        myDialog3 = new Dialog(getActivity());
        myDialog3.setContentView(R.layout.dialog_fill_profile);

        myDialog4 = new Dialog(getActivity());
        myDialog4.setContentView(R.layout.dialog_edit_profile);

        noAdsPlaced = view.findViewById(R.id.tv_materials);
        noLikes = view.findViewById(R.id.tv_favorites);

        RelativeLayout llAds = view.findViewById(R.id.llAds);
        llAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frm,new MyMaterialsFragment()).addToBackStack(null).commit();
            }
        });

        RelativeLayout llFavs = view.findViewById(R.id.llFavs);
        llFavs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frm,new MyFavoritesFragment()).addToBackStack(null).commit();
            }
        });

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final String mail = firebaseAuth.getCurrentUser().getEmail();

        final String profilePicUrl = firebaseAuth.getCurrentUser().getPhotoUrl().toString();

        final RequestOptions options = new RequestOptions();
        options.centerCrop();

        Glide.with(getActivity()).load(profilePicUrl).apply(options).into(profilePic);

        DocumentReference docRef = firestoreDB.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        userNameL = document.getString("name");
                        userPhoneL = document.getString("phone");
                        userBackgroundL = document.getString("background");
                        userCityL = document.getString("city");
                        userProfilePicL = document.getString("downloadUri");

                        userCity.setText(userCityL);
                        userBackground.setText(userBackgroundL);
                        userPhoneNo.setText(userPhoneL);

                        if ((userNameL == null) || (userPhoneL == null) || (userBackgroundL == null) || (userCityL == null)) {

                            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                            String showDialog = sharedpreferences.getString(DonotShow, "");

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

        recyclerView = view.findViewById(R.id.rvNoteList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        loadNotesList();

        firestoreListener = firestoreDB.collection("books").whereEqualTo("uid", uidL)
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

        firestoreDB.collection("books")
                .whereEqualTo("uid", uidL)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            int adsPlacedSize = querySnapshot.size();
                            String adsPlacedSizeString = String.valueOf(adsPlacedSize);
                            noAdsPlaced.setText(adsPlacedSizeString);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        firestoreDB.collection("users").document(emailL).collection("favorites")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            int likesSize = querySnapshot.size();
                            String likesSizeString = String.valueOf(likesSize);
                            noLikes.setText(likesSizeString);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog1 = new ProgressDialog(getActivity());

                progressDialog1.setMessage("Loading edit window...");
                progressDialog1.show();

                updateUserData();

                progressDialog1.dismiss();

                myDialog4.show();
            }
        });

        progressDialog.dismiss();

        return view;

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

                    sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

                updateUserData();

                myDialog4.show();
            }
        });

        myDialog3.show();

    }

    private void loadNotesList() {

        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //String uid = user.getUid();

        final Query query = firestoreDB.collection("books").whereEqualTo("uid", uidL);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Note, NoteViewHorizontal>(response) {
            @Override
            protected void onBindViewHolder(NoteViewHorizontal holder, int position, Note model) {

                final Note noteHorizontal = notesList.get(position);

                holder.title.setText(noteHorizontal.getTitle());
                holder.author.setText(noteHorizontal.getAuthor());

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(holder.bookpic);

                holder.onclicklinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

                        UpdateNoteUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        userExp.setText(UpdateNoteUser);

                        entryName = noteHorizontal.getEntryName();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(bookpic);

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(noteHorizontal.getDownloadUri()).apply(options).into(ivExpandedPic);
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

                    }
                });

                Button chat = myDialog.findViewById(R.id.btnChatSeller);
                Button call = myDialog.findViewById(R.id.btnCallSeller);
                LikeButton likeButton = myDialog.findViewById(R.id.heart_button);

                chat.setVisibility(View.GONE);
                call.setVisibility(View.GONE);
                likeButton.setVisibility(View.GONE);

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

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void updateUserData() {

        final EditText et_name = myDialog4.findViewById(R.id.et_name);
        final EditText et_email = myDialog4.findViewById(R.id.et_email);
        final EditText et_phone = myDialog4.findViewById(R.id.et_phone);
        final Spinner spCity = myDialog4.findViewById(R.id.spCity);
        final Spinner spBackground = myDialog4.findViewById(R.id.spBackground);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.degrees_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBackground.setAdapter(adapter);

        ArrayAdapter<CharSequence> cityadapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.cities_array, android.R.layout.simple_spinner_item);
        cityadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(cityadapter);

        final String mail = firebaseAuth.getCurrentUser().getEmail();

        et_name.setText(userNameL);
        et_email.setText(mail);
        et_phone.setText(userPhoneL);

        if (userBackgroundL.equals("Architecture")){
            spBackground.setSelection(1);
        } else if (userBackgroundL.equals("Arts")) {
            spBackground.setSelection(2);
        } else if (userBackgroundL.equals("Commerce")) {
            spBackground.setSelection(3);
        } else if (userBackgroundL.equals("Computer Applications")) {
            spBackground.setSelection(4);
        } else if (userBackgroundL.equals("Education")) {
            spBackground.setSelection(5);
        } else if (userBackgroundL.equals("Engineering")) {
            spBackground.setSelection(6);
        } else if (userBackgroundL.equals("Literature")) {
            spBackground.setSelection(7);
        } else if (userBackgroundL.equals("Law")) {
            spBackground.setSelection(8);
        } else if (userBackgroundL.equals("Medical")) {
            spBackground.setSelection(9);
        } else if (userBackgroundL.equals("Science")) {
            spBackground.setSelection(10);
        }

        if (userCityL.equals("Ariyalur")){
            spCity.setSelection(1);
        } else if (userCityL.equals("Chennai")) {
            spCity.setSelection(2);
        } else if (userCityL.equals("Coimbatore")) {
            spCity.setSelection(3);
        } else if (userCityL.equals("Cuddalore")) {
            spCity.setSelection(4);
        } else if (userCityL.equals("Dharmapuri")) {
            spCity.setSelection(5);
        } else if (userCityL.equals("Dindigul")) {
            spCity.setSelection(6);
        } else if (userCityL.equals("Erode")) {
            spCity.setSelection(7);
        } else if (userCityL.equals("Kancheepuram")) {
            spCity.setSelection(8);
        } else if (userCityL.equals("Karur")) {
            spCity.setSelection(9);
        } else if (userCityL.equals("Krishnagiri")) {
            spCity.setSelection(10);
        } else if (userCityL.equals("Madurai")) {
            spCity.setSelection(11);
        } else if (userCityL.equals("Nagapattinam")) {
            spCity.setSelection(12);
        } else if (userCityL.equals("Kanyakumari")) {
            spCity.setSelection(13);
        } else if (userCityL.equals("Namakkal")) {
            spCity.setSelection(14);
        } else if (userCityL.equals("Perambalur")) {
            spCity.setSelection(15);
        } else if (userCityL.equals("Pudukottai")) {
            spCity.setSelection(16);
        } else if (userCityL.equals("Ramanathapuram")) {
            spCity.setSelection(17);
        } else if (userCityL.equals("Salem")) {
            spCity.setSelection(18);
        } else if (userCityL.equals("Sivagangai")) {
            spCity.setSelection(19);
        } else if (userCityL.equals("Thanjavur")) {
            spCity.setSelection(20);
        } else if (userCityL.equals("Theni")) {
            spCity.setSelection(21);
        } else if (userCityL.equals("Thiruvallur")) {
            spCity.setSelection(22);
        } else if (userCityL.equals("Thiruvarur")) {
            spCity.setSelection(23);
        } else if (userCityL.equals("Tuticorin")) {
            spCity.setSelection(24);
        } else if (userCityL.equals("Trichirappalli")) {
            spCity.setSelection(25);
        } else if (userCityL.equals("Thirunelveli")) {
            spCity.setSelection(26);
        } else if (userCityL.equals("Tiruppur")) {
            spCity.setSelection(27);
        } else if (userCityL.equals("Thiruvannamalai")) {
            spCity.setSelection(28);
        } else if (userCityL.equals("The Nilgiris")) {
            spCity.setSelection(29);
        } else if (userCityL.equals("Vellore")) {
            spCity.setSelection(30);
        } else if (userCityL.equals("Virudhunagar")) {
            spCity.setSelection(31);
        }

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

                String nameLL = et_name.getText().toString();
                String emailLL = et_email.getText().toString();
                String phoneLL = et_phone.getText().toString();
                String cityLL = spCity.getSelectedItem().toString();
                String backgroundLL = spBackground.getSelectedItem().toString();

                Map<String, String> userMap = new HashMap<>();

                userMap.put("name", nameLL);
                userMap.put("email", emailLL );
                userMap.put("phone", phoneLL);
                userMap.put("city", cityLL);
                userMap.put("background", backgroundLL);

                final String mailid = firebaseAuth.getCurrentUser().getEmail();

                firestore.collection("users").document(mailid).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        myDialog4.dismiss();

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.frm,new MyProfileFragment()).addToBackStack(null).commit();

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

    }

}
