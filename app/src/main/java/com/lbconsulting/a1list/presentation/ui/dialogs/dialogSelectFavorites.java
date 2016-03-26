package com.lbconsulting.a1list.presentation.ui.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.adapters.FavoritesArrayAdapter;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;


/**
 * A dialog where the user selects favorite items to add to the list
 */
public class dialogSelectFavorites extends DialogFragment {

    private static final String ARG_LIST_TITLE_JSON = "listTitleJson";
    private ListTitle mListTitle;
    private FavoritesArrayAdapter mFavoritesArrayAdapter;
    private ListItemRepository_Impl mListItemRepository;

    private AlertDialog mDialog;

    public dialogSelectFavorites() {
        // Empty constructor required for DialogFragment
    }

    public static dialogSelectFavorites newInstance(String listTitleJson) {
        Timber.i("newInstance()");
        dialogSelectFavorites dialogFragment = new dialogSelectFavorites();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_TITLE_JSON, listTitleJson);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(ARG_LIST_TITLE_JSON)) {
            String listTitleJson = args.getString(ARG_LIST_TITLE_JSON);
            Gson gson = new Gson();
            mListTitle = gson.fromJson(listTitleJson, ListTitle.class);
        }
        if (mListTitle != null) {
            Timber.i("onCreate(): \"%s\"", mListTitle.getName());
        } else {
            Timber.e("onCreate(): FAILED to retrieve ListTitle!");
        }
        mListItemRepository = AndroidApplication.getListItemRepository();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                Button applyButton = mDialog.getButton(Dialog.BUTTON_POSITIVE);
                applyButton.setTextSize(17);
                applyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Apply
                        mFavoritesArrayAdapter.selectCheckedItems();
                        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mListTitle.getUuid()));
                        dismiss();
                    }
                });

                Button cancelButton = mDialog.getButton(Dialog.BUTTON_NEGATIVE);
                cancelButton.setTextSize(17);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Cancel
                        dismiss();
                    }
                });
            }
        });
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_favorites, null, false);

        // find the dialog's views
        ListView lvFavorites = (ListView) view.findViewById(R.id.lvFavorites);

        lvFavorites.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.whiteSmoke));

        List<ListItem> favorites = mListItemRepository.retrieveFavoriteListItems();
        mFavoritesArrayAdapter = new FavoritesArrayAdapter(getActivity(), favorites);
        lvFavorites.setAdapter(mFavoritesArrayAdapter);

        // build the dialog
        String title = String.format("Select Items for \"%s\"", mListTitle.getName());
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.btnApply_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mDialog;
    }

}
