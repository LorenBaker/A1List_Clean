package com.lbconsulting.a1list.utils;

/**
 * EventBus events.
 */
public class MyEvents {



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

//    public static class setListTheme {
//        private final String mAttributeUuid;
//
//        public setListTheme(String attributeUuid) {
//            mAttributeUuid = attributeUuid;
//        }
//
//        public String getAttributeUuid() {
//            return mAttributeUuid;
//        }
//    }
}


