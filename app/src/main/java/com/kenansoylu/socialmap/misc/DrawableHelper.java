package com.kenansoylu.socialmap.misc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * {@link Drawable} helper class.
 *
 * @author Filipe Bezerra
 * @version 18/01/2016
 * @since 18/01/2016
 * https://stackoverflow.com/questions/32924986/change-fill-color-on-vector-asset-in-android-studio
 */
public class DrawableHelper {
    @NonNull
    Context mContext;
    private Color mColor;
    private Drawable mDrawable;
    private Drawable mWrappedDrawable;

    public DrawableHelper(@NonNull Context context) {
        mContext = context;
    }

    public static DrawableHelper withContext(@NonNull Context context) {
        return new DrawableHelper(context);
    }

    public DrawableHelper withDrawable(@DrawableRes int drawableRes) {
        mDrawable = ContextCompat.getDrawable(mContext, drawableRes);
        return this;
    }

    public DrawableHelper withDrawable(@NonNull Drawable drawable) {
        mDrawable = drawable;
        return this;
    }

    public DrawableHelper withColor(Color color) {
        mColor = color;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public DrawableHelper tint() {
        if (mDrawable == null) {
            throw new NullPointerException("É preciso informar o recurso drawable pelo método withDrawable()");
        }

        if (mColor == null) {
            throw new IllegalStateException("É necessário informar a cor a ser definida pelo método withColor()");
        }

        mWrappedDrawable = mDrawable.mutate();
        mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
        DrawableCompat.setTint(mWrappedDrawable, mColor.toArgb());
        DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);

        return this;
    }

    public void applyToBackground(@NonNull View view) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        view.setBackground(mWrappedDrawable);
    }

    public void applyTo(@NonNull ImageView imageView) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        imageView.setImageDrawable(mWrappedDrawable);
    }

    public void applyTo(@NonNull MenuItem menuItem) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        menuItem.setIcon(mWrappedDrawable);
    }

    public Drawable get() {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("É preciso chamar o método tint()");
        }

        return mWrappedDrawable;
    }
}