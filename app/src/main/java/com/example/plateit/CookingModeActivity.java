package com.example.plateit;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.plateit.adapters.CookingStepsAdapter;
import java.util.List;
import java.util.ArrayList;

public class CookingModeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvStepProgress;
    private List<String> steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_mode);

        // Get Steps from Intent
        steps = getIntent().getStringArrayListExtra("steps_list");
        if (steps == null)
            steps = new ArrayList<>();

        viewPager = findViewById(R.id.viewPagerSteps);
        progressBar = findViewById(R.id.progressBar);
        tvStepProgress = findViewById(R.id.tvStepProgress);
        View btnPrevious = findViewById(R.id.btnPrevious);
        View btnNext = findViewById(R.id.btnNext);
        View btnClose = findViewById(R.id.btnClose);

        // Setup ViewPager
        CookingStepsAdapter adapter = new CookingStepsAdapter(steps);
        viewPager.setAdapter(adapter);

        // Add Page Transformer for Animation
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        // Initialize Progress
        updateProgress(0);

        // Listeners
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < steps.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                // Finished
                finish(); // Or show "Bon Appetit" dialog
            }
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void updateProgress(int position) {
        int total = steps.size();
        int current = position + 1;

        tvStepProgress.setText("Step " + current + " of " + total);

        // Calculate progress percentage
        int progress = (int) ((current / (float) total) * 100);
        progressBar.setProgress(progress);
    }

    // Animation Class
    public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                view.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                view.setAlpha(0f);
            }
        }
    }
}
