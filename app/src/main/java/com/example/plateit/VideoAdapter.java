package com.example.plateit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<RecipeVideo> videoList;

    public VideoAdapter(List<RecipeVideo> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        RecipeVideo video = videoList.get(position);
        holder.title.setText(video.getTitle());
        holder.time.setText(video.getTime());
        holder.thumbnail.setImageResource(video.getThumbnailResId());
        // For the mock, we can just set a random background color or image if needed,
        // but the resourceId is enough for now.
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;
        ImageView thumbnail;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            time = itemView.findViewById(R.id.videoTime);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);
        }
    }
}
