package com.example.lab4.presentation.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab4.presentation.common.AppGraph;
import com.example.lab4.presentation.common.Ui;
import com.example.lab4.presentation.detail.DetailActivity;
import com.example.lab4.presentation.editor.EditorActivity;

public class CatalogActivity extends AppCompatActivity {
    private RecipeAdapter adapter;
    private RecyclerView list;
    private TextView empty;
    private android.os.Parcelable scrollState;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = Ui.column(this);
        root.addView(Ui.text(this, "Книга рецептов", 30, true));
        root.addView(Ui.text(this, "Ваши любимые блюда в одном месте", 16, false));
        Button add = Ui.button(this, "Добавить рецепт"); root.addView(add);
        add.setOnClickListener(v -> startActivity(new Intent(this, EditorActivity.class)));
        empty = Ui.text(this, "Пока нет рецептов. Добавьте первый с фотографией блюда.", 18, false);
        root.addView(empty);
        list = new RecyclerView(this); list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(id -> startActivity(new Intent(this, DetailActivity.class).putExtra("id", id)));
        list.setAdapter(adapter); root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        if (state != null) scrollState = state.getParcelable("scroll");
    }
    @Override protected void onResume() {
        super.onResume();
        AppGraph.IO.execute(() -> {
            java.util.List<com.example.lab4.domain.model.Recipe> recipes = AppGraph.actions(this).all();
            runOnUiThread(() -> {
                adapter.submitList(recipes, () -> {
                    if (scrollState != null) { list.getLayoutManager().onRestoreInstanceState(scrollState); scrollState = null; }
                });
                empty.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable("scroll", list.getLayoutManager().onSaveInstanceState());
    }
}
