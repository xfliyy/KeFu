package com.m7.imkfsdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * 2018/7/30 12:35 by xufangli
 */
public class ImageLoadUtils {
    public static void load(Context context, String url, int placeholderId, int errorId, SimpleTarget<Bitmap> simpleTarget) {
        Glide.with(context).asBitmap().load(url).apply(RequestOptions.placeholderOf(placeholderId).error(errorId)).into(simpleTarget);
    }

    public static void load(Context context, String url, int placeholderId, int errorId, ImageView imageView) {
        Glide.with(context).load(url).apply(RequestOptions.placeholderOf(placeholderId).error(errorId).centerCrop()).transition(withCrossFade()).into(imageView);
    }

    public static void loadCircleImage(Context context, String url, int placeholderId, int errorId, ImageView imageView) {
        Glide.with(context).load(url).apply(RequestOptions.placeholderOf(placeholderId).error(errorId).circleCropTransform()).into(imageView);
    }


}
