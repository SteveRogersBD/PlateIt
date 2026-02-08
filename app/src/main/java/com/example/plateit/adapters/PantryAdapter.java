package com.example.plateit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import com.example.plateit.db.PantryItem;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private List<PantryItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(PantryItem item);
    }

    public PantryAdapter(List<PantryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<PantryItem> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvAmount.setText(item.amount);

        // Simple date format or "time ago" logic
        long diff = System.currentTimeMillis() - item.dateAdded;
        long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
        if (days == 0) {
            holder.tvDate.setText("Added today");
        } else if (days == 1) {
            holder.tvDate.setText("Added yesterday");
        } else {
            holder.tvDate.setText("Added " + days + " days ago");
        }

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            com.squareup.picasso.Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .fit()
                    .into(holder.imgPantryItem);
        } else {
            holder.imgPantryItem.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvDate;
        ImageButton btnDelete;
        android.widget.ImageView imgPantryItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgPantryItem = itemView.findViewById(R.id.imgPantryItem);
        }
    }
}
