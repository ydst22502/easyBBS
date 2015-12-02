package com.turtle920.easyBBS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

public class DetailActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    RequestQueue mQueue;
    long postid;
    String useridOfThisPost;//发帖人信息，用来传到intent里

    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private SimpleAdapter mSimpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        postid = Long.parseLong(bundle.getString("postid"));

        listView = (ListView) findViewById(R.id.listView_detailActivity_reply);
        listItem = new ArrayList<HashMap<String, Object>>();

        mSimpleAdapter = new SimpleAdapter(this, listItem,//需要绑定的数据
                R.layout.item_detail,//每一行的布局
                //动态数组中的数据源的键对应到定义布局的View中
                new String[]{"ItemImage", "ItemUsername", "ItemContent", "ItemTime"},
                new int[]{R.id.imageView_detailActivity_replyList_avatar,
                        R.id.textView_detailActivity_replyList_username,
                        R.id.textView_detailActivity_replyList_content,
                        R.id.textView_detailActivity_replyList_replyTime
                }
        );

        listView.setAdapter(mSimpleAdapter);
        listView.setOnItemClickListener(this);

        mQueue = Volley.newRequestQueue(getApplicationContext());
        requestPostContent(postid);//请求post详情
        requestReplyOfThisPost(postid);//请求reply内容

        View view1 = (View)findViewById(R.id.linearLayout_detailActivity_poster);
        view1.setOnClickListener(this);

        Button button = (Button)findViewById(R.id.button_detailActivity_reply);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_detailActivity_reply){
            EditText editText = (EditText)findViewById(R.id.editText_detailActiviy_reply);
            String replyContent = editText.getText().toString();

            SharedPreferences sharedPreferences = getSharedPreferences("login", Activity.MODE_PRIVATE);
            String userid = sharedPreferences.getString("userid", "");
            String token = sharedPreferences.getString("token", "");

            Log.d("TAG", "reply");
            insertReply(postid, Long.parseLong(userid), replyContent);

        }

        if (v.getId() == R.id.linearLayout_detailActivity_poster){
            Intent intent = new Intent(DetailActivity.this, PersonActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userid", useridOfThisPost);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击回复该用户
    }

    private void insertReply(final long postid, final long userid, final String content){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "reply/reply",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        EditText editText = (EditText)findViewById(R.id.editText_detailActiviy_reply);
                        if (response.equals("-1")){
                            editText.setText("回复失败");
                        } else {
                            editText.setText(null);
                            editText.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0) ;

                            requestReplyOfThisPost(postid);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                EditText editText = (EditText)findViewById(R.id.editText_detailActiviy_reply);
                editText.setText("回复失败");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("postid", "" + postid);
                map.put("userid", "" + userid);
                map.put("content", "" + content);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }

    private void requestPostContent(final long postid) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "post/ask-by-postid",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", response);
                        Gson gson = new Gson();
                        JsonDetail detail = gson.fromJson(response, JsonDetail.class);

                        requestUserInfo(detail.userid);//请求发帖用户信息
                        useridOfThisPost = ""+detail.userid;

                        TextView textView3 = (TextView) findViewById(R.id.textView_detailActivity_title);
                        textView3.setText(detail.title);

                        TextView textView4 = (TextView) findViewById(R.id.textView_detailActivity_content);
                        textView4.setText(detail.content);

                        TextView textView5 = (TextView) findViewById(R.id.textView_detailActivity_posttime);
                        textView5.setText(detail.posttime);
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
                map.put("postid", "" + postid);
                return map;
            }
        };

        mQueue.add(stringRequest);

    }

    private void requestUserInfo(final long userid) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "user/get-userinfo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        JsonUserinfo userinfo = gson.fromJson(response, JsonUserinfo.class);
                        TextView textView1 = (TextView) findViewById(R.id.textView_detailActivity_username);
                        textView1.setText(userinfo.username);

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

    private void requestReplyOfThisPost(final long postid) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.BASE_URL + "reply/all-of-the-postid",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView textView1 = (TextView) findViewById(R.id.textView_detailActivity_replyCount);

                        if (!response.equals("-1")) {//回复非空
                            Gson gson = new Gson();
                            List<JsonReplyInfo> replyInfos = gson.fromJson(response, new TypeToken<List<JsonReplyInfo>>() {
                            }.getType());

                            textView1.setText(replyInfos.size() + " replies");

                            listItem.clear();
                            for (int i = 0; i < replyInfos.size(); i++) {
                                JsonReplyInfo replyInfo = replyInfos.get(i);

                                HashMap<String, Object> map = new HashMap<String, Object>();
                                //"ItemImage", "ItemUsername", "ItemContent", "ItemTime"},
                                map.put("ItemImage", R.drawable.my_avatar);//加入图片
                                map.put("ItemUsername", replyInfo.username);
                                map.put("ItemContent", replyInfo.content);
                                map.put("ItemTime", replyInfo.replytime);
                                //这个地方精髓哈，压replyId到数据数组里面，但是不和adapter绑定
                                map.put("ItemReplyId", replyInfo.replyid);
                                listItem.add(map);
                            }

                            mSimpleAdapter.notifyDataSetChanged();
                        } else {
                            textView1.setText("no reply yet");
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
                map.put("postid", "" + postid);
                return map;
            }
        };

        mQueue.add(stringRequest);
    }

}
