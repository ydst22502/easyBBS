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


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public String email;
    public String password;
    public HashMap<String, String> response = new HashMap<>();
    public Config server = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button1 = (Button) findViewById(R.id.button_loginActivity_login);
        button1.setOnClickListener(this);

        Button button2 = (Button) findViewById(R.id.button_loginActivity_signUp);
        button2.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        /***
         * 点击了登陆按钮
         */
        if (v.getId() == R.id.button_loginActivity_login) {
            EditText editText1 = (EditText) findViewById(R.id.editText_loginActivity_email);
            email = editText1.getText().toString();
            EditText editText2 = (EditText) findViewById(R.id.editText_loginActivity_password);
            password = editText2.getText().toString();
            Log.d("TAG", "editText: " + this.email + " " + this.password);

            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/login",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("TAG", "Json from server: " + response);//response为服务器返回的Json
                            Gson gson = new Gson();
                            VolleyLogin loginResponse = gson.fromJson(response, VolleyLogin.class);

                            Log.d("TAG", "token decode from Json: " + loginResponse.token);
                            if (loginResponse.flag == 1) {
                                //认证成功
                                //存一下userid和token
                                SharedPreferences mySharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = mySharedPreferences.edit();
                                editor.putString("userid", loginResponse.userid);
                                editor.putString("token", loginResponse.token);
                                editor.apply();

                                TextView textView = (TextView) findViewById(R.id.textView_loginActivity_error);
                                textView.setText("正在登陆...");

                                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                Bundle bundle = new Bundle();
                                bundle.putString("userid", loginResponse.userid);
                                bundle.putString("token", loginResponse.token);
                                intent.putExtras(bundle);
                                startActivity(intent);

                            } else {
                                //认证失败
                                TextView textView = (TextView) findViewById(R.id.textView_loginActivity_error);
                                textView.setText("邮箱或密码错误，请重试");
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
                    map.put("email", email);
                    map.put("authkey", password);
                    return map;
                }
            };

            mQueue.add(stringRequest);
        }

        /**********
         * 点击了注册按钮
         */
        if (v.getId() == R.id.button_loginActivity_signUp) {
            EditText editText1 = (EditText) findViewById(R.id.editText_loginActivity_email);
            email = editText1.getText().toString();
            EditText editText2 = (EditText) findViewById(R.id.editText_loginActivity_password);
            password = editText2.getText().toString();

            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/duplication-of-email",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("TAG", "验证邮箱 "+response);
                            if (response.equals("1")) {//没有重复邮箱

                                TextView textView = (TextView) findViewById(R.id.textView_loginActivity_error);
                                textView.setText("请稍后...");

                                Intent intent = new Intent(LoginActivity.this, ChooseUserNameActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("email", email);
                                bundle.putString("password", password);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } else{//有重复邮箱
                                TextView textView = (TextView) findViewById(R.id.textView_loginActivity_error);
                                textView.setText("邮箱已注册过，请勿重复注册");
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
                    map.put("email", email);
                    map.put("authkey", password);
                    return map;
                }
            };

            mQueue.add(stringRequest);
        }

    }

}
