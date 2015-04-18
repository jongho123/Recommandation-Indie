package kookmin.cs.mobile.recommendation_indie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainPage extends ActionBarActivity {

  private static final int REQUEST_CODE_LOGIN = 1001;
  private boolean singer = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

  }

  protected void onStart() {
    super.onStart();
    Log.i("mytag", "onStart");

    LinearLayout contentsLayout = (LinearLayout) findViewById(R.id.activity_main_container);
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    contentsLayout.removeAllViewsInLayout();

    if(singer) {
      inflater.inflate(R.layout.activity_main_singer, contentsLayout, true);
      Log.i("mytag", "inflate");
      return ;
    }

    inflater.inflate(R.layout.activity_main_listener, contentsLayout, true);
    Log.i("mytag", "end start");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
    super.onActivityResult(requestCode, resultCode, Data);

    Log.i("mytag", "onActivityResult");
    if(requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
      if(Data.getExtras().getInt("user") == 1)
        singer = true;

      Toast.makeText(getApplicationContext(), ""+singer, Toast.LENGTH_SHORT).show();
    }
    Log.i("mytag", "end onActivityResult");

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
}
