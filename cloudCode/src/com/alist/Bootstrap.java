
package com.alist;

import com.backendless.Backendless;
import com.backendless.servercode.IBackendlessBootstrap;

import com.alist.models.AppSettings;
import com.alist.models.ListItem;
import com.alist.models.ListTheme;
import com.alist.models.ListTitle;

public class Bootstrap implements IBackendlessBootstrap
{
            
  @Override
  public void onStart()
  {

    Backendless.Persistence.mapTableToClass( "AppSettings", AppSettings.class );
    Backendless.Persistence.mapTableToClass( "ListItem", ListItem.class );
    Backendless.Persistence.mapTableToClass( "ListTheme", ListTheme.class );
    Backendless.Persistence.mapTableToClass( "ListTitle", ListTitle.class );
    // add your code here
  }
    
  @Override
  public void onStop()
  {
    // add your code here
  }
    
}
        