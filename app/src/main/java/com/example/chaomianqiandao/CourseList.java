package com.example.chaomianqiandao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chaomianqiandao.Entity.ActiveList;
import com.example.chaomianqiandao.Entity.ChannelList;
import com.example.chaomianqiandao.Entity.Content;
import com.example.chaomianqiandao.Entity.Data;
import com.example.chaomianqiandao.utils.Network;
import com.example.chaomianqiandao.utils.ResponseInfo;

import java.util.ArrayList;
import java.util.List;

public class CourseList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private final static int Get_CourseList=100;
    private final static int Get_TaskList=101;
    private final static int Get_Type=102;
    private final static String TAG="Course_List";
    private List<ChannelList> mList;
    private FirstApplication mFirstApplication=FirstApplication.getInstance();
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Get_CourseList:
                    ResponseInfo info=(ResponseInfo) msg.obj;
                    Log.e(TAG,info.BodyInfo);
                    mList=new ArrayList<>();
                    JSONObject jsonObject= JSONObject.parseObject(info.BodyInfo);
                    JSONArray array = jsonObject.getJSONArray("channelList");
                    for(int i=0;i<array.size();i++){
                        ChannelList temp=array.getObject(i,ChannelList.class);
                        if(temp.getContent().getRoletype()==3&&temp.getContent().getState()==0){
                            mList.add(temp);
                            Content content=temp.getContent();
                            Data data=content.getCourse().getData().get(0);
                            StringBuilder url=new StringBuilder("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist?courseId=");
                            url.append(data.getId()).append("&classId=");
                            url.append(content.getId()).append("&uid=");
                            url.append(mFirstApplication.infoMap.get("uid")).append("&cpi=")
                                    .append(content.getCpi());
                            Network.getSync(url.toString(),handler,Get_TaskList);
                        }
                    }
                    mRecyclerView.setAdapter(new MyAdapter());
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(CourseList.this));
                    break;
                case Get_TaskList:
                    ResponseInfo info1=(ResponseInfo) msg.obj;
                    JSONObject jsonObject1= JSONObject.parseObject(info1.BodyInfo);
                    List<ActiveList> mTaskList=jsonObject1.getJSONArray("activeList").toJavaList(ActiveList.class);
                    for(int i=0;i<mTaskList.size();i++){
                        if(i>2){
                        //默认检查前三个任务
                            break;
                        }
                        ActiveList activeList=mTaskList.get(i);
                        if (activeList.getActiveType()==2&&activeList.getStatus()==1){   //活动为签到  签到未结束
                            Toast.makeText(mFirstApplication, "发现签到！", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,"发现签到！！！");
                            String url="https://mobilelearn.chaoxing.com/newsign/signDetail?activePrimaryId="+activeList.getId()+"&type=1";
                            Network.getSync(url,handler,Get_Type);
                            break;
                        }
                    }
                    break;
                case Get_Type:
                    ResponseInfo info2=(ResponseInfo) msg.obj;
                    JSONObject jsonObject2=JSONObject.parseObject(info2.BodyInfo);
                    Intent intent=new Intent(CourseList.this,Sign.class);
                    switch (jsonObject2.getInteger("otherId")) {
                        case 0:
                            if (jsonObject2.getInteger("ifPhoto") == 0) {
                                //普通签到
                                intent.putExtra("sign_name", "普通签到");
                                intent.putExtra("sign_type", 0);
                            } else {
                                //拍照签到
                                intent.putExtra("sign_name", "拍照签到");
                                intent.putExtra("sign_type", 1);
                            }
                            break;
                        case 2:
                            if (jsonObject2.getInteger("ifRefreshEwm") == 0) {
                                //二维码签到  不刷新
                                intent.putExtra("sign_name", "二维码签到");
                                intent.putExtra("sign_type", 2);
                            } else {
                                //二维码签到  10s刷新
                                intent.putExtra("sign_name", "二维码签到");
                                intent.putExtra("sign_type", 3);
                            }
                            break;
                        case 3:
                            //手势签到
                            intent.putExtra("sign_name", "手势签到");
                            intent.putExtra("sign_type", 4);
                            break;
                        case 4:
                            //定位签到
                            intent.putExtra("sign_name", "定位签到");
                            intent.putExtra("sign_type", 5);
                            intent.putExtra("content", jsonObject2.getString("content"));
                            break;
                        case 5:
                            //签到码签到
                            intent.putExtra("sign_name", "签到码签到");
                            intent.putExtra("sign_type", 6);
                            break;
                    }
                    //activeId
                    intent.putExtra("aid",jsonObject2.getString("id"));
                    intent.putExtra("courseId",getIntent().getLongExtra("courseId",0));
                    intent.putExtra("classId",getIntent().getLongExtra("classId",0));
                    intent.putExtra("cpi",getIntent().getLongExtra("cpi",0));
                    //签到码  签到手势或者签到码
                    intent.putExtra("sign_code",jsonObject2.getString("signCode"));
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        mRecyclerView = findViewById(R.id.course_list);
        //获取课程列表
        Network.getSync("http://mooc1-api.chaoxing.com/mycourse/backclazzdata",handler,Get_CourseList);
    }


    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=View.inflate(CourseList.this,R.layout.course_item,null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ChannelList channelList=mList.get(position);
            Content content=channelList.getContent();
            Data data=content.getCourse().getData().get(0);
            holder.course.setText(data.getName());
            holder.teacher.setText(data.getTeacherfactor());
            Glide.with(CourseList.this)
                    .load(data.getImageurl())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon);
            holder.linear.setOnClickListener(v -> {
                Intent intent=new Intent(CourseList.this,TaskDetail.class);
                intent.putExtra("courseId",data.getId());
                intent.putExtra("classId",content.getId());
                intent.putExtra("cpi",content.getCpi());
                intent.putExtra("name",data.getName());
                startActivity(intent);
            });
        }
        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        com.example.chaomianqiandao.utils.ImageViewM icon;
        TextView course;
        TextView teacher;
        LinearLayout linear;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.course_icon);
            course = itemView.findViewById(R.id.course_name);
            teacher = itemView.findViewById(R.id.course_teacher);
            linear = itemView.findViewById(R.id.course_linear);
        }
    }
}

