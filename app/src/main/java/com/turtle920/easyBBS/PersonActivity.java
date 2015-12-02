package com.turtle920.easyBBS;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class PersonActivity extends AppCompatActivity implements View.OnClickListener {

    RequestQueue mQueue;
    String username;//此页显示此用户名的个人信息
    String userid;//此页显示此id的个人信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        mQueue = Volley.newRequestQueue(getApplicationContext());

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        userid = bundle.getString("userid");

        requestUserInfo(userid);

        TextView textView = (TextView) findViewById(R.id.textView_personActivity_message);
        textView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.textView_personActivity_message) {
            Intent intent = new Intent(PersonActivity.this, SendMessageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("receiverId", userid);
            bundle.putString("receiverName", username);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void requestUserInfo(final String userid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/get-userinfo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        JsonUserinfo userinfo = gson.fromJson(response, JsonUserinfo.class);

                        TextView textView1 = (TextView) findViewById(R.id.textView_personActivity_username);
                        textView1.setText(userinfo.username);
                        username = userinfo.username;
                        TextView textView2 = (TextView) findViewById(R.id.textView_personActivity_email);
                        textView2.setText(userinfo.email);
                        TextView textView3 = (TextView) findViewById(R.id.textView_personActivity_introduction);

                        if (userinfo.introduction.equals("empty")) {
                            textView3.setText("ta很懒什么也没留下");
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
