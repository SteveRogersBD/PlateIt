package com.example.plateit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import com.example.plateit.responses.ChatResponse;
import com.squareup.picasso.Picasso;
import java.util.List;

public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ChatResponse.RecipeCard recipe);
    }

    private List<ChatResponse.RecipeCard> recipes;
    private OnItemClickListener listener;

    public RecipeCardAdapter(List<ChatResponse.RecipeCard> recipes, OnItemClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatResponse.RecipeCard recipe = recipes.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onItemClick(recipe);
        });

        holder.tvTitle.setText(recipe.getTitle());

        // Repurpose Category for metadata if available
        if (recipe.getReadyInMinutes() != null) {
            holder.tvCategory.setText(recipe.getReadyInMinutes() + " mins");
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvCategory;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgBlogThumbnail);
            tvCategory = itemView.findViewById(R.id.tvBlogCategory);
            tvTitle = itemView.findViewById(R.id.tvBlogTitle);
        }
    }
}
