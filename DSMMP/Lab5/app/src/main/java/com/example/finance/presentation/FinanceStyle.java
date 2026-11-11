package com.example.finance.presentation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared visual components based on the finance theme from laboratory 3. */
final class FinanceStyle {
    static final int BACKGROUND = 0xFFF6F9FA, PRIMARY = 0xFF0F5B78, INK = 0xFF171C1E;
    static final int MUTED = 0xFF59666B, PALE_BLUE = 0xFFBCE9FF, WHITE = Color.WHITE;
    static int dp(Context c, float value) { return (int) (value * c.getResources().getDisplayMetrics().density + .5f); }
    static GradientDrawable shape(int color, float radius, Context c) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c, radius)); return d;
    }
    static TextView label(Context c, String text, int size, int color, boolean bold) {
        TextView v = new TextView(c); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v;
    }
    static LinearLayout column(Context c, int color, int padding) {
        LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, padding); v.setPadding(p,p,p,p); v.setBackground(shape(color, 20, c)); return v;
    }
    static void addSpace(LinearLayout parent, int height) { View v = new View(parent.getContext()); parent.addView(v, new LinearLayout.LayoutParams(1, dp(parent.getContext(), height))); }
    static void addCard(LinearLayout parent, View card) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(parent.getContext(), 10); parent.addView(card, lp);
    }
    static TextView action(Context c, String title, boolean primary, Runnable action) {
        TextView v = label(c, title, 14, primary ? WHITE : PRIMARY, true); v.setGravity(Gravity.CENTER);
        v.setPadding(dp(c, 12), dp(c, 14), dp(c, 12), dp(c, 14));
        v.setBackground(shape(primary ? PRIMARY : 0xFFE2F1F5, 14, c)); v.setOnClickListener(w -> action.run());
        v.setContentDescription(title); return v;
    }
}
