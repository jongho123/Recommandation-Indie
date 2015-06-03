package kookmin.cs.mobile.recommendation_indie;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

/**
 * Created by sloth on 2015-06-03.
 */
public class AnalysisPage extends ActionBarActivity {

  analysisMusicListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_analysis);

    ListView musicList = (ListView) findViewById(R.id.analysis_music_list);
    adapter = new analysisMusicListAdapter(this);

    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    String[] projection = {
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA,
    };

    Cursor cursor = this.managedQuery(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null);

    while (cursor.moveToNext()) {
      if (!cursor.getString(1).equalsIgnoreCase("<unknown>")) {
        adapter.addItem(
            new analysisMusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
      }
    }

    musicList.setAdapter(adapter);
  }
}
