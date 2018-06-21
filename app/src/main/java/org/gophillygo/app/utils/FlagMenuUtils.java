package org.gophillygo.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import org.gophillygo.app.R;
import org.gophillygo.app.data.models.AttractionFlag;

public class FlagMenuUtils {
    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    public static PopupMenu getFlagPopupMenu(Context context, View view, AttractionFlag selectedFlag) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());

        // Force icons to show in the popup menu via the support library API
        // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
        MenuPopupHelper popupHelper = new MenuPopupHelper(context,
                (MenuBuilder)menu.getMenu(),
                view);
        popupHelper.setForceShowIcon(true);
        popupHelper.setGravity(Gravity.END|Gravity.RIGHT);
        popupHelper.show();

        highlightSelectedFlag(context, menu, selectedFlag);

        return menu;
    }

    private static void highlightSelectedFlag(Context context, PopupMenu menu, AttractionFlag selectedFlag) {
        for (AttractionFlag.Option option : AttractionFlag.Option.values()) {
            if (option.id != null) {
                resetItem(menu.getMenu().findItem(option.id), context);
            }
        }
        if (selectedFlag != null && selectedFlag.getOption() != null && selectedFlag.getOption().id != null) {
            selectItem(menu.getMenu().findItem(selectedFlag.getOption().id), context);
        }
    }

    private static void resetItem(MenuItem item, Context context) {
        styleItem(item, R.color.color_text_grey, context);
    }

    private static void selectItem(MenuItem item, Context context) {
        styleItem(item, R.color.color_primary, context);
    }

    private static void styleItem(MenuItem item, @ColorRes int colorRes, Context context) {
        int color = ContextCompat.getColor(context, colorRes);
        Drawable drawable = item.getIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
            item.setIcon(drawable);
        }
        SpannableString title = new SpannableString(item.getTitle());
        title.setSpan(new ForegroundColorSpan(color), 0, title.length(), 0);
        item.setTitle(title);
    }
}
