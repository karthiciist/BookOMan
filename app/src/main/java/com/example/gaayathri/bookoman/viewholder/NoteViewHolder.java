package com.example.gaayathri.bookoman.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gaayathri.bookoman.R;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    public TextView title, author, degree, specialization, location, mrp, price;
    public ImageView bookpic;
    public LinearLayout onclicklinearLayout;

    public NoteViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.tvTitle);
        author = view.findViewById(R.id.tvContent);
        degree = view.findViewById(R.id.tvDegree);
        specialization = view.findViewById(R.id.tvSpecial);
        location = view.findViewById(R.id.tvLocation);
        mrp = view.findViewById(R.id.tvMrp);
        price = view.findViewById(R.id.tvPrice);

        bookpic = view.findViewById(R.id.ivBookPic);

        onclicklinearLayout = view.findViewById(R.id.llOnClick);

        mrp.setPaintFlags(mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
