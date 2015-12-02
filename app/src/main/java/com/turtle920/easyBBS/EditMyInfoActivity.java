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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class EditMyInfoActivity extends AppCompatActivity {

    String userid;
    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
        userid = sharedPreferences.getString("userid", "");

        mQueue = Volley.newRequestQueue(getApplicationContext());
        requestUserInfo(userid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_my_info, menu);
        return true;
    }

    private void requestUserInfo(final String userid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/get-userinfo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        JsonUserinfo userinfo = gson.fromJson(response, JsonUserinfo.class);

                        TextView textView1 = (TextView) findViewById(R.id.editText_editMyInfoActivity_username);
                        textView1.setText(userinfo.username);
                        TextView textView2 = (TextView) findViewById(R.id.editText_editMyInfoActivity_email);
                        textView2.setText(userinfo.email);
                        TextView textView3 = (TextView) findViewById(R.id.editText_editMyInfoActivity_introduction);
                        textView3.setText(userinfo.introduction);

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
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            TextView textView1 = (TextView) findViewById(R.id.editText_editMyInfoActivity_username);
            TextView textView2 = (TextView) findViewById(R.id.editText_editMyInfoActivity_email);
            TextView textView3 = (TextView) findViewById(R.id.editText_editMyInfoActivity_introduction);

            refreshMyInfo(userid, textView1.getText().toString(), textView2.getText().toString(), textView3.getText().toString());

        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshMyInfo(final String userid, final String username, final String email, final String introduction) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/refresh-userinfo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", response);
                        if (response.equals("-1")) {
                            Toast toast = Toast.makeText(getApplicationContext(), "update failed", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "update successfully!", Toast.LENGTH_SHORT);
                            toast.show();
                            Intent intent = new Intent(EditMyInfoActivity.this, MyPageActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                map.put("username", username);
                map.put("email", email);
                map.put("introduction", introduction);
                return map;
            }
        };
        mQueue.add(stringRequest);

    }

}
