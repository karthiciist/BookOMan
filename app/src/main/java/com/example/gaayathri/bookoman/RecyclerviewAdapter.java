package com.example.gaayathri.bookoman;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.gaayathri.bookoman.model.Note;

import java.util.List;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {

    private List<Note> notesList;
    private Context context;
    private FirebaseFirestore firestoreDB;

    public RecyclerviewAdapter(List<Note> notesList, Context context, FirebaseFirestore firestoreDB) {
        this.notesList = notesList;
        this.context = context;
        this.firestoreDB = firestoreDB;
    }

    @Override
    public RecyclerviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);

        return new RecyclerviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerviewAdapter.ViewHolder holder, int position) {
        final int itemPosition = position;
        final Note note = notesList.get(itemPosition);

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getAuthor());


    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        ImageView edit;
        ImageView delete;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvTitle);
            content = view.findViewById(R.id.tvContent);

        }
    }

}