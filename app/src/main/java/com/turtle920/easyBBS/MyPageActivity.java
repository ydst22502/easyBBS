package com.turtle920.easyBBS;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity implements View.OnClickListener {

    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
        String userid = sharedPreferences.getString("userid", "");
        String token = sharedPreferences.getString("token", "");

        mQueue = Volley.newRequestQueue(getApplicationContext());
        requestUserInfo(userid);
        //requestAvatar(userid);

        TextView textView1 = (TextView) findViewById(R.id.textView_myPageActivity_edit);
        textView1.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.textView_myPageActivity_edit) {
            Intent intent = new Intent(MyPageActivity.this, EditMyInfoActivity.class);
            startActivity(intent);
        }
    }

    private void requestAvatar(final String userid) {

        ImageRequest imageRequest = new ImageRequest(Config.USER_ASSETS_URL + md5(userid) + "/images/avatar.jpg",
                new Response.Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        Log.d("TAG", response.toString());
                        ImageView imageView = (ImageView) findViewById(R.id.imageView_myPageActivity_avatar);
                        imageView.setImageBitmap(response);
                    }
                }, 0, 0, null, null);
        mQueue.add(imageRequest);
    }

    private static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void requestUserInfo(final String userid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/get-userinfo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        JsonUserinfo userinfo = gson.fromJson(response, JsonUserinfo.class);

                        TextView textView1 = (TextView) findViewById(R.id.textView_myPageActivity_username);
                        textView1.setText(userinfo.username);
                        TextView textView2 = (TextView) findViewById(R.id.textView_myPageActivity_email);
                        textView2.setText(userinfo.email);
                        TextView textView3 = (TextView) findViewById(R.id.textView_myPageActivity_introduction);

                        if (userinfo.introduction.equals("empty")) {
                            textView3.setText("我很懒什么也没留下");
                        } else {
                            textView3.setText(userinfo.introduction);
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
                return map;
            }
        };
        mQueue.add(stringRequest);
    }


}
