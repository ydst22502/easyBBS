package com.turtle920.easyBBS;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ChooseUserNameActivity extends AppCompatActivity implements View.OnClickListener {

    String email;
    String password;
    String username;

    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user_name);

        mQueue = Volley.newRequestQueue(getApplicationContext());

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        email = bundle.getString("email");
        password = bundle.getString("password");

        Button button = (Button) findViewById(R.id.button_chooseUserNameActivity_finish);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_chooseUserNameActivity_finish) {
            EditText editText1 = (EditText) findViewById(R.id.editText_chooseUserNameActivity_username);
            username = editText1.getText().toString();

            checkDuplicationOfName(username);

        }
    }

    private void checkDuplicationOfName(final String username){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/duplication-of-name",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", "choose name response: " + response);
                        if (response.equals("1")) {//没有重名


                            TextView textView = (TextView) findViewById(R.id.textView_chooseUserNameActivity_error);
                            textView.setText("正在创建新用户...");

                            /*********************插入数据库******************************/
                            insertNewUserIntoDB(username, email, password);

                        } else {//有重名
                            TextView textView = (TextView) findViewById(R.id.textView_chooseUserNameActivity_error);
                            textView.setText("有人已经霸占这个名字了，换一个咯");
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
                map.put("username", username);
                Log.d("TAG", "map put username: " + username);
                return map;
            }
        };

        mQueue.add(stringRequest);
    }

    private void insertNewUserIntoDB(final String username, final String email, final String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/create",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", response);//response为服务器返回的userid 和token的Json
                        Gson gson = new Gson();
                        VolleyLogin insertResponse = gson.fromJson(response, VolleyLogin.class);

                        /***********
                         * 存一下userid 和token
                         ***********/
                        SharedPreferences mySharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mySharedPreferences.edit();
                        editor.putString("userid", insertResponse.userid);
                        editor.putString("token", insertResponse.token);
                        editor.apply();

                        /***********
                         * 跳转到homepage
                         ***********/
                        Intent intent = new Intent(ChooseUserNameActivity.this, HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid", insertResponse.userid);
                        bundle.putString("token", insertResponse.token);
                        intent.putExtras(bundle);
                        startActivity(intent);

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
                map.put("username", username);
                map.put("email", email);
                map.put("authkey", password);
                return map;
            }
        };

        mQueue.add(stringRequest);
    }
}
