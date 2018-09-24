package com.example.gaayathri.bookoman.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gaayathri.bookoman.R;
import com.victor.loading.book.BookLoading;

public class NoteViewHorizontal extends RecyclerView.ViewHolder {

    public TextView title, author;
    public ImageView bookpic;
    public LinearLayout onclicklinearLayout;

    BookLoading bookLoading2;

    public NoteViewHorizontal(View view) {
        super(view);
        title = view.findViewById(R.id.tvTitle);
        author = view.findViewById(R.id.tvAuthor);

        bookpic = view.findViewById(R.id.ivBookPicHorizontal);

        onclicklinearLayout = view.findViewById(R.id.llOnClick);

        bookLoading2 = view.findViewById(R.id.bookloading2);
        bookLoading2.start();

    }

}
