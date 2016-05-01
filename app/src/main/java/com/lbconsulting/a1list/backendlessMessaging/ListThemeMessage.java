package com.lbconsulting.a1list.backendlessMessaging;


import com.backendless.Backendless;
import com.backendless.messaging.MessageStatus;
import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTheme;

import timber.log.Timber;

/**
 * This class holds the message payload for actions associated with ListThemes
 */
public class ListThemeMessage {


    ListTheme listTheme;
    int action;
    int target;
    String defaultListThemeUuid;

    public ListThemeMessage() {

    }

    public ListThemeMessage(ListTheme listTheme, int action, int target, String defaultListThemeUuid) {
        this.listTheme = listTheme;
        this.action = action;
        this.target = target;
        this.defaultListThemeUuid = defaultListThemeUuid;
    }

    public static String toJson(ListTheme listTheme, int action, int target, String defaultListThemeUuid) {
        String listThemeJsonString = "";

        try {
            ListThemeMessage message = new ListThemeMessage(listTheme, action, target, defaultListThemeUuid);
            Gson gson = new Gson();
            listThemeJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listThemeJsonString;
    }

    public static ListThemeMessage fromJason(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ListThemeMessage.class);
    }

    public static void sendMessage(ListTheme listTheme, int action, String defaultListThemeUuid) {
        String messageChannel = listTheme.getMessageChannel();

        int target = Messaging.TARGET_ALL_DEVICES;
        String listThemeMessageJson = toJson(listTheme, action, target, defaultListThemeUuid);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listThemeMessageJson);
        if (messageStatus.getErrorMessage() == null) {
            // successfully sent message to Backendless.
            switch (action) {
                case Messaging.ACTION_CREATE:
                    Timber.i("sendMessage(): CREATE \"%s\" message successfully sent.", listTheme.getName());
                    break;

                case Messaging.ACTION_UPDATE:
                    Timber.i("sendMessage(): UPDATE \"%s\" message successfully sent.", listTheme.getName());
                    break;

                case Messaging.ACTION_DELETE:
                    Timber.i("sendMessage(): DELETE \"%s\" message successfully sent.", listTheme.getName());
                    break;
            }

        } else {
            // error sending message to Backendless.
            Timber.e("sendMessage(): FAILED to send message for \"%s\". %s.",
                    listTheme.getName(), messageStatus.getErrorMessage());
        }

    }

    public ListTheme getListTheme() {
        return listTheme;
    }

    public void setListTheme(ListTheme listTheme) {
        this.listTheme = listTheme;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getDefaultListThemeUuid() {
        return defaultListThemeUuid;
    }

    public void setDefaultListThemeUuid(String defaultListThemeUuid) {
        this.defaultListThemeUuid = defaultListThemeUuid;
    }
}
