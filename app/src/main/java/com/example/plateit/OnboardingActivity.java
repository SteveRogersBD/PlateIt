package com.example.plateit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext, btnSkip;
    private View dotsIndicator;
    private ImageView dot1, dot2, dot3;
    private com.example.plateit.utils.SessionManager sessionManager;

    private List<OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        sessionManager = new com.example.plateit.utils.SessionManager(this);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        // Setup onboarding data
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.slide1,
                "Cook from Anywhere",
                "Extract recipes instantly from YouTube, Instagram, blogs, or any website with just a link."));
        onboardingItems.add(new OnboardingItem(
                R.drawable.slide2,
                "Shop Your Pantry",
                "Scan ingredients with your camera or add them manually to find recipes you can cook right now."));
        onboardingItems.add(new OnboardingItem(
                R.drawable.slide3,
                "Your AI Sous Chef",
                "Get step-by-step voice guidance, ask questions, and show your cooking for real-time advice."));

        // Setup ViewPager
        OnboardingAdapter adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        // Page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);

                // Change button text on last page
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        // Button listeners
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < onboardingItems.size() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());

        // Initial dot state
        updateDots(0);
    }

    private void updateDots(int position) {
        int activeColor = getResources().getColor(R.color.tech_black, null);
        int inactiveColor = getResources().getColor(R.color.gray_400, null);

        dot1.setColorFilter(position == 0 ? activeColor : inactiveColor);
        dot2.setColorFilter(position == 1 ? activeColor : inactiveColor);
        dot3.setColorFilter(position == 2 ? activeColor : inactiveColor);
    }

    private void finishOnboarding() {
        sessionManager.setOnboardingCompleted();

        // Navigate to SignIn
        Intent intent = new Intent(OnboardingActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    // Onboarding Item Model
    static class OnboardingItem {
        int imageResId;
        String title;
        String description;

        OnboardingItem(int imageResId, String title, String description) {
            this.imageResId = imageResId;
            this.title = title;
            this.description = description;
        }
    }

    // ViewPager Adapter
    static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
        private List<OnboardingItem> items;

        OnboardingAdapter(List<OnboardingItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding, parent, false);
            return new OnboardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            OnboardingItem item = items.get(position);
            holder.imgOnboarding.setImageResource(item.imageResId);
            holder.tvTitle.setText(item.title);
            holder.tvDescription.setText(item.description);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OnboardingViewHolder extends RecyclerView.ViewHolder {
            ImageView imgOnboarding;
            TextView tvTitle, tvDescription;

            OnboardingViewHolder(View itemView) {
                super(itemView);
                imgOnboarding = itemView.findViewById(R.id.imgOnboarding);
                tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
                tvDescription = itemView.findViewById(R.id.tvOnboardingDescription);
            }
        }
    }
}
