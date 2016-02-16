package com.lbconsulting.a1list.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.lbconsulting.a1list.AndroidApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;


/**
 * A1List Common methods.
 */
public class CommonMethods {

    public static void changePasswordRequest(final Context context, final String email) {
        if (isNetworkAvailable()) {
            if (!email.equals(MySettings.NOT_AVAILABLE)) {
                Backendless.UserService.restorePassword(email, new AsyncCallback<Void>()
                {
                    public void handleResponse(Void response) {
                        String msg = "An email has been sent to " + email + " with a link to change your password.";
                        String title = "Change Password Request";
                        CommonMethods.showOkDialog(context, title, msg);
                        Timber.i("changePasswordRequest(): %s", msg);
                    }

                    public void handleFault(BackendlessFault fault) {
                        // password recovery failed, to get the error code call fault.getCode()
                        String msg = "Error " + fault.getCode() + ". " + fault.getMessage();
                        String title = "Change Password Request Error";
                        CommonMethods.showOkDialog(context, title, msg);
                        Timber.e("changePasswordRequest(): %s", msg);
                    }
                });
            }
        } else {
            String msg = "Unable to request change of password. Network is not available.";
            String title = "Change Password Request Failed";
            showOkDialog(context, title, msg);
            Timber.e("changePasswordRequest(): %s", msg);
        }
    }

    public static boolean isEmailValid(String email) {
        // Source: http://www.regular-expressions.info/email.html
        final String EMAIL_PATTERN = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        Pattern pattern;
        Matcher matcher;

/*
            ^#start - of - string
            ( ? =.*[0 - 9])#a digit must occur at least once
            ( ? =.*[a - z])#a lower case letter must occur at least once
            ( ? =.*[A - Z])#an upper case letter must occur at least once
            ( ? =\\S + $)#no whitespace allowed in the entire string
            .{4,}#anything, at least six places though
            $#end - of - string
        */

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    public static boolean isNetworkAvailable() {
        boolean networkAvailable = false;
        ConnectivityManager cm = (ConnectivityManager) AndroidApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if ((ni != null) && (ni.isConnected())) {
            // We have a network connection
            networkAvailable = true;
        }
        if (networkAvailable) {
            Timber.i("Network is available.");
        } else {
            Timber.i("Network NOT available.");
        }

        return networkAvailable;
    }

    public static void showSnackbar(View view, String message, int length) {
        Snackbar.make(view, message, length)
                .setAction("Action", null).show();
    }

    public static void showOkDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set dialog title and message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnOK = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                btnOK.setTextSize(18);
            }
        });

        // show it
        alertDialog.show();
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(int dp) {
        float floatDp = (float) dp;
        Resources resources = AndroidApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = floatDp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

/*    *//**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     *//*
    public static float convertPixelsToDp(float px) {
        Resources resources = App.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }*/
}
