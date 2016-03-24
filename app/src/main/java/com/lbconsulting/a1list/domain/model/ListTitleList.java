package com.lbconsulting.a1list.domain.model;

import java.util.List;

/**
 * Created by Loren on 3/17/2016.
 */
public class ListTitleList {

    private static List <ListTitle> listTitleList;

    public ListTitleList() {
        // A default constructor.
    }

    public static List<ListTitle> getListTitleList() {
        return listTitleList;
    }

    public static void setListTitleList(List<ListTitle> listTitleList) {
        ListTitleList.listTitleList = listTitleList;
    }
}
