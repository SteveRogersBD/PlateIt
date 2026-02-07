package com.example.plateit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.squareup.picasso.Picasso;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<BlogItem> blogList;
    private OnBlogClickListener listener;

    public interface OnBlogClickListener {
        void onBlogClick(BlogItem blog);
    }

    public BlogAdapter(List<BlogItem> blogList, OnBlogClickListener listener) {
        this.blogList = blogList;
        this.listener = listener;
    }

    public void updateData(List<BlogItem> newBlogs) {
        this.blogList = newBlogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog_card, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogItem item = blogList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getSource()); // Use Source as Category

        if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
            Picasso.get().load(item.getThumbnail()).into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onBlogClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle, tvCategory;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgBlogThumbnail);
            tvTitle = itemView.findViewById(R.id.tvBlogTitle);
            tvCategory = itemView.findViewById(R.id.tvBlogCategory);
        }
    }
}
