package com.lbconsulting.a1list.presentation.ui.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.presentation.ui.dialogs.colorPicker.GradientView;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * A dialog where the user edits the ListAttributes' name
 */
public class dialogColorPicker extends DialogFragment {

    public static final int TEXT_COLOR_PICKER = 1;
    public static final int START_COLOR_PICKER = 2;
    public static final int END_COLOR_PICKER = 3;

    private static final String ARG_COLOR_PICKER_ID = "argColorPickerID";
    private static final String ARG_STARTING_COLOR = "argStartingColor";

    private TextView mTextView;
    private Drawable mIcon;

    private AlertDialog mDialog;
    private int mColorPickerId;
    private int mStartingColor;
    private int mSelectedColor;


    public dialogColorPicker() {
        // Empty constructor required for DialogFragment
    }


    public static dialogColorPicker newInstance(int colorPickerID, int startingColor) {
        Timber.i("newInstance()");
        dialogColorPicker fragment = new dialogColorPicker();
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR_PICKER_ID, colorPickerID);
        args.putInt(ARG_STARTING_COLOR, startingColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        Bundle args = getArguments();
        if (args.containsKey(ARG_COLOR_PICKER_ID)) {
            mColorPickerId = args.getInt(ARG_COLOR_PICKER_ID);
            mStartingColor = args.getInt(ARG_STARTING_COLOR);
        } else {
            String msg = "onCreate(): Fragment arguments do not contain Color Picker ID = %s!";
            Timber.e(msg,ARG_COLOR_PICKER_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button saveButton = mDialog.getButton(Dialog.BUTTON_POSITIVE);
                saveButton.setTextSize(17);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        switch (mColorPickerId) {
                            case TEXT_COLOR_PICKER:
                                EventBus.getDefault().post(new MyEvents.setListThemeTextColor(mSelectedColor));
                                break;

                            case START_COLOR_PICKER:
                                EventBus.getDefault().post(new MyEvents.setListThemeStartColor(mSelectedColor));
                                break;

                            case END_COLOR_PICKER:
                                EventBus.getDefault().post(new MyEvents.setListThemeEndColor(mSelectedColor));
                                break;
                        }
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


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("onCreateDialog()");

        // inflate the xml layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_color_picker, null, false);

        // find the dialog's views
        mIcon = ContextCompat.getDrawable(getActivity(), R.drawable.andy_the_robot);
        mTextView = (TextView) view.findViewById(R.id.color);
        mTextView.setCompoundDrawablesWithIntrinsicBounds(mIcon, null, null, null);
        GradientView mTop = (GradientView) view.findViewById(R.id.top);
        GradientView mBottom = (GradientView) view.findViewById(R.id.bottom);
        mTop.setBrightnessGradientView(mBottom);
        mBottom.setOnColorChangedListener(new GradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(GradientView view, int color) {
                mSelectedColor = color;
                mTextView.setTextColor(color);
                mTextView.setText(String.format(getActivity().getString(R.string.onColorChanged),
                        Integer.toHexString(color)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIcon.setTint(color);
                }
            }
        });

        mTop.setColor(mStartingColor);

        String title = "";
        switch (mColorPickerId) {
            case TEXT_COLOR_PICKER:
                title = getActivity().getString(R.string.colorPicker_textColor_title);
                break;

            case START_COLOR_PICKER:
                title = getActivity().getString(R.string.colorPicker_startColor_title);
                break;

            case END_COLOR_PICKER:
                title = getActivity().getString(R.string.colorPicker_endColor_title);
                break;
        }

        // build the dialog
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.btnSelect_title, null)
                .setNegativeButton(R.string.btnCancel_title, null)
                .create();

        return mDialog;
    }

}
