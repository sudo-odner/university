package com.example.lab4.presentation.catalog;

import android.graphics.Color;
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
        });
        this.listener = listener;
    }
    static class Holder extends RecyclerView.ViewHolder {
        ImageView photo; TextView title, meta;
        Holder(View item, ImageView photo, TextView title, TextView meta) { super(item); this.photo=photo; this.title=title; this.meta=meta; }
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.content.Context c = parent.getContext();
        LinearLayout row = new LinearLayout(c); row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 10));
        row.setBackgroundColor(Color.WHITE);
        ImageView photo = new ImageView(c); photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(photo, new LinearLayout.LayoutParams(Ui.dp(c, 96), Ui.dp(c, 96)));
        LinearLayout labels = new LinearLayout(c); labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(Ui.dp(c, 16), 0, 0, 0);
        TextView title = Ui.text(c, "", 20, true), meta = Ui.text(c, "", 15, false);
        labels.addView(title); labels.addView(meta);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, Ui.dp(c, 10)); row.setLayoutParams(params);
        return new Holder(row, photo, title, meta);
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Recipe r = getItem(position);
        h.title.setText(r.title); h.meta.setText(r.category + " · " + r.minutes + " мин");
        Glide.with(h.photo).load(new File(r.imagePath)).override(300, 300).centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(h.photo);
        h.itemView.setOnClickListener(v -> listener.onClick(r.id));
    }
}
