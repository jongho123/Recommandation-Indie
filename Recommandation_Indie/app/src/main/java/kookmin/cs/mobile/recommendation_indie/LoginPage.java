package kookmin.cs.mobile.recommendation_indie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by sloth on 2015-04-19.
 */
public class LoginPage extends Activity implements View.OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    Button btnSignUp = (Button) findViewById(R.id.btn_sign_up);

    /* test button */
    Button btnListener = (Button) findViewById(R.id.btn_sign_in);
    Button btnSinger = (Button) findViewById(R.id.btn_singer);

    btnSignUp.setOnClickListener(this);
    btnListener.setOnClickListener(this);
    btnSinger.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    switch(v.getId()) {
      case R.id.btn_sign_up:
        Toast.makeText(getApplicationContext(), "sign up", Toast.LENGTH_SHORT).show();
        break;
      case R.id.btn_sign_in:
        Toast.makeText(getApplicationContext(), "sign in", Toast.LENGTH_SHORT).show();
        break;
      case R.id.btn_singer:
        Toast.makeText(getApplicationContext(), "singer", Toast.LENGTH_SHORT).show();
        Intent result = new Intent();
        result.putExtra("user", 1);
        setResult(RESULT_OK, result);
        finish();
        break;
    }
  }
}
