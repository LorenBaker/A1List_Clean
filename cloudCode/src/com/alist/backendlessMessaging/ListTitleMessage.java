package com.alist.backendlessMessaging;


import com.alist.models.ListTitle;
import com.google.gson.Gson;

/**
 * This class holds the message payload for actions associated with ListTitles
 */
public class ListTitleMessage {


    ListTitle listTitle;
    int action;
    int target;

    public ListTitleMessage() {

    }

    public ListTitleMessage(ListTitle listTitle, int action, int target) {
        this.listTitle = listTitle;
        this.action = action;
        this.target = target;
    }

    public static String toJson(ListTitle listTitle, int action, int target) {
        String listTitleJsonString = "";

        try {
            ListTitleMessage message = new ListTitleMessage(listTitle, action, target);
            Gson gson = new Gson();
            listTitleJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listTitleJsonString;
    }

//    public static ListTitleMessage fromJason(String jsonString) {
//        Gson gson = new Gson();
//        return gson.fromJson(jsonString, ListTitleMessage.class);
//    }

    public ListTitle getListTitle() {
        return listTitle;
    }

    public void setListTitle(ListTitle listTitle) {
        this.listTitle = listTitle;
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
