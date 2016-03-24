package com.lbconsulting.a1list.presentation.ui.activities;


import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.RetrieveAllListThemes_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTheme.interactors.RetrieveAllListThemes;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.InsertNewListTitle_InBackground;
import com.lbconsulting.a1list.domain.interactors.listTitle.impl.UpdateListTitle_InBackground;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.AppSettingsRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.adapters.ListThemeSpinnerArrayAdapter;
import com.lbconsulting.a1list.presentation.ui.dialogs.dialogEditListTitleName;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;


public class ListTitleActivity extends AppCompatActivity implements View.OnClickListener,
//        ListThemesPresenter.ListThemeView,
        RetrieveAllListThemes.Callback,
        UpdateListTitle_InBackground.Callback,
        InsertNewListTitle_InBackground.Callback {

    public static final String ARG_LIST_TITLE_JSON = "argListTitleJson";
    public static final String ARG_MODE = "argMode";
    public static final int EDIT_EXISTING_LIST_TITLE = 10;
    public static final int CREATE_NEW_LIST_TITLE = 20;

    //region Activity Views

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    //@layout/activity_progress_bar
    @Bind(R.id.activityProgressBar)
    View listActivityProgressBar;
    @Bind(R.id.tvActivityProgressBarMessage)
    TextView tvProgressBarMessage;

    //@layout/content_list_theme
    @Bind((R.id.listTitlesActivityContent))
    View listTitlesActivityContent;

    //@layout/content_list_title_settings
    @Bind(R.id.llContentListTitleSettings)
    LinearLayout llContentListTitleSettings;
    @Bind(R.id.btnListTitleName)
    Button btnListTitleName;
    @Bind(R.id.spnListTitles)
    Spinner spnListTitles;
    @Bind(R.id.groupListSorting)
    RadioGroup groupListSorting;
    @Bind((R.id.rbSortAlphabetically))
    RadioButton rbSortAlphabetically;
    @Bind((R.id.rbSortManually))
    RadioButton rbSortManually;
    @Bind(R.id.groupListPrivate)
    RadioGroup groupListPrivate;
    @Bind((R.id.rbPrivateList))
    RadioButton rbPrivateList;
    @Bind((R.id.rbPublicList))
    RadioButton rbPublicList;

    //@layout/content_cancel_save_buttons
    @Bind(R.id.ckApplyTextSizeAndMarginsToAllListThemes)
    CheckBox ckApplyTextSizeAndMarginsToAllListThemes;
    @Bind(R.id.llCancelNewSave)
    LinearLayout llCancelNewSave;
    @Bind(R.id.btnCancel)
    Button btnCancel;
    @Bind(R.id.btnSaveTheme)
    Button btnSaveList;
    //endregion

    private int mMode;
    private ListTitle mListTitle;
    private AppSettingsRepository_Impl mAppSettingsRepository;
    private ListThemeRepository_Impl mListThemeRepository;
    private ListTitleRepository_Impl mListTitleRepository;
    //    private ListThemesPresenter_Impl mListThemesPresenter;
    private ListThemeSpinnerArrayAdapter mListThemeSpinnerArrayAdapter;


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
        setContentView(R.layout.activity_list_title);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        Bundle args = getIntent().getExtras();

        if (args.containsKey(ARG_LIST_TITLE_JSON)) {
            String listTitleJson = args.getString(ARG_LIST_TITLE_JSON);
            Gson gson = new Gson();
            mListTitle = gson.fromJson(listTitleJson, ListTitle.class);
            if (mListTitle == null) {
                Timber.e("onCreate(): FAILED to parse json string to ListTitle.");
            }
        } else {
            Timber.e("onCreate(): FAILED to retrieve json string from intent extras.");
        }

        mMode = EDIT_EXISTING_LIST_TITLE;
        if (args.containsKey(ARG_MODE)) {
            mMode = args.getInt(ARG_MODE);
        } else {
            Timber.e("onCreate(): FAILED to retrieve mMode.");
        }

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            switch (mMode) {
                case EDIT_EXISTING_LIST_TITLE:
                    actionBar.setTitle(String.format("Edit \"%s\"", mListTitle.getName()));
                    break;

                case CREATE_NEW_LIST_TITLE:
                    actionBar.setTitle("Create New List");
                    break;
            }
        }

        switch (mMode) {
            case EDIT_EXISTING_LIST_TITLE:
                btnSaveList.setText("Save Changes");
                mToolbar.setTitle(String.format("Edit \"%s\"", mListTitle.getName()));
                break;

            case CREATE_NEW_LIST_TITLE:
                btnSaveList.setText("Create List");
                mToolbar.setTitle("Create New List");
                break;
        }

        mAppSettingsRepository = new AppSettingsRepository_Impl(this);
        mListThemeRepository = new ListThemeRepository_Impl(this);
        mListTitleRepository = new ListTitleRepository_Impl(this,mAppSettingsRepository ,mListThemeRepository);
