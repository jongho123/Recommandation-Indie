package kookmin.cs.mobile.recommendation_indie;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sloth on 2015-06-12.
 */
public class MyListTab extends Fragment implements AdapterView.OnItemClickListener,
                                                   AdapterView.OnItemLongClickListener {

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";
  analysisMusicListAdapter adapter;

  Handler handler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    adapter = new analysisMusicListAdapter(getActivity());

    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    String[] projection = {
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA
    };

    Cursor cursor = getActivity().managedQuery(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null);

    while (cursor.moveToNext()) {
      if (!cursor.getString(0).equalsIgnoreCase("<unknown>") && !cursor.getString(1)
          .equalsIgnoreCase("<unknown>")) {
        adapter.addItem(
            new analysisMusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_my_list, container, false);

    ListView mylist = (ListView) rootView.findViewById(R.id.my_list);
    mylist.setAdapter(adapter);

    mylist.setOnItemClickListener(this);

    return rootView;
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    final analysisMusicItem item = adapter.getMusicList().get(i);

    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.e("mytag", "come in send ");

        try {
          url = new URL("http://52.68.82.234:19918/analysis");
          urlConnection = (HttpURLConnection) url.openConnection(); // HTTP ����

          urlConnection.setDoInput(true);
          urlConnection.setDoOutput(true);
          urlConnection.setUseCaches(false);

          urlConnection.setRequestMethod("POST");
          urlConnection
              .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

          DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

          File path = new File(item.getData(2));
          if (!path.exists()) {
            Log.e("mytag", "file not exists");
            return;
          }
          String filename = new String(path.getName().getBytes(), "ISO-8859-1");
          String title = new String(item.getData(0).getBytes(), "ISO-8859-1");
          String artist = new String(item.getData(1).getBytes(), "ISO-8859-1");
          String user_id = new String(MainPage.USER_ID.getBytes(), "ISO-8859-1");

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
          out.writeBytes("{\"user_id\":\"" + user_id + "\"," + "\"request\":\"play\"}" + "\r\n");
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
          in.close();

          handler.sendEmptyMessage(0);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          urlConnection.disconnect();
        }
      }
    }).start();

    handler = new Handler() {
      public void handleMessage(Message msg) {
        Intent resultRecom = new Intent();
        resultRecom.putExtra("title", item.getData(0));
        resultRecom.putExtra("artist", item.getData(1));
        getActivity().setResult(Activity.RESULT_OK, resultRecom);
        getActivity().finish();
      }
    };
  }
  @Override
  public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
    return false;
  }
}
