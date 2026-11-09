package com.example.lab4.presentation.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    public static int dp(Context c, int n) { return Math.round(n * c.getResources().getDisplayMetrics().density); }
    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c, 20), dp(c, 20), dp(c, 20), dp(c, 20));
        l.setBackgroundColor(Color.rgb(255, 250, 244));
        return l;
    }
    public static TextView text(Context c, String value, int size, boolean bold) {
        TextView t = new TextView(c);
        t.setText(value); t.setTextSize(size); t.setTextColor(Color.rgb(49, 36, 29));
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(c, 6), 0, dp(c, 6));
        return t;
    }
    public static Button button(Context c, String label) { Button b = new Button(c); b.setText(label); return b; }
    public static void space(LinearLayout l, int dp) { View v = new View(l.getContext()); l.addView(v, new LinearLayout.LayoutParams(1, Ui.dp(l.getContext(), dp))); }
    private Ui() { }
}
