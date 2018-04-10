package com.liang.lollipop.lcrop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.liang.lollipop.lcrop.bean.ImageBean;
import com.liang.lollipop.lcrop.util.LItemTouchHelper;

import java.util.ArrayList;

/**
 * Created by Lollipop on 2017/07/31.
 * 图片显示的Holder
 * @author lollipop
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

    private LItemTouchHelper helper;
    private RequestManager glide;
    private LayoutInflater inflater;
    private ArrayList<ImageBean> beanArrayList;
    private int itemHeight = 0;

    public ImageAdapter(LItemTouchHelper helper, RequestManager glide, LayoutInflater inflater, ArrayList<ImageBean> beanArrayList) {
        this.helper = helper;
        this.glide = glide;
        this.inflater = inflater;
        this.beanArrayList = beanArrayList;
    }

    public void setItemHeight(RecyclerView recyclerView, final int columns) {
        recyclerView.post(new GetViewSizeRunnable(recyclerView,columns){

            @Override
            public void getViewSize(float width, float height,int copies) {
                ImageAdapter.this.itemHeight = (int) (width/copies);
                notifyDataSetChanged();
            }
        });
    }

    public void setBeanArrayList(ArrayList<ImageBean> beanArrayList) {
        this.beanArrayList = beanArrayList;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageHolder holder = new ImageHolder(inflater.inflate(ImageHolder.LAYOUT_ID,parent,false));
        holder.setGlide(glide);
        holder.setLItemTouchHelper(helper);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        if(holder==null) {
            return;
        }
        holder.setImageHeight(itemHeight);
        holder.onBind(beanArrayList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return beanArrayList.get(position).type;
    }

    @Override
    public int getItemCount() {
        if(beanArrayList==null) {
            return 0;
        }
        return beanArrayList.size();
    }

    private abstract class GetViewSizeRunnable implements Runnable{

        private View view;
        private int copies;

        public GetViewSizeRunnable(View view, int copies) {
            this.view = view;
            this.copies = copies;
        }

        @Override
        public void run() {
            if(view==null) {
                return;
            }
            int height = view.getHeight();
            int width = view.getWidth();
            getViewSize(width,height,copies);
        }
        public abstract void getViewSize(float width,float height,int copies);
    }

}
