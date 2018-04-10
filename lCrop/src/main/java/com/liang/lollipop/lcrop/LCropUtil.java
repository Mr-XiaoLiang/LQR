package com.liang.lollipop.lcrop;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.liang.lollipop.lcrop.activity.CropActivity;
import com.liang.lollipop.lcrop.activity.SelectImagesActivity;

import java.util.ArrayList;

/**
 * Created by Lollipop on 2017/08/02.
 * 图片处理工具集合的对外接口封装
 */
public class LCropUtil {

    public static final int CROP_OK = Activity.RESULT_OK;

    public static final int CROP_REQUEST = 787;

    public static ArrayList<String> getData(int resultCode,Intent data){
        if(resultCode != SelectImagesActivity.RESULT_OK || data == null){
            return new ArrayList<>();
        }
        ArrayList<String> result = data.getStringArrayListExtra(SelectImagesActivity.RESULT_DATA);
        if(result == null){
            return new ArrayList<>();
        }
        return result;
    }

    public static void selectPhotos(Activity activity,int requestCode,String title,boolean showCamera,int maxSize,int minSize,int colSize){
        Intent intent = new Intent(activity, SelectImagesActivity.class);
        intent.putExtra(SelectImagesActivity.ARG_TITLE,title);
        intent.putExtra(SelectImagesActivity.ARG_SHOW_CAMERA,showCamera);
        intent.putExtra(SelectImagesActivity.ARG_MAX_SIZE,maxSize);
        intent.putExtra(SelectImagesActivity.ARG_MIN_SIZE,minSize);
        intent.putExtra(SelectImagesActivity.ARG_COL_SIZE,colSize);
        activity.startActivityForResult(intent,requestCode);
    }

    public static void selectPhotos(Activity activity,int requestCode,String title,int maxSize,int minSize,int colSize){
        selectPhotos(activity,requestCode,title,true,maxSize,minSize,colSize);
    }

    public static void selectPhotos(Activity activity,int requestCode,String title,int maxSize,int colSize){
        selectPhotos(activity,requestCode,title,maxSize,1,colSize);
    }

    public static void selectPhotos(Activity activity,int requestCode,String title,int maxSize){
        selectPhotos(activity,requestCode,title,maxSize,3);
    }

    public static void selectPhotos(Activity activity,int requestCode,String title){
        selectPhotos(activity,requestCode,title,1);
    }

    public static void selectPhotos(Activity activity,int requestCode){
        selectPhotos(activity,requestCode,null,1);
    }

    public static void selectPhotos(Fragment fragment, int requestCode, String title, boolean showCamera, int maxSize, int minSize, int colSize){
        Intent intent = new Intent(fragment.getActivity(), SelectImagesActivity.class);
        intent.putExtra(SelectImagesActivity.ARG_TITLE,title);
        intent.putExtra(SelectImagesActivity.ARG_SHOW_CAMERA,showCamera);
        intent.putExtra(SelectImagesActivity.ARG_MAX_SIZE,maxSize);
        intent.putExtra(SelectImagesActivity.ARG_MIN_SIZE,minSize);
        intent.putExtra(SelectImagesActivity.ARG_COL_SIZE,colSize);
        fragment.startActivityForResult(intent,requestCode);
    }

    public static void selectPhotos(Fragment fragment,int requestCode,String title,int maxSize,int minSize,int colSize){
        selectPhotos(fragment,requestCode,title,true,maxSize,minSize,colSize);
    }

    public static void selectPhotos(Fragment fragment,int requestCode,String title,int maxSize,int colSize){
        selectPhotos(fragment,requestCode,title,maxSize,1,colSize);
    }

    public static void selectPhotos(Fragment fragment,int requestCode,String title,int maxSize){
        selectPhotos(fragment,requestCode,title,maxSize,3);
    }

    public static void selectPhotos(Fragment fragment,int requestCode,String title){
        selectPhotos(fragment,requestCode,title,1);
    }

