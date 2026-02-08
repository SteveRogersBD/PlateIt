package com.example.plateit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import java.util.List;

import android.widget.ImageView;
import com.example.plateit.models.RecipeStep;
import com.squareup.picasso.Picasso;

public class CookingStepsAdapter extends RecyclerView.Adapter<CookingStepsAdapter.ViewHolder> {

    private List<RecipeStep> steps;

    public CookingStepsAdapter(List<RecipeStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeStep step = steps.get(position);
        holder.tvNumber.setText(String.valueOf(position + 1));

        // Handle potentially null instruction
        String text = step.getInstruction() != null ? step.getInstruction() : "";
        holder.tvDescription.setText(text);

        // Handle Image
        if (step.getImageUrl() != null && !step.getImageUrl().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            Picasso.get().load(step.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // or any placeholder
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        TextView tvDescription;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvStepNumberBig);
            tvDescription = itemView.findViewById(R.id.tvStepDescriptionBig);
            ivImage = itemView.findViewById(R.id.ivStepImage);
        }
    }
}
