package com.lbconsulting.a1list.backendlessMessaging;


import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTheme;

/**
 * This class holds the message payload for actions associated with ListThemes
 */
public class ListThemeMessage {


    ListTheme listTheme;
    int action;
    int target;

    public ListThemeMessage() {

    }

    public ListThemeMessage(ListTheme listTheme, int action, int target) {
        this.listTheme = listTheme;
        this.action = action;
        this.target = target;
    }

    public static String toJson(ListTheme listTheme, int action, int target) {
        String listThemeJsonString = "";

        try {
            ListThemeMessage message = new ListThemeMessage(listTheme, action, target);
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
}