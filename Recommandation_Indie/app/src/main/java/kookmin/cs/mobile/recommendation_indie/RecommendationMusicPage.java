package kookmin.cs.mobile.recommendation_indie;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.4
 * @brief 테스트용으로 만든 음악 추천 페이지입니다.
 * @details 음악 스트리밍 테스트용으로 3가지 버튼을 만들었으며 시작을 누르면 서버에서 .amr 음악 파일을 스트리밍으로 받아 음악을 재생합니다. 음악을 받아오는데 약간의
 * 딜레이가 있습니다. 현재는 테스트용 짧은 음악 한가지만 재생됩니다. 음악 play 종료 후 다시 추천 요청합니다.
 * @date 2015-04-20
 * @Todo 기능 부분은 서비스로 구현해야함, UI 구성, 왜 GET 요청이 두번씩 올까?
 */
public class RecommendationMusicPage extends ActionBarActivity implements View.OnClickListener,
                                                                          MediaPlayer.OnPreparedListener,
                                                                          MediaPlayer.OnErrorListener,
                                                                          MediaPlayer.OnCompletionListener {

  static final String AUDIO_URL = "http://52.68.82.234:19918";
  private MediaPlayer mediaplayer;
  private int playbackPosition = 0;
  private boolean musicPlay = false;
  private boolean restart = false;
  private boolean serverConnect = false;

  private Button btnPlay;

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";
  private JSONObject recommendedMusic;

  public static String prevTitle = "";
  public static String prevArtist = "";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_muisc_recommendation);

    btnPlay = (Button) findViewById(R.id.btn_play);
    Button btnNext = (Button) findViewById(R.id.btn_next_play);
    Button btnPrev = (Button) findViewById(R.id.btn_prev_play);

    btnPlay.setOnClickListener(this);
    btnNext.setOnClickListener(this);
    btnPrev.setOnClickListener(this);

    work.start();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_play:
        if (!musicPlay && !serverConnect) {
          icon2playing();
          if (!restart ) {
            serverConnect = true;
            playMusic();
          } else {
            mediaplayer.start();
            mediaplayer.seekTo(playbackPosition);
            musicPlay = !musicPlay;
            Toast.makeText(getApplicationContext(), "음악 파일 재생 재시작됨", Toast.LENGTH_SHORT).show();
          }
        } else {
          icon2stoping();
          playbackPosition = mediaplayer.getCurrentPosition();
          mediaplayer.pause();
          musicPlay = !musicPlay;
          Toast.makeText(getApplicationContext(), "음악 파일 재생 중지됨", Toast.LENGTH_SHORT).show();
        }
        break;

      case R.id.btn_prev_play:
      case R.id.btn_next_play:
        icon2playing();
        playMusic();
        break;
    }
  }

  protected void playMusic() {
    if (mediaplayer != null && mediaplayer.isPlaying()) {
      releaseMusic();
    }

    try {
      mediaplayer = new MediaPlayer();
      mediaplayer.reset();
      mediaplayer.setDataSource(AUDIO_URL + "/streaming" + "/" + recommendedMusic.getString("url") + "/" + recommendedMusic.getString("track_id"));
      mediaplayer.prepareAsync();
      mediaplayer.setOnPreparedListener(this);
      mediaplayer.setOnErrorListener(this);
      mediaplayer.setOnCompletionListener(this);
      musicPlay = true;
      restart = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // music player 메모리 해제
  protected void releaseMusic() {
    if (mediaplayer != null) {
      try {
        mediaplayer.release();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // 액티비티 종료
  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseMusic();
    Toast.makeText(getApplicationContext(), "추천 액티비티 종료", Toast.LENGTH_SHORT).show();
  }

  // prepare 되었을 때 이벤트
  @Override
  public void onPrepared(MediaPlayer mediaPlayer) {
    mediaplayer.start();
    Toast.makeText(getApplicationContext(), "음악 파일 재생 시작됨", Toast.LENGTH_SHORT).show();
  }

  // error 이벤트
  @Override
  public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
    switch (i) {
      case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
        Toast.makeText(this, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i2,
                       Toast.LENGTH_SHORT).show();
        break;

      case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
        Toast.makeText(this, "MEDIA ERROR SERVER DIED " + i2, Toast.LENGTH_SHORT).show();
        break;

      case MediaPlayer.MEDIA_ERROR_UNKNOWN:
        Toast.makeText(this, "MEDIA ERROR UNKOWN " + i2, Toast.LENGTH_SHORT).show();
        break;
    }

    return false;
  }

  // music play 종료 후 이벤트
  @Override
  public void onCompletion(MediaPlayer mediaPlayer) {
    releaseMusic();
    playMusic();
  }

  private void icon2playing() {
    btnPlay.setSelected(true);
  }

  private void icon2stoping() {
    btnPlay.setSelected(false);
  }

  Thread work = new Thread(new Runnable() {
    @Override
    public void run() {
      try {
        Log.i("mytag", "come in connect");

        url = new URL("http://52.68.82.234:19918/recommendation");
        urlConnection = (HttpURLConnection) url.openConnection(); // HTTP ����

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);

        urlConnection.setRequestMethod("POST");
        urlConnection
            .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes(
            "Content-Disposition: form-data;" + "name=\"base\";" + "\r\n");
        out.writeBytes("\r\n");

        String title = new String(prevTitle.getBytes(), "ISO-8859-1");
        String artist = new String(prevArtist.getBytes(), "ISO-8859-1");

        out.writeBytes(
            "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\"}" + "\r\n");
        out.flush();

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes(
            "Content-Disposition: form-data;" + "name=\"info\";" + "\r\n");
        out.writeBytes("\r\n");
        out.writeBytes("{\"user_id\":\"guest\"," + "\"request\":\"recommendation\"}" + "\r\n");
        out.flush();
        out.writeBytes("--" + boundary + "--\r\n");

        out.flush();
        out.close();

        Log.i("mytag", "send message");

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

        Log.i("mytag", "read message");

        int data;
        String res="";
        while ((data = in.read()) != -1) {
          res += (char)data;
        }
        in.close();

        Log.i("mytag", res);
        recommendedMusic = new JSONObject(res);
        Log.i("mytag2",recommendedMusic.getString("url"));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        urlConnection.disconnect();
        serverConnect = false;
      }
    }
  });
}
