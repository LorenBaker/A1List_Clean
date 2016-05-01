package com.lbconsulting.a1list.domain.model;

/**
 * This class holds the ListTitleAndPosition
 */
public class ListTitleAndPosition {

    private ListTitle listTitle;
    private ListTitlePosition listTitlePosition;

    public ListTitleAndPosition(ListTitle listTitle, ListTitlePosition listTitlePosition) {
        this.listTitle = listTitle;
        this.listTitlePosition = listTitlePosition;
    }

    public ListTitle getListTitle() {
        return listTitle;
    }

    public void setListTitle(ListTitle listTitle) {
        this.listTitle = listTitle;
    }

    public ListTitlePosition getListTitlePosition() {
        return listTitlePosition;
    }

    public void setListTitlePosition(ListTitlePosition listTitlePosition) {
        this.listTitlePosition = listTitlePosition;
    }
}