    public static void selectPhotos(Fragment fragment,int requestCode){
        selectPhotos(fragment,requestCode,null,1);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment, int requestCode, String title, boolean showCamera, int maxSize, int minSize, int colSize){
        Intent intent = new Intent(fragment.getActivity(), SelectImagesActivity.class);
        intent.putExtra(SelectImagesActivity.ARG_TITLE,title);
        intent.putExtra(SelectImagesActivity.ARG_SHOW_CAMERA,showCamera);
        intent.putExtra(SelectImagesActivity.ARG_MAX_SIZE,maxSize);
        intent.putExtra(SelectImagesActivity.ARG_MIN_SIZE,minSize);
        intent.putExtra(SelectImagesActivity.ARG_COL_SIZE,colSize);
        fragment.startActivityForResult(intent,requestCode);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment,int requestCode,String title,int maxSize,int minSize,int colSize){
        selectPhotos(fragment,requestCode,title,true,maxSize,minSize,colSize);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment,int requestCode,String title,int maxSize,int colSize){
        selectPhotos(fragment,requestCode,title,maxSize,1,colSize);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment,int requestCode,String title,int maxSize){
        selectPhotos(fragment,requestCode,title,maxSize,3);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment,int requestCode,String title){
        selectPhotos(fragment,requestCode,title,1);
    }

    public static void selectPhotos(android.support.v4.app.Fragment fragment,int requestCode){
        selectPhotos(fragment,requestCode,null,1);
    }


    public static CropBuilder cropPhoto(String srcPath,String outPath){
        return CropBuilder.create(srcPath, outPath);
    }

    public static class CropBuilder{

        private String imageUrl = "";
        private String outputUrl = "";

        private String title = "";

        private int aspectX = 1;
        private int aspectY = 1;

        private int outputWidth = 500;
        private int outputHeight = 500;

        static CropBuilder create(String imageUrl,String outputUrl){
            return new CropBuilder().setImageUrl(imageUrl).setOutputUrl(outputUrl);
        }

        private CropBuilder(){}

        private CropBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        private CropBuilder setOutputUrl(String outputUrl) {
            this.outputUrl = outputUrl;
            return this;
        }

        public CropBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public CropBuilder setAspect(int aspectX,int aspectY) {
            this.aspectX = aspectX;
            this.aspectY = aspectY;
            return this;
        }

        public CropBuilder setOutput(int outputWidth,int outputHeight) {
            this.outputWidth = outputWidth;
            this.outputHeight = outputHeight;
            return this;
        }

        private Intent createIntent(Activity context){
            try{
                Intent intent = new Intent(context, CropActivity.class);
                intent.putExtra(CropActivity.ARG_IMAGE_URL,imageUrl);
                intent.putExtra(CropActivity.ARG_TITLE,title);
                intent.putExtra(CropActivity.ARG_OUTPUT_URL,outputUrl);
                intent.putExtra(CropActivity.ARG_OUT_HEIGHT,outputHeight);
                intent.putExtra(CropActivity.ARG_OUT_WIDTH,outputWidth);
                intent.putExtra(CropActivity.ARG_ASPECT_X,aspectX);
                intent.putExtra(CropActivity.ARG_ASPECT_Y,aspectY);
                return intent;
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }

        public void start(Activity context){
            context.startActivityForResult(createIntent(context),CROP_REQUEST);
        }

        public void start(Fragment fragment){
            fragment.startActivityForResult(createIntent(fragment.getActivity()),CROP_REQUEST);
        }

        public void start(android.support.v4.app.Fragment fragment){
            fragment.startActivityForResult(createIntent(fragment.getActivity()),CROP_REQUEST);
        }

    }

    public static Uri getUri(Context context,String path){
        Uri resultUri;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            //安卓N以后，文件管理高度私有化，
            // 如果跨应用传递地址，需要使用ContentProvider或FileProvider
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, path);
            resultUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        }else{
            resultUri = Uri.parse(path);
        }
        if(resultUri == null){
            throw new RuntimeException("Uri is null");
        }
        return resultUri;
    }


}
