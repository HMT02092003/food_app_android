package com.example.food.Activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.example.food.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {
    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Đặt màu cho Status Bar
        getWindow().setStatusBarColor(Color.parseColor("#FFE4B5"));

        // --- Đảm bảo các View không hiển thị lúc ban đầu ---
        // Đặt alpha về 0f (hoàn toàn trong suốt) cho tất cả các View sẽ được animate
        binding.imageView2.setAlpha(0f);
        binding.textView2.setAlpha(0f);
        binding.bottomRightDecoration.setAlpha(0f);
        binding.topLeftDecoration.setAlpha(0f);

        // Đặt vị trí ban đầu của các View lên cao để chuẩn bị rơi
        float startYOffset = -1000f; // Bắt đầu từ 1000 pixels phía trên màn hình
        binding.bottomRightDecoration.setTranslationY(startYOffset);
        binding.imageView2.setTranslationY(startYOffset);
        binding.topLeftDecoration.setTranslationY(startYOffset);

        // Thời gian cho mỗi animation rơi
        long dropDuration = 800; // 0.8 giây cho mỗi View để rơi

        // 1. Tạo animation cho bottomRightDecoration
        ObjectAnimator animBottomRightDrop = ObjectAnimator.ofFloat(binding.bottomRightDecoration, "translationY", startYOffset, 0f);
        animBottomRightDrop.setDuration(dropDuration);
        ObjectAnimator animBottomRightFadeIn = ObjectAnimator.ofFloat(binding.bottomRightDecoration, "alpha", 0f, 1f);
        animBottomRightFadeIn.setDuration(dropDuration);

        // 2. Tạo animation cho logo (imageView2)
        ObjectAnimator animLogoDrop = ObjectAnimator.ofFloat(binding.imageView2, "translationY", startYOffset, 0f);
        animLogoDrop.setDuration(dropDuration);
        ObjectAnimator animLogoFadeIn = ObjectAnimator.ofFloat(binding.imageView2, "alpha", 0f, 1f);
        animLogoFadeIn.setDuration(dropDuration);

        // 3. Tạo animation cho topLeftDecoration
        ObjectAnimator animTopLeftDrop = ObjectAnimator.ofFloat(binding.topLeftDecoration, "translationY", startYOffset, 0f);
        animTopLeftDrop.setDuration(dropDuration);
        ObjectAnimator animTopLeftFadeIn = ObjectAnimator.ofFloat(binding.topLeftDecoration, "alpha", 0f, 1f);
        animTopLeftFadeIn.setDuration(dropDuration);

        // Tạo animation cho text "VIETNAMESE FOOD" (chỉ mờ dần xuất hiện, không rơi)
        ObjectAnimator animTextFadeIn = ObjectAnimator.ofFloat(binding.textView2, "alpha", 0f, 1f);
        animTextFadeIn.setDuration(500); // Mờ dần trong 0.5 giây

        // Tạo AnimatorSet để chạy các animation theo trình tự
        AnimatorSet sequentialAnimatorSet = new AnimatorSet();
        sequentialAnimatorSet.playSequentially(
                // Gom các animation rơi và hiện ra cùng lúc cho từng đối tượng
                // bottomRightDecoration rơi và hiện ra
                createTogetherAnimator(animBottomRightDrop, animBottomRightFadeIn),
                // Logo rơi và hiện ra
                createTogetherAnimator(animLogoDrop, animLogoFadeIn),
                // topLeftDecoration rơi và hiện ra
                createTogetherAnimator(animTopLeftDrop, animTopLeftFadeIn),
                // Cuối cùng là text mờ dần hiện ra
                animTextFadeIn
        );

        // Thêm Listener để lắng nghe sự kiện kết thúc của toàn bộ AnimatorSet
        sequentialAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                // Không cần làm gì ở đây vì đã ẩn chúng lúc ban đầu
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // Được gọi khi toàn bộ chuỗi animation kết thúc
                // Chuyển sang LoginActivity
                Intent mainIntent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish(); // Đóng IntroActivity
            }

            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        // Sử dụng Handler để trì hoãn việc bắt đầu animation
        // Animation sẽ bắt đầu sau 0.5 giây (500 mili giây)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sequentialAnimatorSet.start(); // Bắt đầu chuỗi animation
            }
        }, 500); // 0.5 giây delay
    }

    // Helper method để tạo AnimatorSet chạy 2 animation cùng lúc
    private AnimatorSet createTogetherAnimator(ObjectAnimator anim1, ObjectAnimator anim2) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim1, anim2);
        return set;
    }
}