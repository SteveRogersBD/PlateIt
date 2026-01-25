package com.example.plateit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.plateit.R;
import com.google.android.material.textfield.TextInputEditText;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        ImageView btnEdit = view.findViewById(R.id.btnEditProfile);
        TextView tvChefName = view.findViewById(R.id.tvChefName); // We can update this later

        btnEdit.setOnClickListener(v -> showEditProfileDialog(tvChefName));

        return view;
    }

    private void showEditProfileDialog(TextView tvNameTarget) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText etName = view.findViewById(R.id.etChefName);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);

        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            if (!newName.isEmpty()) {
                tvNameTarget.setText(newName); // Update UI immediately
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
