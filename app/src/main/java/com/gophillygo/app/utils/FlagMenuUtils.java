package com.gophillygo.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.R;
import com.gophillygo.app.data.models.AttractionFlag;

import org.w3c.dom.Text;

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

        return menu;
    }
}
