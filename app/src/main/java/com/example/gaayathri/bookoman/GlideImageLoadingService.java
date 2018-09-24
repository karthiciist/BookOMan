package com.example.gaayathri.bookoman;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import ss.com.bannerslider.ImageLoadingService;

public class GlideImageLoadingService implements ImageLoadingService {

    public Context context;

    public GlideImageLoadingService(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        Glide.with(context).load(url).apply(RequestOptions.fitCenterTransform()).into(imageView);
    }

    @Override
    public void loadImage(int resource, ImageView imageView) {
        Glide.with(context).load(resource).apply(RequestOptions.fitCenterTransform()).into(imageView);
    }

    @Override
    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        Glide.with(context).load(url).apply(RequestOptions.fitCenterTransform()).into(imageView);
    }
}
