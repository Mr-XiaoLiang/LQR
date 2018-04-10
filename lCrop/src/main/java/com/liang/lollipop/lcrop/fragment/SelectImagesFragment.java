package com.liang.lollipop.lcrop.fragment;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.liang.lollipop.lcrop.R;
import com.liang.lollipop.lcrop.adapter.ImageAdapter;
import com.liang.lollipop.lcrop.adapter.ImageHolder;
import com.liang.lollipop.lcrop.bean.ImageBean;
import com.liang.lollipop.lcrop.util.LItemTouchCallback;
import com.liang.lollipop.lcrop.util.LItemTouchHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Lollipop
 * 选择图片的碎片
 */
public class SelectImagesFragment extends DialogFragment
        implements LItemTouchCallback.OnItemTouchCallbackListener {

    private static final int LOADER_ID = 456;

    private OnImagesSelectedListener onImagesSelectedListener;

    private ArrayList<ImageBean> imageBeen;
    private ArrayList<ImageBean> selectImageBeen;

    private int columns = 3;

    private ImageAdapter imageAdapter;

    private RecyclerView recyclerView;

    private int maxSelectedSize = 20;

    private boolean isShowCamera = true;

    private LoaderManager loaderManager;

    public SelectImagesFragment() {
        imageBeen = new ArrayList<>();
        selectImageBeen = new ArrayList<>();
    }

    public static SelectImagesFragment newInstance() {
        return newInstance(4);
    }

    public static SelectImagesFragment newInstance(int columns) {
        SelectImagesFragment fragment = new SelectImagesFragment();
        fragment.setColumns(columns);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_images, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.fragment_select_images_recyclerview);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        if(loaderManager!=null)
            loaderManager.initLoader(LOADER_ID, null, loaderCallback);
    }

    private void initView(){
        RequestManager glide = Glide.with(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),columns);//初始化列表layout管理器
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//设定为纵向
        recyclerView.setLayoutManager(layoutManager);//将管理器设置进列表
        recyclerView.setItemAnimator(new DefaultItemAnimator());//设置列表item动画
        LItemTouchHelper helper = LItemTouchHelper.newInstance(recyclerView,this);//设置控制帮助类
        imageAdapter = new ImageAdapter(helper, glide,LayoutInflater.from(getContext()),imageBeen);//初始化列表适配器
        recyclerView.setAdapter(imageAdapter);//为列表设置适配器
        imageAdapter.setItemHeight(recyclerView,columns);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnImagesSelectedListener)
            onImagesSelectedListener = (OnImagesSelectedListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onImagesSelectedListener = null;
        if(loaderManager!=null)
            loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onSwiped(int adapterPosition) {

    }

    @Override
    public boolean onMove(int srcPosition, int targetPosition) {
        return false;
    }

    @Override
    public void onItemViewClick(RecyclerView.ViewHolder holder, View v) {
        int index = holder.getAdapterPosition();
        ImageBean imageBean = imageBeen.get(index);
        switch (imageBean.type){
            case ImageBean.TYPE_CAMERA:
                if(onImagesSelectedListener!=null)
                    onImagesSelectedListener.onCallCamera();
                break;
            case ImageBean.TYPE_PHOTO:
                if(selectImageBeen==null)
                    selectImageBeen = new ArrayList<>();
                if(!imageBean.isChecked&&maxSelectedSize<=selectImageBeen.size())
                    return;
                imageBean.isChecked = !imageBean.isChecked;
                if(imageBean.isChecked)
                    selectImageBeen.add(imageBean);
                onSelectedImageChange();
                onImageSelected(imageBean);
                break;
        }
    }

    private void onSelectedImageChange(){
        //当选中图片数量大于0并且选中数量大于最大选择数量的时候，强行删除最后的图片
        //用于纠正图片选择矛盾
        while (selectImageBeen.size()>0&&maxSelectedSize<selectImageBeen.size()){
            selectImageBeen.remove(selectImageBeen.size()-1);
        }
        Iterator<ImageBean> iterator = selectImageBeen.iterator();
        int index = 1;
        while(iterator.hasNext()){
            ImageBean imageBean = iterator.next();
            if(imageBean.isChecked){
                int i = index++;
                if(i == imageBean.index)
                    continue;
                imageBean.index = i;
            }else{
                imageBean.index = 0;
                iterator.remove();
            }
            int position = imageBeen.indexOf(imageBean);
            if(position>=0)
                imageAdapter.notifyItemChanged(position);
        }
    }

    /**
     * 图片选择状态变化的回调监听
     */
    public interface OnImagesSelectedListener {
        /**
         * 当图片被选中时的回调监听
         * @param size 数量为当前选中图片的总数
         * @param index 当前选中图片的序号
         * @param position 选中图片在整个列表中的序号
         * @param url 选中图片的地址
         */
        void onImageSelected(int size,int index,int position,String url);

        /**
         * 当被要求使用相机时
         */
        void onCallCamera();
    }

    public void setColumns(int columns) {
        this.columns = columns;
        if(recyclerView==null)
            return;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager){
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanCount(columns);
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[3]+"=? OR "+IMAGE_PROJECTION[3]+"=? ",
                    new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
        }

        private boolean fileExist(String path){
            return !TextUtils.isEmpty(path) && new File(path).exists();
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    if(imageBeen==null){
                        imageBeen = new ArrayList<>();
                        imageAdapter.setBeanArrayList(imageBeen);
                    }
                    imageBeen.clear();

                    if(selectImageBeen!=null)
                        selectImageBeen.clear();

                    if(isShowCamera)
                        imageBeen.add(new ImageBean(ImageBean.TYPE_CAMERA));

                    data.moveToFirst();
                    do{
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if(!fileExist(path)){continue;}
                        ImageBean image;
                        if (!TextUtils.isEmpty(name)) {
                            image = new ImageBean(path, name, dateTime);
                            imageBeen.add(image);
                        }
                    }while(data.moveToNext());
                    imageAdapter.notifyDataSetChanged();
                    onImageSelected(null);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void onImageSelected(ImageBean imageBean){
        if(onImagesSelectedListener!=null)
            onImagesSelectedListener.onImageSelected(
                    selectImageBeen.size(),
                    selectImageBeen.indexOf(imageBean),
                    imageBeen.indexOf(imageBean),
                    imageBean==null?"":imageBean.url);
    }

    public void setMaxSelectedSize(int maxSelectedSize) {
        this.maxSelectedSize = maxSelectedSize;
    }

    public void setShowCamera(boolean showCamera) {
        if(isShowCamera==showCamera)
            return;
        isShowCamera = showCamera;
        if(imageBeen==null)
            return;
        if(imageBeen.size()<1&&showCamera)
            imageBeen.add(new ImageBean(ImageBean.TYPE_CAMERA));
        if(imageBeen.size()>0){
            if(showCamera){
                if(imageBeen.get(0).type!=ImageBean.TYPE_CAMERA)
                    imageBeen.add(new ImageBean(ImageBean.TYPE_CAMERA));
            }else{
                if(imageBeen.get(0).type==ImageBean.TYPE_CAMERA)
                    imageBeen.remove(0);
            }
        }
    }

    public void setLoaderManager(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
        if(loaderManager!=null&&loaderManager.getLoader(LOADER_ID)==null&&loaderCallback!=null)
            loaderManager.initLoader(LOADER_ID, null, loaderCallback);

    }

    public ArrayList<ImageBean> getSelectImageBeen() {
        return selectImageBeen;
    }
}
