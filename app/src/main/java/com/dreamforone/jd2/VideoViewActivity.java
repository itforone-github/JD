package com.dreamforone.jd2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dreamforone.jd2.R;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.RetrofitService;
import util.ServerPost;

public class VideoViewActivity extends AppCompatActivity {
    ImageView imageUrlView;
    VideoView videoView;
    boolean touchBoolean=false,garoseroBoolean=false;
    String mb_id,bo_table,wr_id,videoUrl,imageUrl;
    MediaController controller;
    boolean finish=false;
    Button closeBtn,btn1,btn2;
    LinearLayout topLayout;
    ImageView modeBtn;
    LinearLayout videoViewLayout,imageViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        imageUrlView=(ImageView)findViewById(R.id.imageUrlView);
        imageViewLayout=(LinearLayout)findViewById(R.id.imageViewLayout);
        videoView=(VideoView)findViewById(R.id.videoView);
        videoViewLayout=(LinearLayout)findViewById(R.id.videoViewLayout);
        controller=new MediaController(this);
        //controller.setVisibility(View.GONE);
        //videoView.setMediaController(controller);
        videoView.setOnCompletionListener(completionListener);
        modeBtn=(ImageView)findViewById(R.id.modeBtn);
        closeBtn=(Button)findViewById(R.id.closeBtn);
        btn1=(Button)findViewById(R.id.btn1);
        btn2=(Button)findViewById(R.id.btn2);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        videoViewLayout.setOnClickListener(mOnClickListener);

        modeBtn.setOnClickListener(mOnClickListener);
        btn1.setOnClickListener(mOnClickListener);
        btn2.setOnClickListener(mOnClickListener);
        topLayout=(LinearLayout)findViewById(R.id.topLayout);


        mb_id=getIntent().getExtras().getString("mb_id");
        bo_table=getIntent().getExtras().getString("bo_table");
        wr_id=getIntent().getExtras().getString("wr_id");
        videoUrl=getIntent().getExtras().getString("videoUrl");
        imageUrl=getIntent().getExtras().getString("imageUrl");
        videoView.setVideoPath(videoUrl );
        
        videoView.start();
        videoView.isPlaying();

        if(bo_table.equals("radio")){
            imageViewLayout.setVisibility(View.VISIBLE);

            Glide.with(VideoViewActivity.this)
                    .load(imageUrl)
                    .into(imageUrlView);
        }else{

        }
    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        Log.d("result","result");
        Intent intent = new Intent();
        intent.putExtra("finish",finish);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    MediaPlayer.OnCompletionListener completionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            closeBtn.setVisibility(View.VISIBLE);
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl(getString(R.string.domain))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            //서버에 보낼 파라미터
            Map map=new HashMap();
            map.put("division","ad_count");
            map.put("bo_table", bo_table);
            map.put("mb_id",mb_id);
            map.put("wr_id",wr_id);
            RetrofitService retrofitService=retrofit.create(RetrofitService.class);
            Call<ServerPost> call=retrofitService.getPush(map);

            call.enqueue(new Callback<ServerPost>() {
                @Override
                public void onResponse(Call<ServerPost> call, Response<ServerPost> response) {
                    //서버에 데이터 받기가 성공할시
                    if(response.isSuccessful()){
                        finish=true;
                        ServerPost repo=response.body();

                        Log.d("winBoolean",repo.getWinBoolean()+"");
                        if(Boolean.parseBoolean(repo.getWinBoolean())==true){
                            AlertDialog.Builder builder=new AlertDialog.Builder(VideoViewActivity.this);
                            builder.setTitle("시청 당첨자")
                                    .setMessage(repo.getMessage())
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    }else{

                    }
                }
                //데이터 받기가 실패할 시
                @Override
                public void onFailure(Call<ServerPost> call, Throwable t) {

                }
            });
        }
    };
    View.OnClickListener mOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.videoViewLayout:
                    if(!touchBoolean){
                        topLayout.setVisibility(View.VISIBLE);
                        touchBoolean=true;
                    }else{
                        topLayout.setVisibility(View.GONE);
                        touchBoolean=false;
                    }
                    break;
                case R.id.modeBtn:
                    if(garoseroBoolean==false){
                        VideoViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        garoseroBoolean=true;
                    }else{
                        VideoViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        garoseroBoolean=false;
                    }
                    break;
                case R.id.btn1:
                    btn1.setVisibility(View.GONE);
                    btn2.setVisibility(View.VISIBLE);
                    VideoViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case R.id.btn2:
                    btn2.setVisibility(View.GONE);
                    btn1.setVisibility(View.VISIBLE);
                    VideoViewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;

            }
        }
    };
}
