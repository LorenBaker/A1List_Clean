package com.lbconsulting.a1list.presentation.ui.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.presenters.interfaces.ListItemsPresenter;
import com.lbconsulting.a1list.presentation.ui.activities.MainActivity;
import com.lbconsulting.a1list.presentation.ui.adapters.ListItemsArrayAdapter;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import timber.log.Timber;


/**
 * A fragment that shows the ListItems for the provided ListTitle
 */
public class fragListItems extends Fragment implements ListItemsPresenter.ListItemView {

    private static final String ARG_LIST_TITLE_JSON = "argListTitleJson";
    private static final String ARG_LIST_TITLE_POSITION = "argListTitlePosition";
    LinearLayout llListItems;
    ListView lvListItems;
    //    private ListItemsPresenter_Impl mPresenter;
    private ListTitle mListTitle;
    private int mPosition;
    private ListItemsArrayAdapter mListItemsArrayAdapter;

    private ListTitleRepository_Impl mListTitleRepository;
    private ListItemRepository_Impl mListItemRepository;


    public fragListItems() {
        // Required empty public constructor
    }

    public static fragListItems newInstance(int position, String listTitleJson) {
        fragListItems frag = new fragListItems();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_TITLE_POSITION, position);
        args.putString(ARG_LIST_TITLE_JSON, listTitleJson);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListTitle = null;
        Bundle args = getArguments();
        if (args.containsKey(ARG_LIST_TITLE_JSON)) {
            String listTitleJson = args.getString(ARG_LIST_TITLE_JSON);
            Gson gson = new Gson();
            mListTitle = gson.fromJson(listTitleJson, ListTitle.class);
        } else {
            Timber.e("onCreate(): No ListTitle found!");
        }

        if (args.containsKey(ARG_LIST_TITLE_POSITION)) {
            mPosition = args.getInt(ARG_LIST_TITLE_POSITION);
        }

        EventBus.getDefault().register(this);

        mListTitleRepository = AndroidApplication.getListTitleRepository();
        mListItemRepository = AndroidApplication.getListItemRepository();

//        mPresenter = new ListItemsPresenter_Impl(ThreadExecutor.getInstance(),
//                MainThreadImpl.getInstance(), this, mListTitle);

        Timber.i("onCreate() complete for \"%s\"", mListTitle.getName());
    }

    @Subscribe
    public void onEvent(MyEvents.updateFragListItemsUI event) {
        // if event.getListTitleUuid() == null then all fragments updateStorage their UI
        if (event.getListTitleUuid() == null || mListTitle.getUuid().equals(event.getListTitleUuid())) {
            List<ListItem> listItems = mListItemRepository.retrieveAllListItems(mListTitle, false);
            displayListItems(listItems);
        }
    }


//    public void onEvent(MyEvents.showListItem event) {
//        int itemPosition = mListItemsArrayAdapter.getItemPosition(event.getListItemUuid());
//        int firstVisible = lvListItems.getFirstVisiblePosition();
//        int lastVisible = lvListItems.getLastVisiblePosition();
//
//        if (itemPosition > lastVisible || itemPosition < firstVisible) {
//            lvListItems.smoothScrollToPosition(itemPosition);
//        }
//    }
//
//    private void refreshListTitle(String listTitleUuid, String source) {
//        if (listTitleUuid != null && !listTitleUuid.equals(MySettings.NOT_AVAILABLE)) {
//            mListTitle = ListTitle.retrieveListTitle(listTitleUuid);
//            if (mListTitle != null) {
//                mListTheme = mListTitle.getAttributes();
//                MyLog.i("fragListItems", source + " refreshListTitle " + mListTitle.getListItem());
//            } else {
//                MyLog.e("fragListItems", source + ": listTitleUuid = " + listTitleUuid + " Unable to find ListTitle");
//            }
//        } else {
//            MyLog.e("fragListItems", source + ": listTitleUuid in null or NOT_AVAILABLE");
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.i("onCreateView() for ListTitle: %s.", mListTitle.getName());
        View rootView = inflater.inflate(R.layout.frag_list_items, container, false);


        llListItems = (LinearLayout) rootView.findViewById(R.id.llListItems);
        Drawable backgroundDrawable = CommonMethods.getBackgroundDrawable(
                mListTitle.retrieveListTheme().getStartColor(), mListTitle.retrieveListTheme().getEndColor());
        llListItems.setBackground(backgroundDrawable);

