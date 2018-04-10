package com.liang.lollipop.lcrop.util;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.liang.lollipop.lcrop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lollipop on 2016/12/9.
 * 权限检查工具类
 */
public class PermissionsUtil {

    /**
     * GMS相关权限未罗列,如果需要可以使用 adb shell pm list permissions -g -d 命令查看
     * 据文档说明,同一个权限组中的某个权限被授权,那么同组其他权限也会被授权.
     * 即,权限申请是以组的形式存在.
     * 部分基本权限因不需要申请,所以未声明
     */

    /**
     * 日历权限
     * （日历读写权限）
     */
    public final static String CALENDAR = Manifest.permission_group.CALENDAR;

    public final static String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public final static String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;
    //写入设置权限
    public final static String WRITE_SETTINGS = Manifest.permission.WRITE_SETTINGS;

    /**
     * 相机使用权限
     * （相机相关，包括闪光灯）
     */
    public final static String CAMERA = Manifest.permission_group.CAMERA;

    public final static String CAMERA_ = Manifest.permission.CAMERA;


    /**
     * 通讯录权限
     * （读写联系人，获取账户）
     */
    public final static String CONTACTS = Manifest.permission_group.CONTACTS;

    public final static String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    public final static String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    public final static String READ_CONTACTS = Manifest.permission.READ_CONTACTS;



    /**
     * 位置权限
     */
    public final static String LOCATION = Manifest.permission_group.LOCATION;

    public final static String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public final static String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;


    /**
     * 麦克风权限
     */
    public final static String MICROPHONE = Manifest.permission_group.MICROPHONE;

    public final static String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    /**
     * 获取手机状态
     * （读取手机状态等）
     */
    public final static String PHONE = Manifest.permission_group.PHONE;

    public final static String READ_CALL_LOG = Manifest.permission.READ_CALL_LOG;
    public final static String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public final static String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public final static String WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG;
    public final static String USE_SIP = Manifest.permission.USE_SIP;
    public final static String PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS;
    public final static String ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL;

    /**
     * 传感器
     */
    public final static String SENSORS = Manifest.permission_group.SENSORS;

    public final static String BODY_SENSORS = Manifest.permission.BODY_SENSORS;


    /**
     * 短信权限
     * (读发短信)
     */
    public final static String SMS = Manifest.permission_group.SMS;

    public final static String READ_SMS = Manifest.permission.READ_SMS;
    public final static String RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH;
    public final static String RECEIVE_MMS = Manifest.permission.RECEIVE_MMS;
    public final static String RECEIVE_SMS = Manifest.permission.RECEIVE_SMS;
    public final static String SEND_SMS = Manifest.permission.SEND_SMS;
//    public final static String READ_CELL_BROADCASTS = Manifest.permission.READ_CELL_BROADCASTS;

    /**
     * 储存权限
     * (文件的读取和写入)
     */
    public final static String STORAGE = Manifest.permission_group.STORAGE;

    public final static String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public final static String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * 获取权限集中需要申请权限的列表
     */
    public static List<String> findDeniedPermissions(Activity activitie, String... permissions) {
        List<String> needRequestPermissonList = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activitie,perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    activitie, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    /**
     * 检查权限,activtiy调用本方法
     */
    public static void checkPermissions(Activity activity, int requestCode, OnPermissionsPass onPermissionsPass, String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(activity,permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),requestCode);
        }else{
            if(onPermissionsPass!=null){
                onPermissionsPass.onPermissionsPass();
            }
        }
    }

    public static boolean checkPermissions(Activity activity, String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(activity,permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            return false;
        }else{
            return true;
        }
    }

    public static void checkPermissions(Activity activity, int requestCode, String... permissions){
        checkPermissions(activity,requestCode,null,permissions);
    }

    /**
     * 检测是否所有的权限都已经授权
     */
    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查未授权的权限
     */
    public static ArrayList<String> verifyPermissions(String[] permissions, int[] grantResults) {
        ArrayList<String> pers = new ArrayList<>();
        for (int i = 0;i<grantResults.length;i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                pers.add(permissions[i]);
            }
        }
        return pers;
    }

    /**
     * 方便分装做的一个回调,用于在权限直接通过时使用
     */
    public interface OnPermissionsPass{
        void onPermissionsPass();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    public static void startAppSettings(Context context) {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    /**
     * 弹出权限申请对话框
     * @param msg 消息内容
     */
    public static void popPermissionsDialog(final Context context, String msg){
        new AlertDialog.Builder(context).setTitle(context.getString(R.string.dialog_title_warning))
                .setMessage(msg)
                .setPositiveButton(context.getString(R.string.per_btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings(context);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.per_btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

}
