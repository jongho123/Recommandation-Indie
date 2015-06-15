package kookmin.cs.mobile.recommendation_indie;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sloth on 2015-06-12.
 */
public class LatestListTab extends Fragment implements AdapterView.OnItemClickListener,
                                                       AdapterView.OnItemLongClickListener {

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";
  SimpleCursorAdapter adapter;
  Cursor cursor;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String[] columns = {"_id", "title", "artist", "url", "track_id"};
    cursor = MainPage.db.query(MainPage.TABLE_NAME, columns, null, null, null, null, "_id desc", null);

    getActivity().startManagingCursor(cursor);

    String[] columns2 = {"title", "artist"};
    int[] to = new int[]{R.id.txt_music_title, R.id.txt_music_artist};
    adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_analysis, cursor, columns2, to);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_latest_list, container, false);

    ListView latestList = (ListView) rootView.findViewById(R.id.latest_list);

    latestList.setAdapter(adapter);
    latestList.setOnItemClickListener(this);
    latestList.setOnItemLongClickListener(this);

    return rootView;
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if (cursor.getCount() > 0) {

      cursor.moveToPosition(i);

      Intent resultRecom = new Intent();
      resultRecom.putExtra("title", cursor.getString(1));
      resultRecom.putExtra("artist", cursor.getString(2));
      getActivity().setResult(Activity.RESULT_OK, resultRecom);
      getActivity().finish();
    }
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
    if (cursor.getCount() > 0) {
      cursor.moveToPosition(i);

      Intent resultRecom = new Intent();
      resultRecom.putExtra("url", cursor.getString(3));
      resultRecom.putExtra("track_id", cursor.getString(4));
      getActivity().setResult(2000, resultRecom);
      getActivity().finish();
    }

    return false;
  }
}