//        lvListItems = (com.nhaarman.listviewanimations.itemmanipulation.DynamicListView) rootView.findViewById(R.id.lvListItems);
        lvListItems = (ListView) rootView.findViewById(R.id.lvListItems);

        lvListItems.setLongClickable(true);

        // Set up the ListView adapter
        mListItemsArrayAdapter = new ListItemsArrayAdapter(getActivity(), lvListItems, mListTitle);
        lvListItems.setAdapter(mListItemsArrayAdapter);


        // setup dragDrop
//        if (!mListTitle.sortListItemsAlphabetically()) {
//            lvListItems.enableDragAndDrop();
//            lvListItems.setOnItemLongClickListener(
//                    new AdapterView.OnItemLongClickListener() {
//                        @Override
//                        public boolean onItemLongClick(final AdapterView<?> parent, final View view,
//                                                       final int position, final long id) {
//                            lvListItems.startDragging(position);
//                            return true;
//                        }
//                    }
//            );
//        } else {
//            lvListItems.disableDragAndDrop();
//            lvListItems.setOnItemLongClickListener(null);
//        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated() for ListTitle \"%s\".", mListTitle.getName());
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.i("onSaveInstanceState() for ListTitle \"%s\".", mListTitle.getName());
//        outState.putString(ARG_LIST_TITLE_JSON, mListTitle.getListTitleUuid());
        /*
        Called to ask the fragment to save its current dynamic state,
        so it can later be reconstructed in a new instance if its process is restarted.
        If a new instance of the fragment later needs to be created, the data you place
        in the Bundle here will be available in the Bundle given to onCreate(Bundle),
        onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
        
        Note however: this method may be called at any time before onDestroy(). 
        
        There are many situations where a fragment may be mostly torn down (such as when placed on 
        the back stack with no UI showing), but its state will not be saved until its owning 
        activity actually needs to save its state.
        */
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_LIST_TITLE_JSON)) {
//            mListTitleUuid = savedInstanceState.getString(ARG_LIST_TITLE_JSON);
//            refreshListTitle(mListTitleUuid, "onViewStateRestored");
//        }
        Timber.i("onViewStateRestored()for ListTitle \"%s\".", mListTitle.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("onResume()for ListTitle \"%s\".", mListTitle.getName());

        List<ListItem> listItems = mListItemRepository.retrieveAllListItems(mListTitle, false);
        displayListItems(listItems);

//        mPresenter.resume();
//        refreshListTitle(mListTitleUuid, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.i("onPause()for ListTitle \"%s\".", mListTitle.getName());

        if(!MainActivity.isLoggingOut()) {
            // Save the ListView's position
            int firstVisiblePosition = lvListItems.getFirstVisiblePosition();
            View v = lvListItems.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - lvListItems.getPaddingTop());

            mListTitleRepository.updateListTitlePosition(mListTitle, firstVisiblePosition, top);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()for ListTitle \"%s\".", mListTitle.getName());
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void displayListItems(List<ListItem> listItems) {
        // mPresenter's results
        Timber.i("displayListItems(): Retrieved %d ListItems for ListTitle \"%s\".",
                listItems.size(), mListTitle.getName());

        mListItemsArrayAdapter.setData(listItems, mListTitle.retrieveListTheme());
        mListItemsArrayAdapter.setListTheme(mListTitle.retrieveListTheme());
        mListItemsArrayAdapter.notifyDataSetChanged();
        Drawable backgroundDrawable = CommonMethods.getBackgroundDrawable(
                mListTitle.retrieveListTheme().getStartColor(), mListTitle.retrieveListTheme().getEndColor());

        int firstVisiblePosition = 0;
        int top = 0;
        ListTitlePosition listTitlePosition = mListTitleRepository.retrieveListTitlePosition(mListTitle);
        if (listTitlePosition != null) {
            firstVisiblePosition = listTitlePosition.getListViewFirstVisiblePosition();
            top = listTitlePosition.getListViewTop();
        }

        lvListItems.setSelectionFromTop(firstVisiblePosition, top);

        llListItems.setBackground(backgroundDrawable);
    }

    @Override
    public void showProgress(String waitMessage) {
        Timber.i("showProgress(): %s.", waitMessage);
    }

    @Override
    public void hideProgress(String message) {
        Timber.i("hideProgress(): %s.", message);
    }

    @Override
    public void showError(String message) {
        Timber.e("showError(): %s.", message);
    }
}
