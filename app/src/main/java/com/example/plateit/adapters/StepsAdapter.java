package com.example.plateit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {

    private List<com.example.plateit.models.RecipeStep> steps;

    public StepsAdapter(List<com.example.plateit.models.RecipeStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.example.plateit.models.RecipeStep step = steps.get(position);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvDescription.setText(step.getInstruction());
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvStepNumber);
            tvDescription = itemView.findViewById(R.id.tvStepDescription);
        }
    }
}
