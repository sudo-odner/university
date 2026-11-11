package com.example.lab4.presentation.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.presentation.common.AppGraph;
import com.example.lab4.presentation.common.Ui;
import com.example.lab4.presentation.detail.DetailActivity;
import com.example.lab4.presentation.editor.EditorActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogActivity extends AppCompatActivity {
    private RecipeAdapter adapter;
    private RecyclerView list;
    private LinearLayout empty;
    private TextView count;
    private EditText search;
    private boolean gridMode;
    private com.google.android.material.button.MaterialButton layoutToggle;
    private List<Recipe> all = new ArrayList<>();
    private android.os.Parcelable scrollState;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.CREAM);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        FrameLayout frame = new FrameLayout(this);
        LinearLayout root = Ui.column(this); frame.addView(root, new FrameLayout.LayoutParams(-1, -1));
        TextView eyebrow = Ui.muted(this, "МОЯ КУХНЯ  /  РЕЦЕПТЫ", 12); eyebrow.setLetterSpacing(.14f); root.addView(eyebrow);
        Ui.space(root, 8); root.addView(Ui.text(this, "Готовить с удовольствием", 29, true));
        Ui.space(root, 6); root.addView(Ui.muted(this, "Сохраняйте любимые блюда и возвращайтесь к ним снова.", 15));
        Ui.space(root, 22);
        search = new EditText(this); search.setSingleLine(true); search.setTextSize(16); search.setHint("Поиск по названию или категории");
        search.setPadding(Ui.dp(this, 18), 0, Ui.dp(this, 18), 0);
        search.setBackground(Ui.rounded(this, 0xffffffff, 16)); search.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        search.setCompoundDrawablePadding(Ui.dp(this, 10)); root.addView(search, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));
        Ui.space(root, 24);
        LinearLayout heading = new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = Ui.text(this, "Коллекция", 22, true); heading.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        count = Ui.muted(this, "", 14); heading.addView(count);
        layoutToggle = new com.google.android.material.button.MaterialButton(this);
        layoutToggle.setAllCaps(false); layoutToggle.setTextSize(14); layoutToggle.setTextColor(Ui.ACCENT);
        layoutToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.SAND));
        layoutToggle.setCornerRadius(Ui.dp(this, 14)); layoutToggle.setInsetTop(0); layoutToggle.setInsetBottom(0);
        layoutToggle.setContentDescription("Переключить список и сетку");
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(Ui.dp(this, 88), Ui.dp(this, 48));
        toggleParams.leftMargin = Ui.dp(this, 12); heading.addView(layoutToggle, toggleParams);
        layoutToggle.setOnClickListener(v -> setGridMode(!gridMode));
        root.addView(heading); Ui.space(root, 14);
        FrameLayout content = new FrameLayout(this); root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        list = new RecyclerView(this);
        gridMode = getPreferences(MODE_PRIVATE).getBoolean("grid", false);
        list.setLayoutManager(gridMode ? new GridLayoutManager(this, 2) : new LinearLayoutManager(this)); list.setClipToPadding(false);
        list.setPadding(0, 0, 0, Ui.dp(this, 84));
        adapter = new RecipeAdapter(id -> startActivity(new Intent(this, DetailActivity.class).putExtra("id", id)));
        list.setAdapter(adapter); adapter.setGridMode(gridMode);
        layoutToggle.setText(gridMode ? "Список" : "Сетка"); content.addView(list, new FrameLayout.LayoutParams(-1, -1));
        empty = new LinearLayout(this); empty.setOrientation(LinearLayout.VERTICAL); empty.setGravity(Gravity.CENTER);
        empty.setPadding(Ui.dp(this, 24), Ui.dp(this, 30), Ui.dp(this, 24), Ui.dp(this, 30));
        empty.setBackground(Ui.rounded(this, Ui.SAND, 24));
        TextView icon = Ui.text(this, "✦", 48, false); icon.setTextColor(Ui.ACCENT); empty.addView(icon);
        Ui.space(empty, 8); TextView title = Ui.text(this, "Здесь появятся ваши рецепты", 20, true); title.setGravity(Gravity.CENTER); empty.addView(title);
        Ui.space(empty, 8); TextView hint = Ui.muted(this, "Добавьте фотографию блюда, ингредиенты и шаги приготовления.", 15); hint.setGravity(Gravity.CENTER); empty.addView(hint);
        Ui.space(empty, 18); com.google.android.material.button.MaterialButton first = Ui.button(this, "Добавить первый рецепт"); empty.addView(first);
        first.setOnClickListener(v -> openEditor());
        FrameLayout.LayoutParams emptyParams = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER); content.addView(empty, emptyParams);
        ExtendedFloatingActionButton add = new ExtendedFloatingActionButton(this); add.setText("Новый рецепт"); add.setIconResource(android.R.drawable.ic_input_add);
        add.setTextColor(0xffffffff); add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.ACCENT));
        FrameLayout.LayoutParams fab = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.END);
        fab.setMargins(0, 0, Ui.dp(this, 20), Ui.dp(this, 20)); frame.addView(add, fab); add.setOnClickListener(v -> openEditor());
        setContentView(frame);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { showFiltered(); }
            public void afterTextChanged(Editable s) { }
        });
        if (state != null) { scrollState = state.getParcelable("scroll"); search.setText(state.getString("query", "")); }
    }
    private void setGridMode(boolean value) {
        gridMode = value; getPreferences(MODE_PRIVATE).edit().putBoolean("grid", value).apply();
        int position = ((LinearLayoutManager)list.getLayoutManager()).findFirstVisibleItemPosition();
        list.setLayoutManager(value ? new GridLayoutManager(this, 2) : new LinearLayoutManager(this));
        adapter.setGridMode(value); layoutToggle.setText(value ? "Список" : "Сетка");
        list.getLayoutManager().scrollToPosition(Math.max(0, position));
    }
    private void openEditor() { startActivity(new Intent(this, EditorActivity.class)); }
    private void showFiltered() {
        String q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe r : all) if (r.title.toLowerCase(Locale.ROOT).contains(q) || r.category.toLowerCase(Locale.ROOT).contains(q)) filtered.add(r);
        adapter.submitList(filtered, () -> { if (scrollState != null) { list.getLayoutManager().onRestoreInstanceState(scrollState); scrollState = null; } });
        count.setText(filtered.size() + " из " + all.size());
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView)empty.getChildAt(2)).setText(all.isEmpty() ? "Здесь появятся ваши рецепты" : "Ничего не найдено");
        ((TextView)empty.getChildAt(4)).setText(all.isEmpty() ? "Добавьте фотографию блюда, ингредиенты и шаги приготовления." : "Попробуйте изменить поисковый запрос.");
        empty.getChildAt(6).setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override protected void onResume() {
        super.onResume();
        AppGraph.IO.execute(() -> { List<Recipe> recipes = AppGraph.actions(this).all(); runOnUiThread(() -> { all = recipes; showFiltered(); }); });
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out); out.putParcelable("scroll", list.getLayoutManager().onSaveInstanceState()); out.putString("query", search.getText().toString());
    }
}
