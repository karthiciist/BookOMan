package com.example.gaayathri.bookoman;


import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyFavoritesFragment extends Fragment implements OnLikeListener, OnAnimationEndListener {

    private EmptyRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private ListenerRegistration firestoreListener;
    private List<Note> notesList;
    private ProgressDialog progressDialog;

    Dialog myDialog;
    Dialog myDialog2;

    String entryName;
    String downloadUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.fragment_my_favorites, container, false);

        setRetainInstance(true);

        getActivity().setTitle("My favorites");

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.dialog_expanded);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.dialog_image_expanded);

        LikeButton likeButton = myDialog.findViewById(R.id.heart_button);
        likeButton.setOnLikeListener(this);
        likeButton.setOnAnimationEndListener(this);

        likeButton.setLiked(true);

        progressDialog = new ProgressDialog(getActivity());

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.rvNoteList);

        LinearLayout tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView.setEmptyView(tvEmpty);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        loadNotesList();

        String uid = firebaseAuth.getCurrentUser().getUid();

        firestoreListener = firestoreDB.collection("books").whereEqualTo(uid, true).orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            //Toast.makeText(getActivity(), "Cannot retrieve your favorites", Toast.LENGTH_SHORT).show();
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


        return view;
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

    @Override
    public void liked(LikeButton likeButton) {

        String email = firebaseAuth.getCurrentUser().getEmail();
        String uuid = firebaseAuth.getCurrentUser().getUid();

        TextView titleFav = myDialog.findViewById(R.id.titleExp);
        TextView authFav = myDialog.findViewById(R.id.authorExp);
        TextView degreeFav = myDialog.findViewById(R.id.degreeExp);
        TextView specialFav = myDialog.findViewById(R.id.specialExp);
        TextView locationFav = myDialog.findViewById(R.id.locationExp);
        TextView mrpFav = myDialog.findViewById(R.id.mrpPriceExp);
        TextView priceFav = myDialog.findViewById(R.id.priceExp);
        TextView userFav = myDialog.findViewById(R.id.userExp);
        TextView sellerMsgFav = myDialog.findViewById(R.id.sellerMsgExp);
        ImageView bookpic = myDialog.findViewById(R.id.ivBookPic);



        final Map<String, String> bookMap = new HashMap<>();

        bookMap.put("liked", entryName);
        bookMap.put("title", titleFav.getText().toString());
        bookMap.put("author", authFav.getText().toString());
        bookMap.put("degree", degreeFav.getText().toString());
        bookMap.put("specialization", specialFav.getText().toString());
        bookMap.put("location", locationFav.getText().toString());
        bookMap.put("price", priceFav.getText().toString());
        bookMap.put("mrp", mrpFav.getText().toString());
        bookMap.put("user",userFav.getText().toString());
        bookMap.put("sellerMsg", sellerMsgFav.getText().toString());
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

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frm,new MyFavoritesFragment()).addToBackStack(null).commit();

        myDialog.dismiss();
    }

    @Override
    public void onAnimationEnd(LikeButton likeButton) {
        Log.d("ExpandedView", "Animation End for %s" + likeButton);
    }

    private void loadNotesList() {

        String uid = firebaseAuth.getCurrentUser().getUid();

        CollectionReference booksRef = firestoreDB.collection("books");

        Query query = booksRef.whereEqualTo(uid, true).orderBy("timestamp", Query.Direction.DESCENDING);


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

                        progressDialog.setMessage("Loading your favorite material...");
                        progressDialog.show();

                        TextView titleFav = myDialog.findViewById(R.id.titleExp);
                        TextView authFav = myDialog.findViewById(R.id.authorExp);
                        TextView degreeFav = myDialog.findViewById(R.id.degreeExp);
                        TextView specialFav = myDialog.findViewById(R.id.specialExp);
                        TextView locationFav = myDialog.findViewById(R.id.locationExp);
                        TextView mrpFav = myDialog.findViewById(R.id.mrpPriceExp);
                        TextView priceFav = myDialog.findViewById(R.id.priceExp);
                        TextView userFav = myDialog.findViewById(R.id.userExp);
                        TextView sellerMsgFav = myDialog.findViewById(R.id.sellerMsgExp);

                        ImageView bookpic = myDialog.findViewById(R.id.ivBookPic);

                        titleFav.setText(note.getTitle());
                        authFav.setText(note.getAuthor());
                        degreeFav.setText(note.getDegree());
                        specialFav.setText(note.getSpecialization());
                        locationFav.setText(note.getLocation());
                        mrpFav.setText(note.getMrp());
                        priceFav.setText(note.getPrice());
                        userFav.setText(note.getuser());
                        sellerMsgFav.setText(note.getSellerMsg());

                        final String uid = note.getUid().toString();

                        entryName = note.getEntryName();

                        mrpFav.setPaintFlags(mrpFav.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        Glide.with(getActivity()).load(note.getDownloadUri()).apply(options).into(bookpic);

                        Button btnCallSeller = myDialog.findViewById(R.id.btnCallSeller);
                        Button btnChatSeller = myDialog.findViewById(R.id.btnChatSeller);

                        btnCallSeller.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                                dialIntent.setData(Uri.parse("tel:" + note.getPhone()));
                                startActivity(dialIntent);
                            }
                        });

                        btnChatSeller.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                launchOneToOneChat(uid, note.getuser());
                            }
                        });

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

}
