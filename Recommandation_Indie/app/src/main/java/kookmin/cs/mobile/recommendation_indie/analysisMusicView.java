package kookmin.cs.mobile.recommendation_indie;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sloth on 2015-06-03.
 */
public class analysisMusicView extends LinearLayout {

  private TextView txtMusicTitle;
  private TextView txtMusicArtist;

  public analysisMusicView(Context context) {
    super(context);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    inflater.inflate(R.layout.list_analysis, this, true);

    txtMusicTitle = (TextView) findViewById(R.id.txt_music_title);
    txtMusicArtist = (TextView) findViewById(R.id.txt_music_artist);
  }

  public analysisMusicView(Context context, analysisMusicItem aItem) {
    super(context);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    inflater.inflate(R.layout.list_analysis, this, true);

    txtMusicTitle = (TextView) findViewById(R.id.txt_music_title);
    txtMusicArtist = (TextView) findViewById(R.id.txt_music_artist);

    txtMusicTitle.setText(aItem.getData(0));
    txtMusicArtist.setText(aItem.getData(1));
  }

  public void setText(int index, String data) {
    switch (index) {
      case 0 :
        txtMusicTitle.setText(data);
        break;
      case 1 :
        txtMusicArtist.setText(data);
        break;
    }
  }
}
