package com.example.food.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.food.Fragments.AllFragment;
import com.example.food.Fragments.MainDishesFragment;
import com.example.food.Fragments.VegetarianFragment;
import com.example.food.Fragments.BeveragesFragment;
import com.example.food.Fragments.SnacksFragment;

public class FoodPagerAdapter extends FragmentStateAdapter {

    public FoodPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AllFragment();
            case 1: return new MainDishesFragment();
            case 2: return new VegetarianFragment();
            case 3: return new BeveragesFragment();
            case 4: return new SnacksFragment();
            default: return new AllFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // tổng số Fragment
    }
}
