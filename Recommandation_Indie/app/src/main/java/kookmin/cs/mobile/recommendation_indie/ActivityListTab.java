package kookmin.cs.mobile.recommendation_indie;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.widget.TabHost.TabSpec;

/**
 * Created by sloth on 2015-06-12.
 */
public class ActivityListTab extends FragmentActivity {

  FragmentTabHost tabHost;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_tab);

    tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
    tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

    TabSpec tabSpec1 = tabHost.newTabSpec("Tab1");
    tabSpec1.setIndicator("이전 추천받은 곡");
    tabHost.addTab(tabSpec1, LatestListTab.class, null);

    // Editor Tab
    TabSpec tabSpec2 = tabHost.newTabSpec("Tab2");
    tabSpec2.setIndicator("나의 곡"); // Tab Subject
    tabHost.addTab(tabSpec2, MyListTab.class, null);

    // show First Tab Content
    tabHost.setCurrentTab(0);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    tabHost.removeAllViews();
  }
}
