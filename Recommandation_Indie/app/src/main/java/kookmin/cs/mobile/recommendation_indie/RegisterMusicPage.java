package kookmin.cs.mobile.recommendation_indie;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
public class RegisterMusicPage extends ActionBarActivity implements View.OnClickListener {

  private URL url;
  private HttpURLConnection urlConnection;
  private EditText editFilename;
  private static String boundary = "ABAB***ABAB";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register_music);

    Button btnRegister = (Button) findViewById(R.id.btn_register);

    editFilename = (EditText) findViewById(R.id.edit_filename);
    btnRegister.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {

    Thread work = new Thread(new Runnable() {
      @Override
      public void run() {

        try {
          url = new URL("http://52.68.82.234:19918/register");
          urlConnection = (HttpURLConnection) url.openConnection(); // HTTP 연결

          urlConnection.setDoInput(true);
          urlConnection.setDoOutput(true);
          //urlConnection.setChunkedStreamingMode(0);
          urlConnection.setUseCaches(false);

          urlConnection.setRequestMethod("POST");
          urlConnection
              .setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

          DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

          String filename = new String(editFilename.getText().toString().getBytes(), "ISO-8859-1");
          out.writeBytes("--" + boundary + "\r\n");
          out.writeBytes(
              "Content-Disposition: form-data; name=\"uploaded\";filename=\"" + filename + "\""
              + "\r\n");
          out.writeBytes("\r\n");

          File path = new File(Environment.getExternalStorageDirectory().getPath()
                               + "/music/" + editFilename.getText());
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

          //Toast.makeText(getApplicationContext(), "recv data : " + result, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          urlConnection.disconnect();
        }
      }
    });

    work.start();
  }

}
