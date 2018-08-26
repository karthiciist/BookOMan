package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

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

    Dialog myDialog;
    Dialog myDialog2;
    Dialog myDialog3;
    Dialog myDialog4;
    String entryName;
    String downloadUri;

    String UpdateNoteTitle;
    String UpdateNoteAuthor;
    String UpdateNoteDegree;
    String UpdateNoteSpecialization;
    String UpdateNotePrice;
    String UpdateNoteLocation;
    String UpdateNoteUser;
    String UpdateNoteMrp;
    String UpdateNoteSellerMsg;

    String userName;
    String name;

    String userNameL;
    String userPhoneL;
    String userDegreeL;
    String userSpecialL;
    String userCityL;
    String userProfilePicL;

    LinearLayout llAds;
    LinearLayout llFavs;

    TextView noAdsPlaced;
    TextView noLikes;
    TextView userNametv;
    TextView userCity;
    TextView userDegree;
    TextView userSpecial;
    TextView userPhoneNo;
    TextView userEmailId;

    CircularImageView profilePic;

    private FirebaseFirestore firestore;

    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    public static final String DonotShow = "false";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        final View view;

        view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading your profile...");
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String name = user.getEmail();

        firestore = FirebaseFirestore.getInstance();

        ImageView editProfile = view.findViewById(R.id.profile_edit);

        userNametv = view.findViewById(R.id.tv_name);
        userCity = view.findViewById(R.id.tv_city);
        userDegree = view.findViewById(R.id.tv_degree);
        userSpecial = view.findViewById(R.id.tv_specialization);
        userPhoneNo = view.findViewById(R.id.tv_phone);
        userEmailId = view.findViewById(R.id.tv_email);

        profilePic = view.findViewById(R.id.profile_pic);

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.expandeddialog);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.imageexpanded);

        myDialog3 = new Dialog(getActivity());
        myDialog3.setContentView(R.layout.fillprofiledialog);

        myDialog4 = new Dialog(getActivity());
        myDialog4.setContentView(R.layout.edit_profile_dialog);

        noAdsPlaced = view.findViewById(R.id.tv_materials);
        noLikes = view.findViewById(R.id.tv_favorites);

        RelativeLayout llAds = view.findViewById(R.id.llAds);
        llAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frm,new MyMaterialsFragment()).commit();
            }
        });

        RelativeLayout llFavs = view.findViewById(R.id.llFavs);
        llFavs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frm,new MyFavoritesFragment()).commit();
            }
        });

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final String mail = firebaseAuth.getCurrentUser().getEmail();

        final String profilePicUrl = firebaseAuth.getCurrentUser().getPhotoUrl().toString();

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
                        userDegreeL = document.getString("degree");
                        userSpecialL = document.getString("specialization");
                        userCityL = document.getString("city");
                        userProfilePicL = document.getString("downloadUri");

                        userNametv.setText(userNameL);
                        userCity.setText(userCityL);
                        userDegree.setText(userDegreeL);
                        userSpecial.setText(userSpecialL);
                        userPhoneNo.setText(userPhoneL);
                        userEmailId.setText(mail);

                        Picasso.with(getActivity()).load(profilePicUrl).fit().centerCrop().into(profilePic);

                        if ((userNameL == null) || (userPhoneL == null) || (userDegreeL == null) || (userSpecialL == null) || (userCityL == null)) {

                            SharedPreferences sharedpreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

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

        firestoreListener = firestoreDB.collection("books").whereEqualTo("uid", uid)
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
                .whereEqualTo("uid", uid)
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

        firestoreDB.collection("users").document(name).collection("favorites")
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

                final EditText et_name = myDialog4.findViewById(R.id.et_name);
                final EditText et_email = myDialog4.findViewById(R.id.et_email);
                final EditText et_phone = myDialog4.findViewById(R.id.et_phone);
                final EditText et_city = myDialog4.findViewById(R.id.et_city);
                final EditText et_degree = myDialog4.findViewById(R.id.et_degree);
                final EditText et_special = myDialog4.findViewById(R.id.et_special);

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

                        progressDialog2 = new ProgressDialog(getActivity());

                        progressDialog2.setMessage("Updating profile...");
                        progressDialog2.show();

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
                                ft.detach(MyProfileFragment.this).attach(MyProfileFragment.this).commit();


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
                                ft.detach(MyProfileFragment.this).attach(MyProfileFragment.this).commit();


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

    private void loadNotesList() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        final Query query = firestoreDB.collection("books").whereEqualTo("uid", uid);

        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Note, NoteViewHorizontal>(response) {
            @Override
            protected void onBindViewHolder(NoteViewHorizontal holder, int position, Note model) {

                final Note noteHorizontal = notesList.get(position);

                holder.title.setText(noteHorizontal.getTitle());
                holder.author.setText(noteHorizontal.getAuthor());

                Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().centerCrop().into(holder.bookpic);

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

                        Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().centerCrop().into(bookpic);

                        myDialog.show();

                        bookpic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Picasso.with(getActivity()).load(noteHorizontal.getDownloadUri()).fit().into(ivExpandedPic);
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

}
