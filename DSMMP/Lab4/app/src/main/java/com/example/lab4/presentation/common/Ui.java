package com.example.lab4.presentation.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public final class Ui {
    public static final int INK = Color.rgb(48, 38, 33);
    public static final int MUTED = Color.rgb(115, 101, 92);
    public static final int ACCENT = Color.rgb(164, 63, 40);
    public static final int CREAM = Color.rgb(255, 249, 242);
    public static final int SAND = Color.rgb(246, 234, 221);
    public static int dp(Context c, int n) { return Math.round(n * c.getResources().getDisplayMetrics().density); }
    public static GradientDrawable rounded(Context c, int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c, radius)); return d;
    }
    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c, 20), dp(c, 16), dp(c, 20), dp(c, 24)); l.setBackgroundColor(CREAM); return l;
    }
    public static TextView text(Context c, String value, int size, boolean bold) {
        TextView t = new TextView(c); t.setText(value); t.setTextSize(size); t.setTextColor(INK);
        if (bold) t.setTypeface(null, Typeface.BOLD); t.setLineSpacing(dp(c, 2), 1f); return t;
    }
    public static TextView muted(Context c, String value, int size) {
        TextView t = text(c, value, size, false); t.setTextColor(MUTED); return t;
    }
    public static MaterialButton button(Context c, String label) {
        MaterialButton b = new MaterialButton(c); b.setText(label); b.setAllCaps(false); b.setTextSize(16);
        b.setCornerRadius(dp(c, 16)); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        b.setTextColor(Color.WHITE); b.setMinHeight(dp(c, 54)); return b;
    }
    public static void space(LinearLayout l, int dp) {
        View v = new View(l.getContext()); l.addView(v, new LinearLayout.LayoutParams(1, Ui.dp(l.getContext(), dp)));
    }
    private Ui() { }
}
