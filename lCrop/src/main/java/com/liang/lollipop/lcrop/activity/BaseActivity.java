package com.liang.lollipop.lcrop.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

/**
 * Created by Lollipop on 2017/07/17.
 * 基础Activity
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean showBack = true;

    private View rootView = null;

    protected RequestManager glide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glide = Glide.with(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        findRootView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        findRootView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        findRootView();
    }

    private void findRootView(){
        //获取根节点View，用于弹出SnackBar
        ViewGroup contentParent = (ViewGroup) findViewById(android.R.id.content);
        rootView = contentParent.getChildCount()>0?contentParent.getChildAt(0):contentParent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(showBack&&getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setFullScreen(boolean isFull){
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        View decorView = getWindow().getDecorView();
        if(isFull){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); //清除非全屏的flag
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //设置全屏的flag
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //清除全屏的flag
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); //非全屏
            int uiOptions = 0;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    protected void setScreenOrientation(boolean isLandscape){
        if(isLandscape){//横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else{//竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected void setShowActionBar(boolean isShow){
        if(isShow){
            if(getSupportActionBar()!=null&&!getSupportActionBar().isShowing()) {
                getSupportActionBar().show();
            }
        }else{
            if(getSupportActionBar()!=null&&getSupportActionBar().isShowing()) {
                getSupportActionBar().hide();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 输出Toast
     * @param str 输出内容
     */
    protected void T(String str){
//        ToastUtil.T(this,str);
        S(str);
    }

    protected void S(View view, String msg, String btnName, final View.OnClickListener btnClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(msg);
        if(!TextUtils.isEmpty(btnName) && btnClick != null){
            builder.setPositiveButton(btnName, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btnClick.onClick(null);
                }
            });
        }
        builder.show();
    }

    protected void S(String msg, String btnName, View.OnClickListener btnClick){
        S(rootView,msg,btnName,btnClick);
    }

    protected void S(View view,String msg){
        S(view,msg, "", null);
    }

    protected void S(String msg){
        S(msg, "", null);
    }

    protected void startActivity(Intent intent, Pair<View,String>... pair) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,pair);
        super.startActivity(intent,optionsCompat.toBundle());
    }

}
