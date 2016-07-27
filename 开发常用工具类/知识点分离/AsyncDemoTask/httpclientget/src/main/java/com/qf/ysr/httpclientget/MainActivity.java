package com.qf.ysr.httpclientget;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class MainActivity extends Activity {
    HttpClient httpClient;
    private EditText et;
    private Button bt;
    private TextView tvShow;

    private void assigments() {
        et = (EditText) findViewById(R.id.et);
        bt = (Button) findViewById(R.id.bt);
        tvShow = (TextView) findViewById(R.id.tvShow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assigments();
        httpClient = new DefaultHttpClient();

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadUrl("http://fanyi.youdao.com/openapi.do?keyfrom=zaixianapp&key=1192467935&type=data&doctype=xml&version=1.1&q=good"+et.getText());
            }
        });

    }

    public void ReadUrl(String path) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String path = params[0];
                HttpGet get = new HttpGet(path);

                try {
                    HttpResponse response = httpClient.execute(get);
                    String value = EntityUtils.toString(response.getEntity());
                    Log.e("MMM", value);
                    return value;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                tvShow.setText(s);
            }
        }.execute(path);
    }
}
