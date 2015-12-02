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
import android.widget.TextView;
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

public class SendMessageActivity extends AppCompatActivity {

    RequestQueue mQueue;
    String userid;//本人
    String receiverId;//要发给谁
    String receiverName;//要发给的人的名字

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mQueue = Volley.newRequestQueue(getApplicationContext());

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        receiverId = bundle.getString("receiverId");
        receiverName = bundle.getString("receiverName");

        TextView textView = (TextView)findViewById(R.id.textView_sendMessageActivity_to);
        textView.setText(receiverName);

        SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
        userid = sharedPreferences.getString("userid", "");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "message/insert",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("-1")){
                                Toast toast = Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT);
                                toast.show();
                            }else{
                                Toast toast = Toast.makeText(getApplicationContext(), "发送成功", Toast.LENGTH_SHORT);
                                toast.show();
                                Intent intent = new Intent(SendMessageActivity.this, HomePageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    EditText editText = (EditText) findViewById(R.id.editText_sendMessageActivity_content);
                    String content = editText.getText().toString();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("senderid", userid);
                    map.put("receiverid", receiverId);
                    map.put("content", content);
                    return map;
                }
            };
            mQueue.add(stringRequest);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_message, menu);
        return true;
    }

}
