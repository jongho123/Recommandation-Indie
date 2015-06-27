package kookmin.cs.mobile.recommendation_indie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 이 액티비티는 클래스는 음악을 찾는 페이지입니다.
 * @details 테스트 버전으로 서버에서 음악 리스트를 받아 출력합니다.
 * @date 2015-04-24
 * @Todo 클릭하면 노래 실행하도록 해야함. UI 개선
 */
public class MusicFinder extends ActionBarActivity implements View.OnClickListener,
                                                              AdapterView.OnItemClickListener,
                                                              AdapterView.OnItemLongClickListener {

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";

  private analysisMusicListAdapter adapter;
  private ArrayList<String> trackId = new ArrayList<>();

  private EditText editFind;

  private int start = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_music_finder);
    editFind = (EditText) findViewById(R.id.edit_find);
    Button btnFindServer = (Button) findViewById(R.id.btn_server_find);
    btnFindServer.setOnClickListener(this);

    adapter = new analysisMusicListAdapter(this);
    ListView list = (ListView) findViewById(R.id.music_list);
    list.setAdapter(adapter);

    list.setOnItemClickListener(this);
    list.setOnItemLongClickListener(this);
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.btn_server_find) {
      InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(editFind.getWindowToken(), 0);

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            url = new URL("http://52.68.82.234:19918/find");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("POST");
            urlConnection
                .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"findinfo\";" + "\r\n");
            out.writeBytes("\r\n");

            String title;
            String artist;

            if (editFind.getText().toString().contains(",")) {
              title =
                  new String(editFind.getText().toString().split(",")[0].getBytes(), "ISO-8859-1");
              artist =
                  new String(editFind.getText().toString().split(",")[1].getBytes(), "ISO-8859-1");
            } else {
              title = new String(editFind.getText().toString().getBytes(), "ISO-8859-1");
              artist = "";
            }

            String user_id = new String(MainPage.USER_ID.getBytes(), "ISO-8859-1");
            out.writeBytes(
                "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\","
                + "\"start\":\"" + start + "\"" + "}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"userinfo\";" + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(
                "{\"user_id\":\"" + user_id + "\"," + "\"request\":\"find\"}" + "\r\n");
            out.flush();
            out.writeBytes("--" + boundary + "--\r\n");

            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String res = "";
            String line;
            while ((line = reader.readLine()) != null) {
              res += line;
            }
            ;
            Log.i("mytag", res);
            in.close();

            if (!res.equalsIgnoreCase("no track")) {
              JSONArray jsonArray = new JSONArray(res);
              for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject track = new JSONObject(jsonArray.get(i).toString());
                adapter.addItem(
                    new analysisMusicItem(track.getString("title"), track.getString("artist"),
                                          track.getString("url")));
                trackId.add(track.getString("track_id"));
              }
              start += jsonArray.length() + 1;

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  adapter.notifyDataSetChanged();
                }
              });
            } else {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(getApplicationContext(), "더 이상 검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                      .show();
                }
              });
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            urlConnection.disconnect();
          }
        }
      }).start();

    }
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    Intent recomInfo = new Intent(this, RecommendationMusicPage.class);
    recomInfo.putExtra("title", adapter.getMusicList().get(i).getData(0));
    recomInfo.putExtra("artist", adapter.getMusicList().get(i).getData(1));

    startActivity(recomInfo);
    finish();
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
    Intent recomInfo = new Intent(this, RecommendationMusicPage.class);
    recomInfo.putExtra("title", adapter.getMusicList().get(i).getData(0));
    recomInfo.putExtra("artist", adapter.getMusicList().get(i).getData(1));
    recomInfo.putExtra("url", adapter.getMusicList().get(i).getData(2));
    recomInfo.putExtra("track_id", trackId.get(i));

    startActivity (recomInfo);
    finish();

    return false;
  }
}
