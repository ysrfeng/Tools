package com.qf.ysr.asyncdemotask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
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
                            URLConnection connection = mUrl.openConnection();
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
                }.execute("http://fanyi.youdao.com/openapi.do?keyfrom=zaixianapp&key=1192467935&type=data&doctype=xml&version=1.1&q=good");
            }
        });
    }


}
