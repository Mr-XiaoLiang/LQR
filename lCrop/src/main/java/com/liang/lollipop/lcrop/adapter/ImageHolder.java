package com.liang.lollipop.lcrop.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.liang.lollipop.lcrop.R;
import com.liang.lollipop.lcrop.bean.BaseBean;
import com.liang.lollipop.lcrop.bean.ImageBean;

/**
 * Created by Lollipop on 2017/07/31.
 * 图片选择的Holder
 * @author lollipop
 */
public class ImageHolder extends BaseHolder{

    public static final int LAYOUT_ID = R.layout.item_image;
    public static final int BODY_ID = R.id.item_image_body;

    private RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);

    private ImageView imageView;
    private CheckBox checkBox;
    private int height = 0;
    private ImageView maskView;

    ImageHolder(View itemView) {
        super(itemView);
        itemView.findViewById(BODY_ID).setOnClickListener(this);
        imageView = (ImageView) itemView.findViewById(R.id.item_image_img);
        checkBox = (CheckBox) itemView.findViewById(R.id.item_image_check);
        maskView = (ImageView) itemView.findViewById(R.id.item_image_mask);
    }

    public void setImageHeight(int height){
        if(this.height==height) {
            return;
        }
        this.height = height;
        imageView.setMinimumHeight(height);
    }

    @Override
    public void onBind(BaseBean bean) {
        super.onBind(bean);
        if(bean instanceof ImageBean) {
            onBind((ImageBean)bean);
        }
    }

    public void onBind(ImageBean bean) {
        if(bean.type==ImageBean.TYPE_CAMERA){
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.ic_camera_white_48dp);
            checkBox.setVisibility(View.INVISIBLE);
            return;
        }else{
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            checkBox.setVisibility(View.VISIBLE);
        }
        if(glide!=null) {
            glide.load(bean.url).apply(requestOptions).into(imageView);
        }
        if(bean.isChecked&&bean.index>0){
            checkBox.setText(bean.index+"");
        }else{
            checkBox.setText("");
        }
        maskView.setVisibility(bean.isChecked?View.VISIBLE:View.GONE);
        checkBox.setChecked(bean.isChecked);
    }
}
