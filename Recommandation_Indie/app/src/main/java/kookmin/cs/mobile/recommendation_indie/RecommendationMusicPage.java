package kookmin.cs.mobile.recommendation_indie;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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
  private boolean ableEvolution = false;

  private Button btnPlay;
  private TextView musicName;
  private ImageView thumbImg;
  private Button btnLike;
  private Button btnUnlike;
  private Bitmap thumbnail;

  private Handler handler;

  private String URL;
  private String trackId;

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";
  private JSONObject recommendedMusic;

  public static String prevTitle = "";
  public static String prevArtist = "";
  public static final int REQUEST_CODE_RECOM = 1002;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_muisc_recommendation);

    thumbImg = (ImageView) findViewById(R.id.img_recommendation_music);
    btnPlay = (Button) findViewById(R.id.btn_play);
    Button btnNext = (Button) findViewById(R.id.btn_next_play);
    Button btnPrev = (Button) findViewById(R.id.btn_prev_play);
    btnLike = (Button) findViewById(R.id.btn_recommendation_like);
    btnUnlike = (Button) findViewById(R.id.btn_recommendation_unlike);

    musicName = (TextView) findViewById(R.id.txt_recommendation_music_title);
    btnPlay.setOnClickListener(this);
    btnNext.setOnClickListener(this);
    btnPrev.setOnClickListener(this);
    btnLike.setOnClickListener(this);
    btnUnlike.setOnClickListener(this);

    handler = new Handler() {
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case 0:
            try {
              mediaplayer = new MediaPlayer();

              mediaplayer.setOnPreparedListener(RecommendationMusicPage.this);
              mediaplayer.setOnErrorListener(RecommendationMusicPage.this);
              mediaplayer.setOnCompletionListener(RecommendationMusicPage.this);

              mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

              mediaplayer.reset();
              mediaplayer.setDataSource(
                  AUDIO_URL + "/streaming" + "/" + recommendedMusic.getString("url") + "/"
                  + recommendedMusic.getString("track_id"));
              Log.i("mytag5", "whatwahtwahtwhatwhatwhathwathwathwahtwaht");

              mediaplayer.prepareAsync();

              musicName.setText(
                  recommendedMusic.getString("title") + " - " + recommendedMusic
                      .getString("artist"));
              btnLike.setText("" + recommendedMusic.getInt("like"));
              btnUnlike.setText("" + recommendedMusic.getInt("unlike"));

              if(!recommendedMusic.getString("url").equalsIgnoreCase("undefined")) {
                thumbImg.setImageBitmap(thumbnail);
              }

              prevTitle = recommendedMusic.getString("title");
              prevArtist = recommendedMusic.getString("artist");

              ContentValues recordValues = new ContentValues();
              recordValues.put("title", recommendedMusic.getString("title"));
              recordValues.put("artist", recommendedMusic.getString("artist"));
              recordValues.put("url", recommendedMusic.getString("url"));
              recordValues.put("track_id", recommendedMusic.getString("track_id"));

              MainPage.db.insert(MainPage.TABLE_NAME, null, recordValues);

              musicPlay = true;
              restart = true;
            } catch (Exception e) {
              e.printStackTrace();
            }
            break;
          case 1:
            try {
              btnLike.setText("" + recommendedMusic.getInt("like"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
            break;
          case 2:
            try {
              btnUnlike.setText("" + recommendedMusic.getInt("unlike"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
            break;
          case 3:
            Toast.makeText(getApplicationContext(), "음악 정보가 없습니다", Toast.LENGTH_SHORT).show();
            break;

          case 10:
            if (mediaplayer != null) {
              if (mediaplayer.isPlaying()) {
                mediaplayer.stop();
                mediaplayer.reset();
                mediaplayer.release();
              }
            }

            icon2playing();
            serverConnect = true;
            new Thread(new Runnable() {
              @Override
              public void run() {
                try {
                  url = new URL("http://52.68.82.234:19918/musicinfo/" + URL + "/" + trackId + "/" + MainPage.USER_ID);
                  urlConnection = (HttpURLConnection) url.openConnection(); // HTTP ????

                  urlConnection.setDoInput(true);
                  urlConnection.setUseCaches(false);

                  urlConnection.setRequestMethod("GET");
                  InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                  int data;
                  String res = "";
                  while ((data = in.read()) != -1) {
                    res += (char) data;
                  }
                  in.close();

                  Charset charset = Charset.forName("ISO-8859-1");
                  ByteBuffer buff = charset.encode(res);

                  charset = Charset.forName("UTF-8");
                  recommendedMusic = new JSONObject(charset.decode(buff).toString());
                  Log.i("mytag", recommendedMusic.toString());

                  if(!recommendedMusic.getString("url").equalsIgnoreCase("undefined")) {
                    new Thread(new Runnable() {
                      @Override
                      public void run() {
                        URL url;
                        HttpURLConnection urlConnection;
                        try {
                          url =
                              new URL(
                                  "http://img.youtube.com/vi/" + recommendedMusic.getString("url")
                                  + "/default.jpg");
                          urlConnection = (HttpURLConnection) url.openConnection();

                          urlConnection.setDoInput(true);
                          urlConnection.setUseCaches(false);

                          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                          thumbnail = BitmapFactory.decodeStream(in);
                          in.close();
                          handler.sendEmptyMessage(0);
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                      }
                    }).start();
                  } else {
                    handler.sendEmptyMessage(0);
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                } finally {
                  urlConnection.disconnect();
                }
              }
            }).start();
            break;
        }
      }
    };

    final Intent args = getIntent();
    Log.i("mytag2", args.toString());
    if(args.getStringExtra("url") != null && args.getStringExtra("track_id") != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          Log.e("mytag", "come in send ");

          try {
            url = new URL("http://52.68.82.234:19918/urlanalysis");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("POST");
            urlConnection
                .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            String title = new String(args.getStringExtra("title").getBytes(), "ISO-8859-1");
            String artist = new String(args.getStringExtra("artist").getBytes(), "ISO-8859-1");
            String track_id = new String(args.getStringExtra("track_id").getBytes(), "ISO-8859-1");
            String video_id = new String(args.getStringExtra("url").getBytes(), "ISO-8859-1");
            String user_id = new String(MainPage.USER_ID.getBytes(), "ISO-8859-1");

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"playinfo\";" + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(
                "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\","
                + "\"trackId\":\"" + track_id + "\"," + "\"videoId\":\"" + video_id + "\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data;" + "name=\"userinfo\";" + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("{\"user_id\":\"" + user_id + "\"," + "\"request\":\"urlanalysis\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "--\r\n");

            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            int data;
            String result = "";
            while ((data = in.read()) != -1) {
              result += (char) data;
            }
            in.close();

          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            urlConnection.disconnect();

            URL = args.getStringExtra("url");
            trackId = args.getStringExtra("track_id");
            handler.sendEmptyMessage(10);
          }
        }
      }).start();

    } else if(args.getStringExtra("title") != null && args.getStringExtra("artist") != null) {
      prevTitle = args.getStringExtra("title");
      prevArtist = args.getStringExtra("artist");

      if (!serverConnect) {
        icon2playing();
        playMusic();
      }
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_play:
        if (!serverConnect) {

          if (!musicPlay) {
            icon2playing();
            if (!restart) {
              playMusic();
            } else {
              serverConnect = false;
              mediaplayer.start();
              mediaplayer.seekTo(playbackPosition);
              musicPlay = !musicPlay;
              Toast.makeText(getApplicationContext(), "음악 파일 재생 재시작됨", Toast.LENGTH_SHORT).show();
            }
          } else {
            serverConnect = false;
            icon2stoping();
            playbackPosition = mediaplayer.getCurrentPosition();
            mediaplayer.pause();
            musicPlay = !musicPlay;
            Toast.makeText(getApplicationContext(), "음악 파일 재생 중지됨", Toast.LENGTH_SHORT).show();
          }
        }
        break;

      case R.id.btn_prev_play:
      case R.id.btn_next_play:
        if (!serverConnect) {
          icon2playing();
          playMusic();
        }
        break;
      case R.id.btn_recommendation_like:
        if (ableEvolution) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              URL url;
              HttpURLConnection urlConnection;
              try {
                url =
                    new URL("http://52.68.82.234:19918/like" + "/" + recommendedMusic
                        .getString("track_id") + "/" + MainPage.USER_ID);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                String res = reader.readLine();
                in.close();

                recommendedMusic.put("like", Integer.parseInt(res));
                handler.sendEmptyMessage(1);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }).start();
        }
        break;
      case R.id.btn_recommendation_unlike:
        if (ableEvolution) {

          new Thread(new Runnable() {
            @Override
            public void run() {
              URL url;
              HttpURLConnection urlConnection;
              try {
                url =
                    new URL("http://52.68.82.234:19918/unlike" + "/" + recommendedMusic
                        .getString("track_id") + "/" + MainPage.USER_ID);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                String res = reader.readLine();
                in.close();

                recommendedMusic.put("unlike", Integer.parseInt(res));
                handler.sendEmptyMessage(2);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }).start();
        }
        break;
    }
  }

  protected void playMusic() {
    if (mediaplayer != null && mediaplayer.isPlaying()) {
      releaseMusic();
    }
    serverConnect = true;

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          url = new URL("http://52.68.82.234:19918/recommendation");
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
              "Content-Disposition: form-data;" + "name=\"base\";" + "\r\n");
          out.writeBytes("\r\n");

          String title = new String(prevTitle.getBytes(), "ISO-8859-1");
          String artist = new String(prevArtist.getBytes(), "ISO-8859-1");
          String user_id = new String(MainPage.USER_ID.getBytes(), "ISO-8859-1");
          out.writeBytes(
              "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\"}" + "\r\n");
          out.flush();

          out.writeBytes("--" + boundary + "\r\n");
          out.writeBytes(
              "Content-Disposition: form-data;" + "name=\"info\";" + "\r\n");
          out.writeBytes("\r\n");
          out.writeBytes(
              "{\"user_id\":\"" + user_id + "\"," + "\"request\":\"recommendation\"}" + "\r\n");
          out.flush();
          out.writeBytes("--" + boundary + "--\r\n");

          out.flush();
          out.close();

          InputStream in = new BufferedInputStream(urlConnection.getInputStream());

          int data;
          String res = "";
          while ((data = in.read()) != -1) {
            res += (char) data;
          }
          in.close();

          Charset charset = Charset.forName("ISO-8859-1");
          ByteBuffer buff = charset.encode(res);

          charset = Charset.forName("UTF-8");

          res = charset.decode(buff).toString();
          if(res.equalsIgnoreCase("no track")) {
            handler.sendEmptyMessage(3);
            return ;
          }
          recommendedMusic = new JSONObject(res);
          Log.i("mytag", recommendedMusic.toString());

          if(!recommendedMusic.getString("url").equalsIgnoreCase("undefined")) {
            new Thread(new Runnable() {
              @Override
              public void run() {
                URL url;
                HttpURLConnection urlConnection;
                try {
                  url =
                      new URL("http://img.youtube.com/vi/" + recommendedMusic.getString("url")
                              + "/default.jpg");
                  urlConnection = (HttpURLConnection) url.openConnection();

                  urlConnection.setDoInput(true);
                  urlConnection.setUseCaches(false);

                  InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                  thumbnail = BitmapFactory.decodeStream(in);
                  in.close();
                  handler.sendEmptyMessage(0);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }).start();
          } else {
            handler.sendEmptyMessage(0);
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          urlConnection.disconnect();
        }
      }
    }).start();
  }

  // music player 메모리 해제
  protected void releaseMusic() {
    if (mediaplayer != null) {
      try {
        mediaplayer.release();
        mediaplayer = null;
        ableEvolution = false;
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
    serverConnect = false;
    ableEvolution = true;
    Log.i("mytag5", "whatwahtwahtwhatwhatwhathwathwathwahtwaht~~~~~~~~~~~~");

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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_recommendation, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_list) {
      startActivityForResult(new Intent(this, ActivityListTab.class), REQUEST_CODE_RECOM);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
    super.onActivityResult(requestCode, resultCode, Data);

    if (requestCode == REQUEST_CODE_RECOM && resultCode == RESULT_OK) {
      prevTitle = Data.getExtras().getString("title");
      prevArtist = Data.getExtras().getString("artist");

      if (!serverConnect) {
        icon2playing();
        playMusic();
      }

    } else if (requestCode == REQUEST_CODE_RECOM && resultCode == 2000) {
      URL = Data.getStringExtra("url");
      trackId = Data.getStringExtra("track_id");
      handler.sendEmptyMessage(10);
    }
  }
}