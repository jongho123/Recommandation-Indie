package kookmin.cs.mobile.recommendation_indie;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_muisc_recommendation);

    Button btnPlay = (Button) findViewById(R.id.btn_play);

    /* test button */
    Button btnPause = (Button) findViewById(R.id.btn_pause);
    Button btnRestart = (Button) findViewById(R.id.btn_restart);

    btnPlay.setOnClickListener(this);
    btnPause.setOnClickListener(this);
    btnRestart.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_play:
        playMusic();
        break;
      case R.id.btn_pause:
        playbackPosition = mediaplayer.getCurrentPosition();
        mediaplayer.pause();
        Toast.makeText(getApplicationContext(), "음악 파일 재생 중지됨", Toast.LENGTH_SHORT).show();
        break;
      case R.id.btn_restart:
        mediaplayer.start();
        mediaplayer.seekTo(playbackPosition);
        Toast.makeText(getApplicationContext(), "음악 파일 재생 재시작됨", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  protected void playMusic() {
    try {
        if (mediaplayer != null && mediaplayer.isPlaying()) {
          releaseMusic();
        }

        mediaplayer = new MediaPlayer();
        mediaplayer.reset();
        mediaplayer.setDataSource(AUDIO_URL + "/recommendation");
        mediaplayer.setOnPreparedListener(this);
        mediaplayer.setOnErrorListener(this);
        mediaplayer.setOnCompletionListener(this);
        mediaplayer.prepareAsync();
    } catch (IOException e) {
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
}
