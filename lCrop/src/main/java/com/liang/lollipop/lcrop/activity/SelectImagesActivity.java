package com.liang.lollipop.lcrop.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.liang.lollipop.lcrop.R;
import com.liang.lollipop.lcrop.bean.ImageBean;
import com.liang.lollipop.lcrop.fragment.SelectImagesFragment;
import com.liang.lollipop.lcrop.util.FileUtils;
import com.liang.lollipop.lcrop.util.PermissionsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 选择图片的Activity
 * @author Lollipop on 2017-07-31
 */
public class SelectImagesActivity extends BaseActivity implements SelectImagesFragment.OnImagesSelectedListener {

    private static final int REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 789;
    private static final int REQUEST_PERMISSIONS_CAMERA = 787;

    private static final int REQUEST_CAMERA = 786;
    /**
     * 列数的参数，用于设置图片选择页面的列数
     * 要求类型为Int
     * 默认为3
     */
    public static final String ARG_COL_SIZE = "ARG_COL";
    /**
     * 最大的图片选择数量
     * 要求类型为Int
     * 默认为1
     */
    public static final String ARG_MAX_SIZE = "ARG_MAX_SIZE";
    /**
     * 最少的图片选择数量
     * 要求类型为Int
     * 默认为1
     */
    public static final String ARG_MIN_SIZE = "ARG_MIN_SIZE";
    /**
     * 设置是否显示相机按钮
     * 要求为Boolean格式
     * 默认为true
     */
    public static final String ARG_SHOW_CAMERA = "ARG_SHOW_CAMERA";
    /**
     * 设置图片选择页面的标题
     * 要求类型为String
     * 默认为：图片选择
     */
    public static final String ARG_TITLE = "ARG_TITLE";
    /**
     * 图片选择的结果
     * 类型为ArrayList<String>
     * 不为空
     */
    public static final String RESULT_DATA = "RESULT_DATA";
    /**
     * 图片选择的正确状态，
     * 值与Activity的默认值相同
     */
    public static final int RESULT_OK = 89;

    private int selectedSize = 0;
    private int maxSize = 0;
    private int minSize = 0;

    private TextView enterBtn;

    private SelectImagesFragment fragment;

    private File tempPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_images);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_select_images_toolbar);
        setSupportActionBar(toolbar);
        initView();
        initData();
        onSelectedSizeChange();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPhotoPermissions();
    }

    private void initView(){
        enterBtn = (TextView) findViewById(R.id.activity_select_images_enter_btn);
        enterBtn.setOnClickListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = (SelectImagesFragment) fragmentManager.findFragmentById(R.id.activity_select_images_fragment);
    }

    private void initData(){
        Intent intent = getIntent();
        String title = intent.getStringExtra(ARG_TITLE);
        if(TextUtils.isEmpty(title)) {
            title = getString(R.string.title_activity_select_images);
        }
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(title);
        }
        maxSize = intent.getIntExtra(ARG_MAX_SIZE,1);
        minSize = intent.getIntExtra(ARG_MIN_SIZE,1);
        int colSize = intent.getIntExtra(ARG_COL_SIZE, 3);
        boolean isShowCamera = intent.getBooleanExtra(ARG_SHOW_CAMERA, true);
        if(fragment==null) {
            return;
        }
        fragment.setColumns(colSize);
        fragment.setMaxSelectedSize(maxSize);
        fragment.setShowCamera(isShowCamera);
    }

    @Override
    public void onImageSelected(int size, int index, int position, String url) {
        selectedSize = size;
        onSelectedSizeChange();
    }

    @Override
    public void onCallCamera() {
        checkCameraPermissions();
    }

    private void onSelectedSizeChange(){
        if(selectedSize<minSize||selectedSize>maxSize){
            enterBtn.setEnabled(false);
        }else{
            enterBtn.setEnabled(true);
        }
        enterBtn.setText(getString(R.string.action_button_string,getString(R.string.action_done),selectedSize,maxSize));
    }

    private boolean getPhotoPermissions(){
        return PermissionsUtil.checkPermissions(this,PermissionsUtil.READ_EXTERNAL_STORAGE);
    }

    private void checkPhotoPermissions(){
        if(getPhotoPermissions()){
            onPhotoPermissionsGrant();
        }else{
            PermissionsUtil.checkPermissions(
                    this,
                    REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE,
                    PermissionsUtil.READ_EXTERNAL_STORAGE);
        }
    }

    private void onPhotoPermissionsGrant(){
        if(fragment!=null) {
            fragment.setLoaderManager(getSupportLoaderManager());
        }
    }

    private boolean getCameraPermissions(){
        return PermissionsUtil.checkPermissions(this,PermissionsUtil.WRITE_EXTERNAL_STORAGE,PermissionsUtil.CAMERA_);
    }

    private void checkCameraPermissions(){
        if(getCameraPermissions()){
            onCameraPermissionsGrant();
        }else{
            PermissionsUtil.checkPermissions(
                    this,
                    REQUEST_PERMISSIONS_CAMERA,
                    PermissionsUtil.WRITE_EXTERNAL_STORAGE,
                    PermissionsUtil.CAMERA_);
        }
    }

    private void onCameraPermissionsGrant(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                tempPhoto = FileUtils.createTmpFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tempPhoto != null && tempPhoto.exists()) {
                if(Build.VERSION.SDK_INT>=24){
                    //安卓N以后，文件管理高度私有化，
                    // 如果跨应用传递地址，需要使用ContentProvider或FileProvider
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, tempPhoto.getAbsolutePath());
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }else{
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempPhoto));
                }
                startActivityForResult(intent, REQUEST_CAMERA);
            } else {
                T(getString(R.string.error_image_not_exist));
            }
        } else {
            T(getString(R.string.msg_no_camera));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (tempPhoto != null) {
                    // notify system the image has change
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempPhoto)));
                }
            }else if (tempPhoto != null && tempPhoto.exists()){
                boolean success = tempPhoto.delete();
                if(success){
                    tempPhoto = null;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                if(!getPhotoPermissions()){
                    PermissionsUtil.popPermissionsDialog(this,getString(R.string.alert_photo_permissions));
                    return;
                }
                onPhotoPermissionsGrant();
                break;
            case REQUEST_PERMISSIONS_CAMERA:
                if(!getCameraPermissions()){
                    PermissionsUtil.popPermissionsDialog(this,getString(R.string.alert_camera_permissions));
                    return;
                }
                onCameraPermissionsGrant();
                break;

                default:break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.activity_select_images_enter_btn){
            onSuccess();
        }
    }

    private void onSuccess(){
        if(fragment==null) {
            return;
        }
        ArrayList<ImageBean> imageBeen = fragment.getSelectImageBeen();
        if(imageBeen==null||imageBeen.size()<minSize){
            T(getString(R.string.error_image_size_too_little));
            return;
        }
        if(imageBeen.size()>minSize){
            T(getString(R.string.error_image_size_too_much));
            return;
        }
        ArrayList<String> imgUrls = new ArrayList<>();
        for(ImageBean imageBean : imageBeen){
            imgUrls.add(imageBean.url);
        }
        Intent intent = new Intent();
        intent.putExtra(RESULT_DATA,imgUrls);
        setResult(RESULT_OK,intent);
        finish();
    }

}
