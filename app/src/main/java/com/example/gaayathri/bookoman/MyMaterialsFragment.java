package com.example.gaayathri.bookoman;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
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

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.like.LikeButton;
import java.util.ArrayList;
import java.util.List;

public class MyMaterialsFragment extends Fragment {

    private EmptyRecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;

    private FirebaseFirestore firestoreDB;
    private ListenerRegistration firestoreListener;
    private List<Note> notesList;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    Dialog myDialog;
    Dialog myDialog2;
    Dialog myDialog3;
    String entryName;

    String UpdateNoteTitle;
    String UpdateNoteAuthor;
    String UpdateNoteDegree;
    String UpdateNoteSpecialization;
    String UpdateNotePrice;
    String UpdateNoteLocation;
    String UpdateNoteUser;
    String UpdateNoteMrp;
    String UpdateNoteSellerMsg;
    String UpdateDownloadUri;
    String downloadUri;

    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;

    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.fragment_my_materials, container, false);

        setRetainInstance(true);

        getActivity().setTitle("My materials");

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.dialog_my_material_expanded);

        myDialog2 = new Dialog(getActivity());
        myDialog2.setContentView(R.layout.dialog_image_expanded);

        myDialog3 = new Dialog(getActivity());
        myDialog3.setContentView(R.layout.dialog_confirm_delete);

        final Client client = new Client("H9P3XBA9GD", "3058fee363b2c4b8afe53e9d9eab642f");
        Index index = client.getIndex("books");

        progressDialog = new ProgressDialog(getActivity());

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        String uid = user.getUid();

        recyclerView = view.findViewById(R.id.rvNoteList);

        LinearLayout tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView.setEmptyView(tvEmpty);

        // FAB setup
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClicked();
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

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
                            Note noteMyMaterials = doc.toObject(Note.class);
                            noteMyMaterials.setEntryName(doc.getId());
                            notesList.add(noteMyMaterials);
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
    public void onDestroy() {
        super.onDestroy();
        firestoreListener.remove();
    }

    private void loadNotesList() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query = firestoreDB.collection("books").whereEqualTo("uid", uid);


        FirestoreRecyclerOptions<Note> response = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(response) {
            @Override
            protected void onBindViewHolder(NoteViewHolder holder, int position, Note model) {

                final Note noteMyMaterials = notesList.get(position);

                holder.title.setText(noteMyMaterials.getTitle());
                holder.author.setText(noteMyMaterials.getAuthor());
                holder.degree.setText(noteMyMaterials.getDegree());
                holder.specialization.setText(noteMyMaterials.getSpecialization());
                holder.mrp.setText(noteMyMaterials.getMrp());
                holder.price.setText(noteMyMaterials.getPrice());
                holder.location.setText(noteMyMaterials.getLocation());

                downloadUri = noteMyMaterials.getDownloadUri();

                final RequestOptions options = new RequestOptions();
                options.centerCrop();

                Glide.with(getActivity()).load(downloadUri).apply(options).into(holder.bookpic);

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

                        ImageView bookpicdialog = myDialog.findViewById(R.id.ivBookPic);

                        UpdateNoteTitle = noteMyMaterials.getTitle();
                        titleExp.setText(UpdateNoteTitle);

                        UpdateNoteAuthor = noteMyMaterials.getAuthor();
                        authExp.setText(UpdateNoteAuthor);

                        UpdateNoteDegree = noteMyMaterials.getDegree();
                        degreeExp.setText(UpdateNoteDegree);

                        UpdateNoteSpecialization = noteMyMaterials.getSpecialization();
                        specialExp.setText(UpdateNoteSpecialization);

                        UpdateNoteLocation = noteMyMaterials.getLocation();
                        locationExp.setText(UpdateNoteLocation);

                        UpdateNoteMrp = noteMyMaterials.getMrp();
                        mrpExp.setText(UpdateNoteMrp);

                        UpdateNotePrice = noteMyMaterials.getPrice();
                        priceExp.setText(UpdateNotePrice);

                        UpdateNoteSellerMsg = noteMyMaterials.getSellerMsg();
                        sellerMsgExp.setText(UpdateNoteSellerMsg);

                        UpdateNoteUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        userExp.setText(UpdateNoteUser);

                        entryName = noteMyMaterials.getEntryName();

                        mrpExp.setPaintFlags(mrpExp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        UpdateDownloadUri = noteMyMaterials.getDownloadUri();
                        Glide.with(getActivity()).load(noteMyMaterials.getDownloadUri()).apply(options).into(bookpicdialog);

                        myDialog.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
                        myDialog.show();

                        bookpicdialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView ivExpandedPic = myDialog2.findViewById(R.id.ivImage);
                                Glide.with(getActivity()).load(noteMyMaterials.getDownloadUri()).apply(options).into(ivExpandedPic);
                                myDialog2.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
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

                        Toast.makeText(getActivity(), entryName, Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();

                    }
                });

                Button edit = myDialog.findViewById(R.id.btnEdit);
                Button delete = myDialog.findViewById(R.id.btnDelete);
                LikeButton likeButton = myDialog.findViewById(R.id.heart_button);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateNote();
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //deleteNote();
                        showDeleteConfirmationDialog();
                    }
                });
                likeButton.setVisibility(View.GONE);

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

    private void showDeleteConfirmationDialog() {

        Button btnDelete = myDialog3.findViewById(R.id.btnDelete);
        Button btnNo = myDialog3.findViewById(R.id.btnNo);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDialog3.dismiss();

                deleteNote();

            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDialog3.dismiss();

            }
        });

        myDialog3.getWindow().getAttributes().windowAnimations = R.style.Dialogscale;
        myDialog3.show();

    }

    private void updateNote() {

        Intent intent = new Intent(getActivity(), SellTestActivity.class);

        intent.putExtra("UpdateNoteEntryName", entryName);
        intent.putExtra("UpdateNoteTitle", UpdateNoteTitle);
        intent.putExtra("UpdateNoteAuthor", UpdateNoteAuthor);
        intent.putExtra("UpdateNoteDegree", UpdateNoteDegree);
        intent.putExtra("UpdateNoteSpecialization", UpdateNoteSpecialization);
        intent.putExtra("UpdateNotePrice", UpdateNotePrice);
        intent.putExtra("UpdateNoteLocation", UpdateNoteLocation);
        intent.putExtra("UpdateNoteUser", UpdateNoteUser);
        intent.putExtra("UpdateNoteMrp", UpdateNoteMrp);
        intent.putExtra("UpdateNoteSellerMsg", UpdateNoteSellerMsg);
        intent.putExtra("UpdateNoteDownloadUri", UpdateDownloadUri);
        startActivity(intent);
        myDialog.dismiss();
    }

    private void deleteNote() {

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        StorageReference imageReference = storageReference.child(firebaseAuth.getCurrentUser().getEmail()).child("bookimages").child("thumb_" + entryName);

        imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Book image deleted successfully!!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), "Cannot delete book image", Toast.LENGTH_SHORT).show();
            }
        });

        firestoreDB.collection("books")
                .document(entryName)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), "Material has been deleted!", Toast.LENGTH_SHORT).show();
                    }
                });
        firestoreDB.collection("users")
                .document(userEmail)
                .collection("books")
                .document(UpdateNoteTitle)
                .delete();

        final Client client = new Client("H9P3XBA9GD", "3058fee363b2c4b8afe53e9d9eab642f");

        Index index = client.getIndex("books");

        index.deleteObjectAsync(entryName, null);

        myDialog.dismiss();
    }

    private void fabClicked() {

        Intent intent = new Intent(getActivity(), SellTestActivity.class);
        startActivity(intent);

    }

}
