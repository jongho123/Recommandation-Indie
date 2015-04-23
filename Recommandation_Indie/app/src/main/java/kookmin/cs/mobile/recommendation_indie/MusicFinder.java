package kookmin.cs.mobile.recommendation_indie;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
public class MusicFinder extends ActionBarActivity {

  private URL url;
  private HttpURLConnection urlConnection;
  private ArrayAdapter<String> adapter;
  private ArrayList<String> musicList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_music_finder);

    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, musicList);

    ListView list = (ListView) findViewById(R.id.music_list);
    list.setAdapter(adapter);

    Thread work = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          url = new URL("http://52.68.82.234:19918/music");
          urlConnection = (HttpURLConnection) url.openConnection();

          urlConnection.setDoInput(true);
          urlConnection.setUseCaches(false);

          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

          String res = reader.readLine();
          Log.i("mytag", res);
          in.close();

          JSONArray jsonArray = new JSONArray(res);
          for(int i = 0; i < jsonArray.length(); ++i) {
            musicList.add(jsonArray.get(i).toString());
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          urlConnection.disconnect();

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              adapter.notifyDataSetChanged();
            }
          });
        }
      }
    });

    work.start();
  }
}
