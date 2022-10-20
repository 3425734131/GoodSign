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
import android.widget.FrameLayout;
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
import com.example.chaomianqiandao.refresh.BounceCallBack;
import com.example.chaomianqiandao.refresh.BounceLayout;
import com.example.chaomianqiandao.refresh.NormalBounceHandler;
import com.example.chaomianqiandao.refresh.footer.DefaultFooter;
import com.example.chaomianqiandao.refresh.header.DefaultHeader;
import com.example.chaomianqiandao.utils.Network;
import com.example.chaomianqiandao.utils.ResponseInfo;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskDetail extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private final static int Get_TaskList=100;
    private final static int Get_Type=101;
    private final static String TAG="Task_List";
    private List<ActiveList> mList;
    private FirstApplication mFirstApplication=FirstApplication.getInstance();
    private HashMap<String, String> aid_url=new HashMap<>();
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Get_TaskList:
                    ResponseInfo info=(ResponseInfo) msg.obj;
                    JSONObject jsonObject= JSONObject.parseObject(info.BodyInfo);
                    mList=jsonObject.getJSONArray("activeList").toJavaList(ActiveList.class);
                    mRecyclerView.setAdapter(new MyAdapter());
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(TaskDetail.this));
                    bounceLayout.setRefreshCompleted();
                    break;
                case Get_Type:
                    ResponseInfo info1=(ResponseInfo) msg.obj;
                    JSONObject jsonObject1=JSONObject.parseObject(info1.BodyInfo);
                    Intent intent=new Intent(TaskDetail.this,Sign.class);
                    switch (jsonObject1.getInteger("otherId")){
                        case 0:
                            if(jsonObject1.getInteger("ifPhoto")==0){
                                //普通签到
                                intent.putExtra("sign_name","普通签到");
                                intent.putExtra("sign_type",0);
                            }else {
                                //拍照签到
                                intent.putExtra("sign_name","拍照签到");
                                intent.putExtra("sign_type",1);
                            }
                            break;
                        case 2:
                            if(jsonObject1.getInteger("ifRefreshEwm")==0){
                                //二维码签到  不刷新
                                intent.putExtra("sign_name","二维码签到");
                                intent.putExtra("sign_type",2);
                            }else {
                                //二维码签到  10s刷新
                                intent.putExtra("sign_name","二维码签到");
                                intent.putExtra("sign_type",3);
                            }
                            break;
                        case 3:
                            //手势签到
                            intent.putExtra("sign_name","手势签到");
                            intent.putExtra("sign_type",4);
                            break;
                        case 4:
                            //定位签到
                            intent.putExtra("sign_name","定位签到");
                            intent.putExtra("sign_type",5);
                            intent.putExtra("content",jsonObject1.getString("content"));
                            break;
                        case 5:
                            //签到码签到
                            intent.putExtra("sign_name","签到码签到");
                            intent.putExtra("sign_type",6);
                            break;
                    }
                    //activeId
                    intent.putExtra("aid",jsonObject1.getString("id"));
                    intent.putExtra("url",aid_url.get(jsonObject1.getString("id")));
                    intent.putExtra("name",getIntent().getStringExtra("name"));
                    //签到码  签到手势或者签到码
                    intent.putExtra("sign_code",jsonObject1.getString("signCode"));
                    startActivity(intent);
                    break;
            }
        }
    };
    private BounceLayout bounceLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        mRecyclerView=findViewById(R.id.task_list);
        //课程名称
        TextView Name= findViewById(R.id.task_name);
        //取消按钮
        findViewById(R.id.task_cancel).setOnClickListener(v -> finish());
        Intent intent= getIntent();
        Name.setText(intent.getStringExtra("name"));
        StringBuilder url=new StringBuilder("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist?courseId=");
        url.append(intent.getLongExtra("courseId",0)).append("&classId=");
        url.append(intent.getLongExtra("classId",0)).append("&uid=");
        url.append(mFirstApplication.infoMap.get("uid")).append("&cpi=")
                .append(intent.getStringExtra("cpi"));
        Network.getSync(url.toString(),handler,Get_TaskList);
        Log.e(TAG,url.toString());

        //刷新空间加载
        FrameLayout frameLayout=findViewById(R.id.task_frameLayout);
        bounceLayout = findViewById(R.id.task_bounce);
        bounceLayout.setHeaderView(new DefaultHeader(this),frameLayout);
        bounceLayout.setFooterView(new DefaultFooter(this),frameLayout);
        bounceLayout.setBounceHandler(new NormalBounceHandler(),mRecyclerView);
        bounceLayout.setEventForwardingHelper((downX, downY, moveX, moveY) -> true);
        //加载刷新回调函数
        bounceLayout.setBounceCallBack(new BounceCallBack() {
            @Override
            public void startRefresh() {
                Network.getSync(url.toString(),handler,Get_TaskList);
            }

            @Override
            public void startLoadingMore() {
                bounceLayout.setLoadingMoreCompleted();
            }
        });

    }


    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=View.inflate(TaskDetail.this,R.layout.course_item,null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ActiveList activeList=mList.get(position);
            holder.course.setText(activeList.getNameOne());
            holder.teacher.setText(activeList.getNameTwo());
            aid_url.put(activeList.getId(),activeList.getUrl());
            Glide.with(TaskDetail.this)
                    .load(activeList.getPicUrl())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon);

                    holder.linear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (activeList.getActiveType()==2){
                                if(activeList.getStatus()==1){
                                    String url="https://mobilelearn.chaoxing.com/newsign/signDetail?activePrimaryId="+activeList.getId()+"&type=1";
                                     Network.getSync(url,handler,Get_Type);
                                }else {
                                    Toast.makeText(mFirstApplication, "签到已结束....", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(mFirstApplication, "不支持签到以外的活动！", Toast.LENGTH_SHORT).show();
                            }
                        }
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