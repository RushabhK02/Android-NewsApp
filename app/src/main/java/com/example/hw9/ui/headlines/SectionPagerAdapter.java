package com.example.hw9.ui.headlines;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.hw9.model.SectionArticleItem;

public class SectionPagerAdapter extends FragmentStatePagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
        public Fragment getItem(int i) {
            SectionObjectFragment fragment = new SectionObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(SectionObjectFragment.ARG_OBJECT, i + 1);
            args.putInt(SectionObjectFragment.ARG_COLUMN_COUNT,1);
            args.putString(fragment.SECTION_NAME, getSection(i));
            fragment.setArguments(args);
            return fragment;
        }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "WORLD";
                case 1: return "BUSINESS";
                case 2: return "POLITICS";
                case 3: return "SPORTS";
                case 4: return "TECHNOLOGY";
                case 5: return "SCIENCE";
                default:return "OTHER";
            }
        }

        public String getSection(int position) {
            switch (position) {
                case 0: return "WORLD";
                case 1: return "BUSINESS";
                case 2: return "POLITICS";
                case 3: return "SPORTS";
                case 4: return "TECHNOLOGY";
                case 5: return "SCIENCE";
                default:return "OTHER";
            }
        }
}
