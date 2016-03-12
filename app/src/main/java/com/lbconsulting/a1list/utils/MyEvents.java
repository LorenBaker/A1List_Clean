package com.lbconsulting.a1list.utils;

import com.lbconsulting.a1list.domain.model.ListTheme;

/**
 * EventBus events.
 */
public class MyEvents {

    public static class createNewListTitle {
        private final String mName;
        private final boolean mShowProgress;

        public createNewListTitle(String listTitleName, boolean showProgress) {
            mName = listTitleName;
            mShowProgress = showProgress;
        }

        public String getName() {
            return mName;
        }

        public boolean showProgress() {
            return mShowProgress;
        }
    }

    public static class setListTitleName {
        private final String mName;

        public setListTitleName(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }


    //region ListTheme Events
    public static class setListThemeName {
        private final String mName;

        public setListThemeName(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    public static class setListThemeStartColor {
        private final int mColor;

        public setListThemeStartColor(int color) {
            mColor = color;
        }

        public int getColor() {
            return mColor;
        }
    }

    public static class setListThemeEndColor {
        private final int mColor;

        public setListThemeEndColor(int color) {
            mColor = color;
        }

        public int getColor() {
            return mColor;
        }
    }

    public static class setListThemeTextColor {
        private final int mColor;

        public setListThemeTextColor(int color) {
            mColor = color;
        }

        public int getColor() {
            return mColor;
        }
    }

    public static class setListThemeTextSize {
        private final int mSelectedTextSize;

        public setListThemeTextSize(int selectedTextSize) {
            mSelectedTextSize = selectedTextSize;
        }

        public float getTextSize() {
            return (float) mSelectedTextSize;
        }
    }

    public static class setListThemeHorizontalPadding {
        private final int mHorizontalPadding;

        public setListThemeHorizontalPadding(int selectedHorizontalPadding) {
            mHorizontalPadding = selectedHorizontalPadding;
        }

        public int getHorizontalPadding() {
            return mHorizontalPadding;
        }
    }

    public static class setListThemeVerticalPadding {
        private final int mVerticalPadding;

        public setListThemeVerticalPadding(int selectedVerticalPadding) {
            mVerticalPadding = selectedVerticalPadding;
        }

        public int getVerticalPadding() {
            return mVerticalPadding;
        }
    }


    public static class updateListTitleActivityUI {
        private ListTheme mSelectedListTheme;

        public updateListTitleActivityUI(ListTheme listTheme) {
            mSelectedListTheme = listTheme;
        }

        public ListTheme getSelectedListTheme() {
            return mSelectedListTheme;
        }
    }
    //endregion


}


