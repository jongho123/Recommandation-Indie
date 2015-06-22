package kookmin.cs.mobile.recommendation_indie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 이 액티비티는 클래스는 로그인 페이지입니다.
 * @details 아이디와 비밀번호를 입력하여 로그인할 수 있는 화면입니다. 회원가입, 로그인 기능을 가지고 있습니다. 현재는 Listener와 singer로 로그인 했을때를
 * 따로 테스트 하기위해 버튼을 3개로 구현하였습니다.
 * @date 2015-04-20
 * @Todo 서버와 연동하여 회원가입과 로그인 기능 구현.
 */
public class LoginPage extends ActionBarActivity implements View.OnClickListener {

  private EditText editUserId;
  private EditText editPass;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    Button btnSignUp = (Button) findViewById(R.id.btn_sign_up);
    editUserId = (EditText) findViewById(R.id.edit_user_id);
    editPass = (EditText) findViewById(R.id.edit_pass);
    /* test button */
    Button btnListener = (Button) findViewById(R.id.btn_sign_in);
    Button btnSinger = (Button) findViewById(R.id.btn_singer);

    btnSignUp.setOnClickListener(this);
    btnListener.setOnClickListener(this);
    btnSinger.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(editUserId.getWindowToken(), 0);

    switch (v.getId()) {
      case R.id.btn_sign_up:
        Toast.makeText(getApplicationContext(), "sign up", Toast.LENGTH_SHORT).show();
        break;
      case R.id.btn_sign_in:
        Intent resultSignin = new Intent();
        resultSignin.putExtra("user", 0);
        if(!editUserId.getText().toString().equalsIgnoreCase("")) {
          resultSignin.putExtra("user_id", editUserId.getText().toString());
        } else {
          Toast.makeText(getApplicationContext(), "id를 입력해주세요.", Toast.LENGTH_SHORT).show();
          break;
        }

        setResult(RESULT_OK, resultSignin);
        finish();
        break;
      case R.id.btn_singer:
        Toast.makeText(getApplicationContext(), "singer", Toast.LENGTH_SHORT).show();
        Intent resultSinger = new Intent();
        resultSinger.putExtra("user", 1);
        if(!editUserId.getText().toString().equalsIgnoreCase("")) {
          resultSinger.putExtra("user_id", editUserId.getText().toString());
        } else {
          Toast.makeText(getApplicationContext(), "id를 입력해주세요.", Toast.LENGTH_SHORT).show();
          break;
        }
        createUser();
        setResult(RESULT_OK, resultSinger);
        finish();
        break;
    }
  }

  private void createUser() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        URL url;
        HttpURLConnection urlConnection;
        try {
          url =
              new URL("http://52.68.82.234:19918/createuser" + "/" + editUserId.getText().toString());
          urlConnection = (HttpURLConnection) url.openConnection();

          urlConnection.setDoInput(true);
          urlConnection.setUseCaches(false);

          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

          String res = reader.readLine();
          in.close();
          Log.i("mytag", res);

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