//        mListThemesPresenter = new ListThemesPresenter_Impl(ThreadExecutor.getInstance(),
//                MainThreadImpl.getInstance(), this, mListThemeRepository);

        mListThemeSpinnerArrayAdapter = new ListThemeSpinnerArrayAdapter(this, spnListTitles);
        spnListTitles.setAdapter(mListThemeSpinnerArrayAdapter);


        // set button OnClickListeners
        for (int i = 0; i < llContentListTitleSettings.getChildCount(); i++) {
            View v = llContentListTitleSettings.getChildAt(i);
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

        ckApplyTextSizeAndMarginsToAllListThemes.setVisibility(View.GONE);
    }

    //region Events
    @Subscribe
    public void onEvent(MyEvents.setListTitleName event) {
        mListTitle.setName(event.getName());
        btnListTitleName.setText(String.format(getString(R.string.btnListTitleName_text), event.getName()));
        updateUI(mListTitle);
    }

    @Subscribe
    public void onEvent(MyEvents.updateListTitleActivityUI event) {
        mListTitle.setListTheme(event.getSelectedListTheme());
        updateUI(mListTitle);
    }


    //endregion

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
        if (mListTitle != null) {
            new RetrieveAllListThemes_InBackground(ThreadExecutor.getInstance(),
                    MainThreadImpl.getInstance(), this, mListThemeRepository).execute();
        } else {
            Timber.e("onResume(): Unable to display ListTitle. mListTitle is null!");
        }

        if (mMode == CREATE_NEW_LIST_TITLE) {
            FragmentManager fm = getSupportFragmentManager();
            Gson gson = new Gson();
            String listTitleJson = gson.toJson(mListTitle);
            dialogEditListTitleName existingListTitleDialog = dialogEditListTitleName.newInstance(listTitleJson);
            existingListTitleDialog.show(fm, "dialogEditListTitleName");
        }
    }

    @Override
    public void onAllListThemesRetrieved(List<ListTheme> listThemes) {
        mListThemeSpinnerArrayAdapter.setData(listThemes);
        mListThemeSpinnerArrayAdapter.notifyDataSetChanged();

        int position = mListThemeSpinnerArrayAdapter.getPosition(mListTitle.getListTheme());
        spnListTitles.setSelection(position);
        updateUI(mListTitle);
    }

    @Override
    public void onRetrievalFailed(String errorMessage) {
        Timber.e("onRetrievalFailed(): %s.", errorMessage);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
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
        switch (v.getId()) {

            case R.id.btnListTitleName:
                Gson gson = new Gson();
                String listTitleJson = gson.toJson(mListTitle);
                dialogEditListTitleName existingListTitleDialog = dialogEditListTitleName.newInstance(listTitleJson);
                existingListTitleDialog.show(fm, "dialogEditListTitleName");
                break;

            case R.id.btnCancel:
                finish();
                break;

            case R.id.btnSaveTheme:
                if (!mListTitle.getName().equals(dialogEditListTitleName.DEFAULT_LIST_TITLE_NAME)) {
                    mListTitle.setSortListItemsAlphabetically(rbSortAlphabetically.isChecked());
                    mListTitle.setListPrivateToThisDevice(rbPrivateList.isChecked());
                    saveListTitle(mListTitle);
                } else {
                    // the list name is the default name
                    String title = "Unable to Save List";
                    String msg = "Please provide a valid list name.";
                    CommonMethods.showOkDialog(this,title,msg);
                }
                break;
        }
    }

    //region Save ListTitle
    private void saveListTitle(ListTitle listTitle) {

        showProgress(String.format("Saving \"%s\"", listTitle.getName()));

        switch (mMode) {
            case EDIT_EXISTING_LIST_TITLE:
                new UpdateListTitle_InBackground(ThreadExecutor.getInstance(),
                        MainThreadImpl.getInstance(), this, mListTitleRepository, listTitle).execute();
                break;

            case CREATE_NEW_LIST_TITLE:
//                Toast.makeText(this, "btnSaveList: CREATE_NEW_LIST_TITLE clicked.", Toast.LENGTH_SHORT).show();
                new InsertNewListTitle_InBackground(ThreadExecutor.getInstance(),
                        MainThreadImpl.getInstance(), this, listTitle, mListTitleRepository,
                        mAppSettingsRepository).execute();
                break;
        }
    }


    @Override
    public void onListTitleInsertedIntoSQLiteDb(String successMessage) {
        Timber.i("onListTitleInsertedIntoSQLiteDb(): %s", successMessage);
        hideProgress();
        finish();
    }

    @Override
    public void onListTitleInsertionIntoSQLiteDbFailed(String errorMessage) {
        Timber.e("onListTitleInsertionIntoSQLiteDbFailed(): %s.", errorMessage);
        hideProgress();
        String title = "";
        CommonMethods.showOkDialog(this, title, errorMessage);
    }

    @Override
    public void onListTitleUpdated(String successMessage) {
        Timber.i("onListTitleUpdated(): %s", successMessage);
        hideProgress();
        finish();
    }

    @Override
    public void onListTitleUpdateFailed(String errorMessage) {
        Timber.e("onListTitleUpdateFailed(): %s.", errorMessage);
        hideProgress();
        String title = "";
        CommonMethods.showOkDialog(this, title, errorMessage);
    }
    //endregion

    //region UI updates
    private void updateUI(ListTitle listTitle) {

//        Resources res = getResources();

        // get backgroundDrawable and padding values
        Drawable backgroundDrawable = CommonMethods.getBackgroundDrawable(
                listTitle.getListTheme().getStartColor(), listTitle.getListTheme().getEndColor());
        int horizontalPadding = CommonMethods.convertDpToPixel(listTitle.getListTheme().getHorizontalPaddingInDp());
        int verticalPadding = CommonMethods.convertDpToPixel(listTitle.getListTheme().getVerticalPaddingInDp());

        // set backgrounds
        CommonMethods.setBackgroundDrawable(listTitlesActivityContent,
                listTitle.getListTheme().getStartColor(), listTitle.getListTheme().getEndColor());
        llCancelNewSave.setBackground(backgroundDrawable);

        // set views' attributes
        for (int i = 0; i < llContentListTitleSettings.getChildCount(); i++) {
            View v = llContentListTitleSettings.getChildAt(i);
            // Note: Switches and CheckBoxes are "Buttons"
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setTextColor(listTitle.getListTheme().getTextColor());
                b.setTextSize(listTitle.getListTheme().getTextSize());
                b.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
                b.setBackground(backgroundDrawable);

                if (listTitle.getListTheme().isBold()) {
                    b.setTypeface(null, Typeface.BOLD);
                } else {
                    b.setTypeface(null, Typeface.NORMAL);
                }
            } else if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(listTitle.getListTheme().getTextColor());
                tv.setTextSize(listTitle.getListTheme().getTextSize());
                tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            }
        }

        for (int i = 0; i < llCancelNewSave.getChildCount(); i++) {
            View v = llCancelNewSave.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                b.setTextColor(listTitle.getListTheme().getTextColor());
            }
        }

        backgroundDrawable = CommonMethods.getBackgroundDrawable(
                listTitle.getListTheme().getStartColor(), listTitle.getListTheme().getEndColor());
        groupListPrivate.setBackground(backgroundDrawable);
        for (int i = 0; i < groupListPrivate.getChildCount(); i++) {
            View v = groupListPrivate.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(listTitle.getListTheme().getTextColor());
                tv.setTextSize(listTitle.getListTheme().getTextSize());
                tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            }
        }

        backgroundDrawable = CommonMethods.getBackgroundDrawable(
                listTitle.getListTheme().getStartColor(), listTitle.getListTheme().getEndColor());
        groupListSorting.setBackground(backgroundDrawable);
        for (int i = 0; i < groupListSorting.getChildCount(); i++) {
            View v = groupListSorting.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(listTitle.getListTheme().getTextColor());
                tv.setTextSize(listTitle.getListTheme().getTextSize());
                tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            }
        }


        if (listTitle.isSortListItemsAlphabetically()) {
            rbSortAlphabetically.setChecked(true);
        } else {
            rbSortManually.setChecked(true);
        }

        if (listTitle.isListPrivateToThisDevice()) {
            rbPrivateList.setChecked(true);
        } else {
            rbPublicList.setChecked(true);
        }

        // show the attributes values in their respective Buttons
        if(listTitle.getName().equals(dialogEditListTitleName.DEFAULT_LIST_TITLE_NAME)){
            btnListTitleName.setText("");
        }else{
            btnListTitleName.setText(listTitle.getName());
        }
    }

    public void showProgress(String waitMessage) {
        Timber.i("showProgress()");
        listActivityProgressBar.setVisibility(View.VISIBLE);
        tvProgressBarMessage.setText(String.format("Please wait ...\n%s", waitMessage));
        listTitlesActivityContent.setVisibility(View.GONE);
    }

    public void hideProgress() {
        Timber.i("hideProgress()");
        listActivityProgressBar.setVisibility(View.GONE);
        listTitlesActivityContent.setVisibility(View.VISIBLE);
    }

//    @Override
//    public void showError(String message) {
//
//    }

//    @Override
//    public void displayAllListThemes(List<ListTheme> allListThemes) {
//        mListThemeSpinnerArrayAdapter.setData(allListThemes);
//        mListThemeSpinnerArrayAdapter.notifyDataSetChanged();
//
//        int position = mListThemeSpinnerArrayAdapter.getmPosition(mListTitle.getListTheme());
//        spnListTitles.setSelection(position);
//        updateUI(mListTitle);
//    }




    //endregion
}

