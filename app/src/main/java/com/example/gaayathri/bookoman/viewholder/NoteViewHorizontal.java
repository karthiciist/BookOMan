package com.example.gaayathri.bookoman.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gaayathri.bookoman.R;

public class NoteViewHorizontal extends RecyclerView.ViewHolder {

    public TextView title, author;
    public ImageView bookpic;
    public LinearLayout onclicklinearLayout;

    public NoteViewHorizontal(View view) {
        super(view);
        title = view.findViewById(R.id.tvTitle);
        author = view.findViewById(R.id.tvAuthor);

        bookpic = view.findViewById(R.id.ivBookPic);

        onclicklinearLayout = view.findViewById(R.id.llOnClick);

    }

}
