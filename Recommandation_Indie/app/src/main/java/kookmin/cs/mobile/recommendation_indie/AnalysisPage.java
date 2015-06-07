package kookmin.cs.mobile.recommendation_indie;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sloth on 2015-06-03.
 */
public class AnalysisPage extends ActionBarActivity {

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";
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
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DATA
    };

    Cursor cursor = managedQuery(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null);

    while (cursor.moveToNext()) {
      if (!cursor.getString(0).equalsIgnoreCase("<unknown>") && !cursor.getString(1).equalsIgnoreCase("<unknown>")) {
        adapter.addItem(
            new analysisMusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                                  cursor.getString(3)));
      }
    }

    musicList.setAdapter(adapter);

    Thread work = new Thread(new Runnable() {
      @Override
      public void run() {
        ArrayList<analysisMusicItem> items = adapter.getMusicList();
        for(int position=0; position < items.size(); position++) {
          try {
            url = new URL("http://52.68.82.234:19918/analysis");
            urlConnection = (HttpURLConnection) url.openConnection(); // HTTP ����

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            //urlConnection.setChunkedStreamingMode(0);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("POST");
            urlConnection
                .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            File path = new File(items.get(position).getData(3));
            if (!path.exists()) {
              Log.e("mytag", "file not exists");
              return;
            }
            String filename = new String(path.getName().getBytes(), "ISO-8859-1");
            String title = new String(items.get(position).getData(0).getBytes(), "ISO-8859-1");
            String artist = new String(items.get(position).getData(1).getBytes(), "ISO-8859-1");
            String album = new String(items.get(position).getData(2).getBytes(), "ISO-8859-1");

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"playinfo\";" + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(
                "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"userinfo\";" + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("{\"user_id\":\"guest\"," + "\"request\":\"play\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"uploaded\";"
                + "filename=\"" + filename + "\"" + "\r\n");
            out.writeBytes("\r\n");

            FileInputStream filestream = new FileInputStream(path);

            int bytesAvailable = filestream.available();
            int bufsize = Math.min(bytesAvailable, 1024 * 32);
            byte[] buff = new byte[bufsize];
            while (filestream.read(buff, 0, bufsize) > 0) {
              out.write(buff, 0, bufsize);
              bytesAvailable = filestream.available();
              bufsize = Math.min(bytesAvailable, 1024 * 32);
            }

            out.writeBytes("\r\n");
            out.writeBytes("--" + boundary + "--\r\n");

            filestream.close();
            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            int data;
            String result = "";
            while ((data = in.read()) != -1) {
              result += (char) data;
            }
            Log.i("mytag", result);
            in.close();

            //Toast.makeText(getApplicationContext(), "recv data : " + result, Toast.LENGTH_SHORT).show();
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            urlConnection.disconnect();
          }
        }
      }
    });

    work.start();

    musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      }
    });
  }
}
