package kookmin.cs.mobile.recommendation_indie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 이 액티비티는 클래스는 메인화면 페이지입니다.
 * @details Listener 화면으로 음악 추천과 음악 검색, 설정의 버튼으로 만들어 보았으며 Singer로 로그인시 음악 등록하는 버튼이 추가로 보여집니다. 액션바에
 * 로그인 버튼이 있습니다. 로그인 버튼을 누르면 로그인 화면으로 넘어갑니다.
 * @date 2015-04-20
 * @Todo 버튼을 이미지로 교체해야함.
 */
public class MainPage extends ActionBarActivity implements View.OnClickListener {

  private static final int REQUEST_CODE_LOGIN = 1001;
  private boolean singer = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  protected void onStart() {
    super.onStart();

    LinearLayout contentsLayout = (LinearLayout) findViewById(R.id.activity_main_container);
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    contentsLayout.removeAllViewsInLayout();

    if (singer) {
      inflater.inflate(R.layout.activity_main_singer, contentsLayout, true);
      Button btnReco = (Button) findViewById(R.id.btn_music_recommendation);
      Button btnRegister = (Button) findViewById(R.id.btn_register_music);
      btnReco.setOnClickListener(this);
      btnRegister.setOnClickListener(this);

      return;
    }

    inflater.inflate(R.layout.activity_main_listener, contentsLayout, true);
    Button btnReco = (Button) findViewById(R.id.btn_music_recommendation);
    btnReco.setOnClickListener(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
    super.onActivityResult(requestCode, resultCode, Data);

    if (requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
      if (Data.getExtras().getInt("user") == 1) {
        singer = true;
      } else {
        singer = false;
      }

      Toast.makeText(getApplicationContext(), "" + singer, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_login) {
      startActivityForResult(new Intent(this, LoginPage.class), REQUEST_CODE_LOGIN);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.btn_music_recommendation) {
      startActivity(new Intent(getApplicationContext(), RecommendationMusicPage.class));
    } else if (v.getId() == R.id.btn_register_music) {
      startActivity(new Intent(getApplicationContext(), RegisterMusicPage.class));
    }
  }
}
