package kookmin.cs.mobile.recommendation_indie;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by sloth on 2015-06-03.
 */
public class analysisMusicListAdapter extends BaseAdapter {
  private Context mContext;

  private ArrayList<analysisMusicItem> mItems = new ArrayList<>();

  public analysisMusicListAdapter(Context context) {
    mContext = context;
  }

  public int getCount() {
    return mItems.size();
  }

  @Override
  public Object getItem(int i) {
    return mItems.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    analysisMusicView itemView;
    if (convertView == null) {
      itemView = new analysisMusicView(mContext);
    } else {
      itemView = (analysisMusicView) convertView;
    }

    Log.i("mytag2", mItems.get(position).getData(0) + " " + mItems.get(position).getData(1) + " " + mItems.get(position).getData(2));
    itemView.setText(0, mItems.get(position).getData(0));
    itemView.setText(1, mItems.get(position).getData(1));
    itemView.setText(2, mItems.get(position).getData(2));
    return itemView;
  }

  public void addItem(analysisMusicItem musicItem) {
    mItems.add(musicItem);
  }

  public ArrayList<analysisMusicItem> getMusicList() {
    return mItems;
  }
}
