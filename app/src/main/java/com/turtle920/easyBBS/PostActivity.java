package com.turtle920.easyBBS;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    String userid;
    String token;
    String title;
    String tag;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
        userid = sharedPreferences.getString("userid", "");
        token = sharedPreferences.getString("token", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_post) {
            /*********
             * 发布~~~~
             */
            EditText editText1 = (EditText) findViewById(R.id.editText_postActivity_title);
            title = editText1.getText().toString();
            EditText editText3 = (EditText)findViewById(R.id.editText_postActivity_tag);
            tag = editText3.getText().toString();
            EditText editText2 = (EditText)findViewById(R.id.editText_postActivity_content);
            content = editText2.getText().toString();

            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "post/post",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("-1")){
                                Toast toast=Toast.makeText(getApplicationContext(), "发帖错误?_?", Toast.LENGTH_SHORT);
                                toast.show();
                            }else{
                                Intent intent = new Intent(PostActivity.this, HomePageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("TAG", error.getMessage(), error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("userid", userid);
                    map.put("token", token);
                    map.put("title", title);
                    map.put("content", content);
                    map.put("tag", tag);
                    return map;
                }
            };

            mQueue.add(stringRequest);

        }
        return false;
    }
}
