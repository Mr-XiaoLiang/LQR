package com.liang.lollipop.lcrop.bean;

/**
 * Created by Lollipop on 2017/07/31.
 * 图片选择的Bean
 */
public class ImageBean extends BaseBean {

    public static final int TYPE_CAMERA = 0;
    public static final int TYPE_PHOTO = 1;

    public String url = "";
    public String name = "";
    public long dateTime = 0;
    public boolean isChecked = false;
    public int index = 0;

    public ImageBean() {
        type = TYPE_PHOTO;
    }

    public ImageBean(int type) {
        this.type = type;
    }

    public ImageBean(String url, String name, long dateTime) {
        this();
        this.url = url;
        this.name = name;
        this.dateTime = dateTime;
        this.index = 0;
        this.isChecked = false;
    }
}
