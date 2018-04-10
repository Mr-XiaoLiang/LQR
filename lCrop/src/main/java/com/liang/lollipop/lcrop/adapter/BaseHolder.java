package com.liang.lollipop.lcrop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.RequestManager;
import com.liang.lollipop.lcrop.bean.BaseBean;
import com.liang.lollipop.lcrop.util.LItemTouchHelper;

/**
 * Created by Lollipop on 2016/12/5.
 * 基础的ViewHolder
 */
public class BaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected int id;
    protected LItemTouchHelper touch;
    protected RequestManager glide;
    protected Context context;
    protected boolean canSwipe = false;
    protected boolean canMove = false;

    public BaseHolder(View itemView) {
        super(itemView);
        setContext(itemView.getContext());
    }

    public void onBind(BaseBean bean){
    }

    public void init(RequestManager glide,LItemTouchHelper touch){
        setGlide(glide);
        setLItemTouchHelper(touch);
    }

    public void setGlide(RequestManager glide) {
        this.glide = glide;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    public void setLItemTouchHelper(LItemTouchHelper touch) {
        this.touch = touch;
    }

    public boolean canSwipe(){
        return canSwipe;
    }

    public boolean canMove() {
        return canMove;
    }

    @Override
    public void onClick(View v) {
        if(touch!=null)
            touch.onItemViewClick(this,v);
    }
}
