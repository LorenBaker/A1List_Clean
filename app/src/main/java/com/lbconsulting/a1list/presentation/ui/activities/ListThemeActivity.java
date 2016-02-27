package com.lbconsulting.a1list.presentation.ui.activities;


import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.impl.ApplyTextSizeAndMarginsToAllListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.impl.UpdateListTheme_InBackground;
import com.lbconsulting.a1list.domain.interactors.interfaces.ApplyTextSizeAndMarginsToAllListThemes_Interactor;
import com.lbconsulting.a1list.domain.interactors.interfaces.UpdateListTheme_Interactor;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repository.ListThemeRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.impl.ListThemeActivityPresenter_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListThemeActivityPresenter;
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
        ListThemeActivityPresenter.ListThemeActivityView, UpdateListTheme_Interactor.Callback,
        ApplyTextSizeAndMarginsToAllListThemes_Interactor.Callback{

    public static final String ARG_LIST_THEME_UUID = "argListThemeUuid";
    public static final String ARG_MODE = "argMode";
    public static final int EDIT_EXISTING_LIST_THEME = 1;
    public static final int CREATE_NEW_LIST_THEME = 2;
    private static ListThemeActivityPresenter_Impl mListThemePresenter;
    @Bind(R.id.llListTheme)
    CoordinatorLayout llListTheme;

    @Bind(R.id.llContentListTheme)
    LinearLayout llContentListTheme;
    @Bind(R.id.llCancelNewSave)
    LinearLayout llCancelNewSave;
    @Bind(R.id.btnThemeName)
    Button btnThemeName;
    @Bind(R.id.ckIsDefaultTheme)
    CheckBox ckIsDefaultTheme;
    @Bind(R.id.btnStartColor)
    Button btnStartColor;
    @Bind(R.id.btnEndColor)
    Button btnEndColor;

    @Bind((R.id.btnTextColor))
    Button btnTextColor;
    @Bind(R.id.btnTextSize)
    Button btnTextSize;

    @Bind(R.id.btnTextStyle)
    Button btnTextStyle;
    @Bind(R.id.ckItemBackgroundTransparent)
    CheckBox ckItemBackgroundTransparent;
    @Bind(R.id.btnHorizontalMargin)
    Button btnHorizontalMargin;
    @Bind(R.id.btnVerticalMargin)
    Button btnVerticalMargin;

    @Bind(R.id.ckApplyTextSizeAndMarginsToAllListThemes)
    CheckBox ckApplyTextSizeAndMarginsToAllListThemes;
    @Bind(R.id.btnCancel)
    Button btnCancel;
    @Bind(R.id.btnSaveTheme)
    Button btnSaveTheme;


    @Bind(R.id.listThemeActivityContent)
    View listThemeActivityContent;

    @Bind(R.id.activityProgressBar)
    ProgressBar mProgressBar;

    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;


    @Bind(R.id.toolbar)
    Toolbar mToolbar;


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
        String originalListThemeUuid = null;
        if (args.containsKey(ARG_LIST_THEME_UUID)) {
            originalListThemeUuid = args.getString(ARG_LIST_THEME_UUID);
        }
        mListThemeRepository = new ListThemeRepository_Impl(this);

        mListThemePresenter = new ListThemeActivityPresenter_Impl(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListThemeRepository, originalListThemeUuid);

        mMode = EDIT_EXISTING_LIST_THEME;
        if (args.containsKey(ARG_MODE)) {
            mMode = args.getInt(ARG_MODE);
        }

        switch (mMode) {
            case EDIT_EXISTING_LIST_THEME:
                btnSaveTheme.setText("Save Theme");
                mToolbar.setTitle("Edit Theme");
                break;

            case CREATE_NEW_LIST_THEME:
                btnSaveTheme.setText("Create Theme");
                mToolbar.setTitle("Create New Theme");
                break;
        }

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set button OnClickListeners
        for (int i = 0; i < llContentListTheme.getChildCount(); i++) {
            View v = llContentListTheme.getChildAt(i);
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

    //region onEvent
    @Subscribe
    public void onEvent(MyEvents.setListThemeName event) {
        mListTheme.setName(event.getName());
        btnThemeName.setText(String.format(getString(R.string.btnThemeName_text), event.getName()));
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeStartColor event) {
        mListTheme.setStartColor(event.getColor());
        displayTheme(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeEndColor event) {
        mListTheme.setEndColor(event.getColor());
        displayTheme(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeTextColor event) {
        mListTheme.setTextColor(event.getColor());
        displayTheme(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeTextSize event) {
        mListTheme.setTextSize(event.getTextSize());
        displayTheme(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeHorizontalPadding event) {
        mListTheme.setHorizontalPaddingInDp(event.getHorizontalPadding());
        displayTheme(mListTheme);
    }

    @Subscribe
    public void onEvent(MyEvents.setListThemeVerticalPadding event) {
        mListTheme.setVerticalPaddingInDp(event.getVerticalPadding());
        displayTheme(mListTheme);
    }
    //endregion

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
        mListThemePresenter.resume();
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
                dialogEditListThemeName editListAttributesNameDialog
                        = dialogEditListThemeName.newInstance(mListTheme.getName(), mListTheme.getUuid());
                editListAttributesNameDialog.show(fm, "dialogEditListThemeName");
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
                displayTheme(mListTheme);
                break;

            case R.id.ckItemBackgroundTransparent:
                mListTheme.setTransparent(ckItemBackgroundTransparent.isChecked());
                displayTheme(mListTheme);
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
//                finish();
                break;
        }
    }

    private void saveTheme() {
        if (ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
            showProgress("Saving Themes.");
        }else{
            showProgress("Saving Theme.");
        }

        new UpdateListTheme_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(), this, mListThemeRepository, mListTheme).execute();

        if (ckApplyTextSizeAndMarginsToAllListThemes.isChecked()) {
            new ApplyTextSizeAndMarginsToAllListThemes_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, mListThemeRepository, mListTheme).execute();
        }
    }

    @Override
    public void displayRetrievedListTheme(ListTheme listTheme) {
        Timber.i("displayRetrievedListTheme()");
        mListTheme = listTheme;
        displayTheme(listTheme);
        if (mMode == CREATE_NEW_LIST_THEME) {
            // TODO: show enter them name dialog
        }
    }

    @Override
    public void onListThemeUpdated(String message) {
        Timber.i("onListThemeUpdated(): %s", message);
        if(!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()){
            finish();
        }
    }

    @Override
    public void onListThemeUpdateFailed(String errorMessage) {
        Timber.e("onListThemeUpdateFailed(): %s.", errorMessage);
        if(!ckApplyTextSizeAndMarginsToAllListThemes.isChecked()){
            finish();
        }
    }

    private void displayTheme(ListTheme listTheme) {

        Resources res = getResources();

        // set backgrounds
        Drawable backgroundDrawable = listTheme.getBackgroundDrawable();
        llListTheme.setBackground(backgroundDrawable);
        llCancelNewSave.setBackground(backgroundDrawable);

        if (listTheme.isTransparent()) {
            btnTextColor.setBackgroundColor(Color.TRANSPARENT);
            btnTextSize.setBackgroundColor(Color.TRANSPARENT);
            btnTextStyle.setBackgroundColor(Color.TRANSPARENT);
        } else {
            btnTextColor.setBackground(backgroundDrawable);
            btnTextSize.setBackground(backgroundDrawable);
            btnTextStyle.setBackground(backgroundDrawable);
        }

        // set views' text color
        for (int i = 0; i < llContentListTheme.getChildCount(); i++) {
            View v = llContentListTheme.getChildAt(i);
            // Note: Switches and CheckBoxes are "Buttons"
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setTextColor(listTheme.getTextColor());
                if (listTheme.isTransparent()) {
                    b.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    b.setBackground(backgroundDrawable);
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

        if (listTheme.isBold()) {
            btnTextStyle.setText(R.string.btnTextStyle_text_bold);
            btnTextColor.setTypeface(null, Typeface.BOLD);
            btnTextSize.setTypeface(null, Typeface.BOLD);
            btnTextStyle.setTypeface(null, Typeface.BOLD);
        } else {
            btnTextStyle.setText(R.string.btnTextStyle_text_normal);
            btnTextColor.setTypeface(null, Typeface.NORMAL);
            btnTextSize.setTypeface(null, Typeface.NORMAL);
            btnTextStyle.setTypeface(null, Typeface.NORMAL);
        }


        // set Text Size
        btnTextSize.setText(res.getString(R.string.btnTextSize_text, listTheme.getTextSize()));
        btnTextColor.setTextSize(listTheme.getTextSize());
        btnTextStyle.setTextSize(listTheme.getTextSize());
        btnTextSize.setTextSize(listTheme.getTextSize());

        // set transparent check box
        ckItemBackgroundTransparent.setChecked(listTheme.isTransparent());


        // set margins
        btnHorizontalMargin.setText(res.getString(R.string.btnHorizontalMargin_text,
                (long) listTheme.getHorizontalPaddingInDp()));
        btnVerticalMargin.setText(res.getString(R.string.btnVerticalMargin_text,
                (long) listTheme.getVerticalPaddingInDp()));
        int horizontalPadding = CommonMethods.convertDpToPixel(listTheme.getHorizontalPaddingInDp());
        int verticalPadding = CommonMethods.convertDpToPixel(listTheme.getVerticalPaddingInDp());
        btnTextColor.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        btnTextStyle.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        btnTextSize.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

    }

    @Override
    public void showProgress(String waitMessage) {
        Timber.i("showProgress()");
        mProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        tvProgressBarMessage.setVisibility(View.VISIBLE);
        listThemeActivityContent.setVisibility(View.GONE);
    }

    @Override
    public void hideProgress() {
        Timber.i("hideProgress()");
        mProgressBar.setVisibility(View.GONE);
        tvProgressBarMessage.setVisibility(View.GONE);
        listThemeActivityContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        Timber.e("showError(): %s.", message);
    }


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
}

