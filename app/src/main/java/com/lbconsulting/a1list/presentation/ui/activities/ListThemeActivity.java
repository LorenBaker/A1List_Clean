package com.lbconsulting.a1list.presentation.ui.activities;


import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.ApplyTextSizeAndMarginsToAllListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.ApplyTextSizeAndMarginsToAllListThemes;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogColorPicker;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogEditListThemeName;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogNumberPicker;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogSelectTheme;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;


public class ListThemeActivity extends AppCompatActivity implements View.OnClickListener,
        ApplyTextSizeAndMarginsToAllListThemes.Callback {

    public static final String ARG_LIST_THEME_JSON = "argListThemeJson";
    public static final String ARG_MODE = "argMode";
    public static final int EDIT_EXISTING_LIST_THEME = 1;
    public static final int CREATE_NEW_LIST_THEME = 2;

    //region Activity Views

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    //@layout/activity_progress_bar
    @Bind(R.id.activityProgressBar)
    View listActivityProgressBar;
    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;

    //@layout/content_list_theme
    @Bind(R.id.llContentListTheme)
    LinearLayout llContentListTheme;

    //@layout/content_list_theme_settings
    @Bind(R.id.llContentListThemeSettings)
    LinearLayout llContentListThemeSettings;
    @Bind(R.id.btnThemeName)
    Button btnThemeName;
    @Bind(R.id.ckIsDefaultTheme)
    CheckBox ckIsDefaultTheme;
    @Bind(R.id.btnStartColor)
    Button btnStartColor;
    @Bind(R.id.btnEndColor)
    Button btnEndColor;
    @Bind(R.id.btnTextSize)
    Button btnTextSize;
    @Bind(R.id.btnTextColor)
    Button btnTextColor;
    @Bind(R.id.btnTextStyle)
    Button btnTextStyle;
    @Bind(R.id.ckItemBackgroundTransparent)
    CheckBox ckItemBackgroundTransparent;
    @Bind(R.id.btnHorizontalMargin)
    Button btnHorizontalMargin;
    @Bind(R.id.btnVerticalMargin)
    Button btnVerticalMargin;

    //@layout/content_cancel_save_buttons
    @Bind(R.id.ckApplyTextSizeAndMarginsToAllListThemes)
    CheckBox ckApplyTextSizeAndMarginsToAllListThemes;
    @Bind(R.id.llCancelNewSave)
    LinearLayout llCancelNewSave;
    @Bind(R.id.btnCancel)
    Button btnCancel;
    @Bind(R.id.btnSaveTheme)
    Button btnSaveTheme;
    //endregion

    private int mMode;
    private ListTheme mListTheme;
    private ListThemeRepository_Impl mListThemeRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_list_theme);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        Bundle args = getIntent().getExtras();

        if (args.containsKey(ARG_LIST_THEME_JSON)) {
            String listThemeJson = args.getString(ARG_LIST_THEME_JSON);
            Gson gson = new Gson();
            mListTheme = gson.fromJson(listThemeJson, ListTheme.class);
            if (mListTheme == null) {
                Timber.e("onCreate(): FAILED to parse json string to ListTheme.");
            }
        } else {
            Timber.e("onCreate(): FAILED to retrieve json string from intent extras.");
        }

        mMode = EDIT_EXISTING_LIST_THEME;
        if (args.containsKey(ARG_MODE)) {
            mMode = args.getInt(ARG_MODE);
        } else {
            Timber.e("onCreate(): FAILED to retrieve mMode.");
        }

        switch (mMode) {
            case EDIT_EXISTING_LIST_THEME:
                btnSaveTheme.setText("Save Theme");
                mToolbar.setTitle("Edit Theme");
                // To prohibit duplicate ListThemes being created in Backendless,
                // make sure that the provided ListTheme has a Backendless ObjectId
                if (mListTheme.getObjectId() == null || mListTheme.getObjectId().isEmpty()) {
                    ListTheme localListTheme = AndroidApplication.getListThemeRepository().retrieveListThemeByUuid(mListTheme.getUuid());
                    if (localListTheme != null && !localListTheme.getObjectId().isEmpty()) {
                        mListTheme.setObjectId(localListTheme.getObjectId());
                    }
                }
                break;

            case CREATE_NEW_LIST_THEME:
                btnSaveTheme.setText("Create Theme");
                mToolbar.setTitle("Create New Theme");
                break;
        }

        mListThemeRepository = AndroidApplication.getListThemeRepository();

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set button OnClickListeners
        for (int i = 0; i < llContentListThemeSettings.getChildCount(); i++) {
            View v = llContentListThemeSettings.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setOnClickListener(this);
            }
        }

        for (int i = 0; i < llCancelNewSave.getChildCount(); i++) {
            View v = llCancelNewSave.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setOnClickListener(this);
            }
        }
    }

    //region Events
    @Subscribe
    public void onEvent(MyEvents.setListThemeName event) {
        mListTheme.setName(event.getName());
        btnThemeName.setText(String.format(getString(R.string.btnThemeName_text), event.getName()));
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeStartColor event) {
        mListTheme.setStartColor(event.getColor());
        updateUI(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeEndColor event) {
        mListTheme.setEndColor(event.getColor());
        updateUI(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeTextColor event) {
        mListTheme.setTextColor(event.getColor());
        updateUI(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeTextSize event) {
        mListTheme.setTextSize(event.getTextSize());
        updateUI(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeHorizontalPadding event) {
        mListTheme.setHorizontalPaddingInDp(event.getHorizontalPadding());
        updateUI(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeVerticalPadding event) {
        mListTheme.setVerticalPaddingInDp(event.getVerticalPadding());
        updateUI(mListTheme);
    }
    //endregion

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
        if (mListTheme != null) {
            updateUI(mListTheme);
        } else {
            Timber.e("onResume(): Unable to display ListTheme. mListTheme is null!");
        }

        if (mMode == CREATE_NEW_LIST_THEME) {
            FragmentManager fm = getSupportFragmentManager();
            Gson gson = new Gson();
            String listThemeJson = gson.toJson(mListTheme);
            dialogEditListThemeName existingListTitleDialog = dialogEditListThemeName.newInstance(listThemeJson);
            existingListTitleDialog.show(fm, "dialogEditListThemeName");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.i("onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_list_theme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_show_themes) {
            FragmentManager fm = getSupportFragmentManager();
            dialogSelectTheme dialog = dialogSelectTheme.newInstance();
            dialog.show(fm, "dialogSelectTheme");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onClick(View v) {

        FragmentManager fm = getSupportFragmentManager();
        dialogColorPicker colorPickerDialog;
        switch (v.getId()) {

            case R.id.btnThemeName:
                Gson gson = new Gson();
                String listThemeJson = gson.toJson(mListTheme);
                dialogEditListThemeName existingListTitleDialog = dialogEditListThemeName.newInstance(listThemeJson);
                existingListTitleDialog.show(fm, "dialogEditListThemeName");
                break;

            case R.id.ckIsDefaultTheme:
                mListTheme.setDefaultTheme(ckIsDefaultTheme.isChecked());
                break;

            case R.id.btnStartColor:
                colorPickerDialog = dialogColorPicker.newInstance(dialogColorPicker.START_COLOR_PICKER,
                        mListTheme.getStartColor());
                colorPickerDialog.show(fm, "dialogStartColorPicker");
                break;

            case R.id.btnEndColor:
                colorPickerDialog = dialogColorPicker.newInstance(dialogColorPicker.END_COLOR_PICKER,
                        mListTheme.getEndColor());
                colorPickerDialog.show(fm, "dialogEndColorPicker");
                break;

            case R.id.btnTextSize:
                dialogNumberPicker numberPickerDialog = dialogNumberPicker.newInstance(dialogNumberPicker.TEXT_SIZE_PICKER,
                        Math.round(mListTheme.getTextSize()));
                numberPickerDialog.show(fm, "dialogNumberPicker");
                break;

            case R.id.btnTextColor:
                colorPickerDialog = dialogColorPicker.newInstance(dialogColorPicker.TEXT_COLOR_PICKER,
                        mListTheme.getTextColor());
                colorPickerDialog.show(fm, "dialogTextColorPicker");
                break;

            case R.id.btnTextStyle:
                mListTheme.setBold(!mListTheme.isBold());
                updateUI(mListTheme);
                break;

            case R.id.ckItemBackgroundTransparent:
                mListTheme.setTransparent(ckItemBackgroundTransparent.isChecked());
                updateUI(mListTheme);
                break;

            case R.id.btnHorizontalMargin:
                numberPickerDialog = dialogNumberPicker.newInstance(dialogNumberPicker.HORIZONTAL_PADDING_PICKER,
                        (int) mListTheme.getHorizontalPaddingInDp());
                numberPickerDialog.show(fm, "dialogNumberPicker");
                break;

            case R.id.btnVerticalMargin:
                numberPickerDialog = dialogNumberPicker.newInstance(dialogNumberPicker.VERTICAL_PADDING_PICKER,
                        (int) mListTheme.getVerticalPaddingInDp());
                numberPickerDialog.show(fm, "dialogNumberPicker");
                break;

            case R.id.btnCancel:
                finish();
                break;

            case R.id.btnSaveTheme:
                saveTheme();
                break;
        }
    }

    private void saveTheme() {

        if (ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
            showProgress("Saving Themes.");
        } else {
            showProgress("Saving Theme.");
        }

        switch (mMode) {
            case EDIT_EXISTING_LIST_THEME:
                mListThemeRepository.updateStorage(mListTheme);
                break;

            case CREATE_NEW_LIST_THEME:
                mListThemeRepository.insertIntoStorage(mListTheme);
                break;
        }

        if (ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
            new ApplyTextSizeAndMarginsToAllListThemes_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, mListTheme).execute();
        }

        finish();
    }


    //region Background Implementation Overrides


//    @Override
//    public void onListThemeInsertedIntoLocalStorage(String successMessage) {
//        Timber.i("onListThemeInsertedIntoLocalStorage(): %s", successMessage);
//        if (!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
//            finish();
//        }
//    }
//
//    @Override
//    public void onListThemeInsertionIntoLocalStorageFailed(String errorMessage) {
//        Timber.e("onListThemeInsertionIntoLocalStorageFailed(): \"%s\"", errorMessage);
//        if (!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
//            finish();
//        }
//    }

//    @Override
//    public void onListThemeUpdated(String message) {
//        Timber.i("onListThemeUpdated(): %s", message);
//        if (!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
//            finish();
//        }
//    }
//
//    @Override
//    public void onListThemeUpdateFailed(String errorMessage) {
//        Timber.e("onListThemeUpdateFailed(): %s.", errorMessage);
//        if (!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
//            finish();
//        }
//    }

    @Override
    public void onTextSizeAndMarginsApplied(String successMessage) {
        Timber.i("onTextSizeAndMarginsApplied(): %s.", successMessage);
        hideProgress();
        finish();
    }

    @Override
    public void onApplyTextSizeAndMarginsFailure(String errorMessage) {
        Timber.e("onTextSizeAndMarginsApplied(): %s.", errorMessage);
        hideProgress();
        finish();
    }
    //endregion

    //region UI updates
    private void updateUI(ListTheme listTheme) {

        Resources res = getResources();

        // get backgroundDrawable and padding values
        Drawable backgroundDrawable = CommonMethods.getBackgroundDrawable(listTheme.getStartColor(), listTheme.getEndColor());
        int horizontalPadding = CommonMethods.convertDpToPixel(listTheme.getHorizontalPaddingInDp());
        int verticalPadding = CommonMethods.convertDpToPixel(listTheme.getVerticalPaddingInDp());

        // set backgrounds
        CommonMethods.setBackgroundDrawable(llContentListTheme, listTheme.getStartColor(), listTheme.getEndColor());
        llCancelNewSave.setBackground(backgroundDrawable);

        // set views' attributes
        for (int i = 0; i < llContentListThemeSettings.getChildCount(); i++) {
            View v = llContentListThemeSettings.getChildAt(i);
            // Note: Switches and CheckBoxes are "Buttons"
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setTextColor(listTheme.getTextColor());
                b.setTextSize(listTheme.getTextSize());
                b.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
                b.setBackground(backgroundDrawable);

                if (listTheme.isBold()) {
                    b.setTypeface(null, Typeface.BOLD);
                } else {
                    b.setTypeface(null, Typeface.NORMAL);
                }
            }
        }

        for (int i = 0; i < llCancelNewSave.getChildCount(); i++) {
            View v = llCancelNewSave.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setTextColor(listTheme.getTextColor());
            }
        }

        ckApplyTextSizeAndMarginsToAllListThemes.setTextColor(listTheme.getTextColor());
        ckApplyTextSizeAndMarginsToAllListThemes.setBackground(backgroundDrawable);

        // show the attributes values in their respective Buttons
        btnThemeName.setText(String.format(getString(R.string.btnThemeName_text),
                listTheme.getName()));
        ckIsDefaultTheme.setChecked(listTheme.isDefaultTheme());

        btnStartColor.setBackgroundColor(listTheme.getStartColor());
        btnEndColor.setBackgroundColor(listTheme.getEndColor());

        btnTextSize.setText(res.getString(R.string.btnTextSize_text,
                (float) listTheme.getTextSize()));

        if (listTheme.isBold()) {
            btnTextStyle.setText(res.getString(R.string.btnTextStyle_text_bold));
        } else {
            btnTextStyle.setText(res.getString(R.string.btnTextStyle_text_normal));
        }

        btnHorizontalMargin.setText(res.getString(R.string.btnHorizontalMargin_text,
                (long) listTheme.getHorizontalPaddingInDp()));
        btnVerticalMargin.setText(res.getString(R.string.btnVerticalMargin_text,
                (long) listTheme.getVerticalPaddingInDp()));

        ckItemBackgroundTransparent.setChecked(listTheme.isTransparent());

    }

    private void showProgress(String waitMessage) {
        Timber.i("showProgress()");
        listActivityProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        llContentListTheme.setVisibility(View.GONE);
    }

    private void hideProgress() {
        Timber.i("hideProgress()");
        listActivityProgressBar.setVisibility(View.GONE);
        llContentListTheme.setVisibility(View.VISIBLE);

    }
    //endregion
}

