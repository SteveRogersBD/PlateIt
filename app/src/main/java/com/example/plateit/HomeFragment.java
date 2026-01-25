package com.example.plateit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plateit.R;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rvRecipes = view.findViewById(R.id.rvRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Mock Data
        List<RecipeVideo> mockData = new ArrayList<>();
        mockData.add(new RecipeVideo("15-Minute Creamy Pasta", "15 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Crispy Air Fryer Chicken", "25 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Ultimate Chocolate Cake", "1 hr", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Spicy Garlic Noodles", "10 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("Healthy Avocado Toast", "5 min", R.drawable.ic_launcher_background));
        mockData.add(new RecipeVideo("One Pan Salmon & Veggies", "20 min", R.drawable.ic_launcher_background));

        VideoAdapter adapter = new VideoAdapter(mockData);
        rvRecipes.setAdapter(adapter);

        return view;
    }
}
