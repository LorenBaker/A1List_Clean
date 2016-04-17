package com.lbconsulting.a1list.backendlessMessaging;


import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.AppSettings;

/**
 * This class holds the message payload for actions associated with AppSettingss
 */
public class AppSettingsMessage {


    AppSettings appSettings;
    int action;
    int target;

    public AppSettingsMessage() {

    }

    public AppSettingsMessage(AppSettings appSettings, int action, int target) {
        this.appSettings = appSettings;
        this.action = action;
        this.target = target;
    }

    public static String toJson(AppSettings appSettings, int action, int target) {
        String appSettingsJsonString = "";

        try {
            AppSettingsMessage message = new AppSettingsMessage(appSettings, action, target);
            Gson gson = new Gson();
            appSettingsJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appSettingsJsonString;
    }

    public static AppSettingsMessage fromJason(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, AppSettingsMessage.class);
    }

    public AppSettings getAppSettings() {
        return appSettings;
    }

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
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
}
