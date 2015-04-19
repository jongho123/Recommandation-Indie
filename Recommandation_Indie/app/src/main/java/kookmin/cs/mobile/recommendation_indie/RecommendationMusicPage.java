package kookmin.cs.mobile.recommendation_indie;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 테스트용으로 만든 음악 추천 페이지입니다.
 * @details 음악 스트리밍 테스트용으로 3가지 버튼을 만들었으며 시작을 누르면 서버에서 .amr 음악 파일을 스트리밍으로 받아 음악을 재생합니다. 음악을 받아오는데 약간의
 * 딜레이가 있습니다. 현재는 테스트용 짧은 음악 한가지만 재생됩니다.
 * @date 2015-04-20
 * @Todo 기능 부분은 서비스로 구현해야함, 약간의 딜레이 문제 해결, UI 구성
 */
public class RecommendationMusicPage extends ActionBarActivity implements View.OnClickListener,
                                                                          MediaPlayer.OnPreparedListener,
                                                                          MediaPlayer.OnErrorListener {

  static final String AUDIO_URL = "http://52.68.82.234:19918";
  private MediaPlayer mediaPlayer;
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
        try {
          mediaPlayer = new MediaPlayer();
          mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
          mediaPlayer.setDataSource(AUDIO_URL + "/recommendation");
          mediaPlayer.setOnPreparedListener(this);
          mediaPlayer.setOnErrorListener(this);
          mediaPlayer.prepareAsync();
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case R.id.btn_pause:
        playbackPosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();
        Toast.makeText(getApplicationContext(), "음악 파일 재생 중지됨", Toast.LENGTH_SHORT).show();
        break;
      case R.id.btn_restart:
        mediaPlayer.start();
        mediaPlayer.seekTo(playbackPosition);
        Toast.makeText(getApplicationContext(), "음악 파일 재생 재시작됨", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mediaPlayer != null) {
      try {
        mediaPlayer.release();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Toast.makeText(getApplicationContext(), "추천 액티비티 종료", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onPrepared(MediaPlayer mediaPlayer) {
    mediaPlayer.start();
    Toast.makeText(getApplicationContext(), "음악 파일 재생 시작됨", Toast.LENGTH_SHORT).show();
  }

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
}
