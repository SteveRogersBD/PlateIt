package com.example.plateit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import com.example.plateit.models.Ingredient;
import com.squareup.picasso.Picasso;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private List<Ingredient> ingredients;

    public IngredientsAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.tvName.setText(ingredient.getName());
        holder.tvAmount.setText(ingredient.getAmount());

        android.util.Log.d("IngredientsAdapter",
                "Loading image for " + ingredient.getName() + ": " + ingredient.getImageUrl());

        if (ingredient.getImageUrl() != null && !ingredient.getImageUrl().isEmpty()) {
            Picasso.get().setLoggingEnabled(true);
            Picasso.get()
                    .load(ingredient.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_camera) // LOADING
                    .error(android.R.drawable.stat_notify_error) // ERROR (Download failed)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_delete); // NULL URL
        }
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivIngredient);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvAmount = itemView.findViewById(R.id.tvIngredientAmount);
        }
    }
}
