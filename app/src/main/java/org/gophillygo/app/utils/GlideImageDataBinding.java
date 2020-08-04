package org.gophillygo.app.utils;

import android.content.Context;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

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
