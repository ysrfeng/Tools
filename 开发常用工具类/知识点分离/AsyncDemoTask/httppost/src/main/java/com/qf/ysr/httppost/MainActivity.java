package com.qf.ysr.httppost;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    private Button bt;
    private TextView tvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvShow = (TextView) findViewById(R.id.tvShow);
        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        try {
                            URL mUrl = new URL(params[0]);
                            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();

                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");
                            //TODO
                          //  connection.setRequestProperty("Content-type", "application/x-java-serialized-object");
                            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
                            BufferedWriter bw = new BufferedWriter(osw);
                            bw.write("keyfrom=zaixianapp&key=1192467935&type=data&doctype=xml&version=1.1&q=good");
                            bw.flush();


                            InputStream is = connection.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "utf-8");
                            BufferedReader br = new BufferedReader(isr);
                            String line;
                            while ((line = br.readLine()) != null) {
                                //   tvShow.setText(line);
                                Log.e("MMMM", line);
                            }
                            br.close();
                            isr.close();
                            is.close();


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.execute("http://fanyi.youdao.com/openapi.do");
            }
        });
    }

}
