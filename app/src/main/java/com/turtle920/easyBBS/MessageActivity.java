package com.turtle920.easyBBS;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayList<HashMap<String, Object>> listItem;
    SimpleAdapter mSimpleAdapter;

    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mQueue = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
        String userid = sharedPreferences.getString("userid", "");
        String token = sharedPreferences.getString("token", "");

        listView = (ListView) findViewById(R.id.listView_messageActivity_message);
        listItem = new ArrayList<HashMap<String, Object>>();

        mSimpleAdapter = new SimpleAdapter(this, listItem,//需要绑定的数据
                R.layout.item_message,//每一行的布局
                //动态数组中的数据源的键对应到定义布局的View中
                new String[]{"ItemUsername", "ItemContent", "ItemTime"},
                new int[]{R.id.textView_messageActivity_username,
                        R.id.textView_messageActivity_content,
                        R.id.textView_messageActivity_timeStamp
                }
        );

        listView.setAdapter(mSimpleAdapter);
        listView.setOnItemClickListener(this);
        requestAllMessageTo(userid);
    }

    private void requestAllMessageTo(final String userid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "message/all-to",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        List<JsonMessage> messages = gson.fromJson(response, new TypeToken<List<JsonMessage>>() {
                        }.getType());

                        listItem.clear();
                        for (int i=0; i<messages.size(); i++){
                            JsonMessage message = messages.get(i);
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("ItemUsername", message.senderusername);
                            map.put("ItemContent", message.content);
                            map.put("ItemTime", message.timestamp);

                            map.put("senderId", message.senderid);
                            map.put("messageId", message.messageid);
                            listItem.add(map);
                        }
                        mSimpleAdapter.notifyDataSetChanged();
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
                map.put("userid", "" + userid);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MessageActivity.this, SendMessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("receiverId", listItem.get(position).get("senderId").toString());
        bundle.putString("receiverName", listItem.get(position).get("ItemUsername").toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
