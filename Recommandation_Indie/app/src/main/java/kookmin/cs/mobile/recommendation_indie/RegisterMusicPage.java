package kookmin.cs.mobile.recommendation_indie;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jongho Lim, sloth@kookmin.ac.kr
 * @version 0.0.2
 * @brief 테스트용으로 만든 음악 등록 페이지입니다.
 * @details 음악 스트리밍 등록 테스트 페이지입니다. 에딧 텍스트와 등록 버튼 하나가 있다. edit 창에 음악 파일 이름을 입력하고 등록 버튼을 누르면 서버에 음악
 * 파일이 올라간다. 음악 파일은 sdcard0 폴더에 Music 폴더 내에 위치한 음악 파일만 등록이 가능하다.
 * @date 2015-04-22
 * @Todo 기능 부분은 서비스로 구현해야함, 약간의 딜레이 문제 해결, UI 구성
 */
public class RegisterMusicPage extends ActionBarActivity implements View.OnClickListener,
                                                                    AdapterView.OnItemSelectedListener {

  private URL url;
  private HttpURLConnection urlConnection;
  private static String boundary = "ABAB***ABAB";

  private int position = 0;

  private Button btnRegister;
  private Button btnCancle;
  private EditText musicTitle;
  private Cursor cursor;

  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register_music);

    btnRegister = (Button) findViewById(R.id.btn_register);
    btnCancle = (Button) findViewById(R.id.btn_cancle_register);
    musicTitle = (EditText) findViewById(R.id.edit_title);

    Spinner spineer = (Spinner) findViewById(R.id.spin_musicfile);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    String[] projection = {
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM
    };

    cursor = managedQuery(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null);

    adapter.add("음악을 파일을 입력하세요");
    while (cursor.moveToNext()) {
      adapter.add(cursor.getString(0));
    }

    spineer.setAdapter(adapter);
    spineer.setOnItemSelectedListener(this);

    btnRegister.setOnClickListener(this);
    btnCancle.setOnClickListener(this);

    handler = new Handler() {
      public void handleMessage(Message msg) {
        if(msg.what == 1) {
          Toast.makeText(getApplicationContext(), "성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), "등록에 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }

        finishActivity();
      }
    };
  }

  private void finishActivity() {
    finish();
  }
  @Override
  public void onClick(View view) {

    Log.i("mytag", "onclick");
    if (view.getId() == R.id.btn_register) {
      Log.i("mytag", "register");
      new Thread(new Runnable() {
        @Override
        public void run() {
          cursor.moveToPosition(position - 1);
          Log.i("mytag", "current position : " + position);
          try {
            url = new URL("http://52.68.82.234:19918/register");
            urlConnection = (HttpURLConnection) url.openConnection(); // HTTP 연결

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestMethod("POST");
            urlConnection
                .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            String filename = new String(cursor.getString(0).getBytes(), "ISO-8859-1");
            String title = new String(musicTitle.getText().toString().getBytes(), "ISO-8859-1");
            String artist = new String(MainPage.USER_ID.getBytes(), "ISO-8859-1");
            Log.i("mytag", "filename");

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data; name=\"userinfo\";"
                + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(
                "{\"user_id\":\"" + artist + "\"," + "\"request\":\"register\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data; name=\"musicinfo\";"
                + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(
                "{\"title\":\"" + title + "\"," + "\"artist\":\"" + artist + "\"}" + "\r\n");
            out.flush();

            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes(
                "Content-Disposition: form-data; name=\"uploaded\";filename=\"" + filename + "\""
                + "\r\n");
            out.writeBytes("\r\n");

            File path = new File(cursor.getString(1));
            if (!path.exists()) {
              Log.e("mytag", "file not exists");
              return;
            }
            FileInputStream filestream = new FileInputStream(path);

            int bytesAvailable = filestream.available();
            int bufsize = Math.min(bytesAvailable, 1024 * 32);
            byte[] buff = new byte[bufsize];
            while (filestream.read(buff, 0, bufsize) > 0) {
              out.write(buff, 0, bufsize);
              bytesAvailable = filestream.available();
              bufsize = Math.min(bytesAvailable, 1024 * 32);
            }

            out.writeBytes("\r\n");
            out.writeBytes("--" + boundary + "--\r\n");

            filestream.close();
            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            int data;
            String result = "";
            while ((data = in.read()) != -1) {
              result += (char) data;
            }
            Log.i("mytag", result);
            in.close();

            if(result.equalsIgnoreCase("ok")) {
              handler.sendEmptyMessage(1);
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
    } else if (view.getId() == R.id.btn_cancle_register) {
      finish();
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    position = i;
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }
}
