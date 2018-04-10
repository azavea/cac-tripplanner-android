package com.gophillygo.app;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Custom binding adapter for loading images from a URL via Glide into an ImageView.
 */
public class GlideImageDataBinding {
    private GlideImageDataBinding() {}

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        Context context = imageView.getContext();
        Glide.with(context).load(url).into(imageView);
    }
}
