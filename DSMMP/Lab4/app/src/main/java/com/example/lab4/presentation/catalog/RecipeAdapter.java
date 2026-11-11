package com.example.lab4.presentation.catalog;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.presentation.common.Ui;
import com.google.android.material.card.MaterialCardView;
import java.io.File;

public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.Holder> {
    public interface OnRecipeClick { void onClick(long id); }
    private final OnRecipeClick listener;
    public RecipeAdapter(OnRecipeClick listener) {
        super(new DiffUtil.ItemCallback<Recipe>() {
            @Override public boolean areItemsTheSame(@NonNull Recipe a, @NonNull Recipe b) { return a.id == b.id; }
            @Override public boolean areContentsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                return a.title.equals(b.title) && a.category.equals(b.category) && a.minutes == b.minutes && a.imagePath.equals(b.imagePath);
            }
        }); this.listener = listener;
    }
    static class Holder extends RecyclerView.ViewHolder {
        final ImageView photo; final TextView title, category, time;
        Holder(View item, ImageView photo, TextView title, TextView category, TextView time) {
            super(item); this.photo=photo; this.title=title; this.category=category; this.time=time;
        }
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.content.Context c = parent.getContext();
        MaterialCardView card = new MaterialCardView(c); card.setRadius(Ui.dp(c, 20)); card.setCardElevation(Ui.dp(c, 2));
        card.setCardBackgroundColor(0xffffffff); card.setClickable(true); card.setFocusable(true);
        LinearLayout row = new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(Ui.dp(c, 9), Ui.dp(c, 9), Ui.dp(c, 14), Ui.dp(c, 9));
        ImageView photo = new ImageView(c); photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackground(Ui.rounded(c, Ui.SAND, 14)); photo.setClipToOutline(true);
        row.addView(photo, new LinearLayout.LayoutParams(Ui.dp(c, 112), Ui.dp(c, 112)));
        LinearLayout labels = new LinearLayout(c); labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(Ui.dp(c, 16), 0, 0, 0);
        TextView category = Ui.muted(c, "", 12); category.setLetterSpacing(.1f); labels.addView(category);
        Ui.space(labels, 5); TextView title = Ui.text(c, "", 19, true); title.setMaxLines(2); title.setEllipsize(android.text.TextUtils.TruncateAt.END); labels.addView(title);
        Ui.space(labels, 8); TextView time = Ui.muted(c, "", 14); labels.addView(time);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1)); card.addView(row);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -2); params.setMargins(0, 0, 0, Ui.dp(c, 12)); card.setLayoutParams(params);
        return new Holder(card, photo, title, category, time);
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Recipe r = getItem(position); h.title.setText(r.title); h.category.setText(r.category.toUpperCase(java.util.Locale.ROOT));
        h.time.setText("◷  " + r.minutes + " мин");
        Glide.with(h.photo).load(new File(r.imagePath)).override(300, 300).centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(h.photo);
        h.itemView.setOnClickListener(v -> listener.onClick(r.id));
    }
}
