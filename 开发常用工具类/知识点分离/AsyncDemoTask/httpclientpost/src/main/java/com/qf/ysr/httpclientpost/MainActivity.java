package com.qf.ysr.httpclientpost;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
                ReadUrl("http://fanyi.youdao.com/openapi.do?keyfrom=zaixianapp&key=1192467935&type=data&doctype=xml&version=1.1&q=good",et.getText().toString());
            }
        });

    }

    public void ReadUrl(String path,String in) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String path = params[0];
//                HttpGet get = new HttpGet(path);
                HttpPost post = new HttpPost(path);
                try {
                   // StringEntity entity = new StringEntity(params[1]);
//                    post.setEntity(entity);
                    List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
                    list.add(new BasicNameValuePair("name",params[0]));
                    post.setEntity(new UrlEncodedFormEntity(list));
                } catch (UnsupportedEncodingException e) {

                    e.printStackTrace();
                }
                try {
                    HttpResponse response = httpClient.execute(post);
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
        }.execute(path,in);
    }

}
