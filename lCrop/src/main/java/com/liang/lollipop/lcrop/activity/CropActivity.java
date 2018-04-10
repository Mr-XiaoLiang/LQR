package com.liang.lollipop.lcrop.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.liang.lollipop.lcrop.R;
import com.oginotihiro.cropview.CropUtil;
import com.oginotihiro.cropview.CropView;

/**
 * @author Lollipop on 2017-08-02
 */
public class CropActivity extends BaseActivity {

    public static final String ARG_IMAGE_URL = "ARG_IMAGE_URL";
    public static final String ARG_OUTPUT_URL = "ARG_OUTPUT_URL";

    public static final String ARG_TITLE = "ARG_TITLE";

    public static final String ARG_ASPECT_X = "ARG_ASPECT_X";
    public static final String ARG_ASPECT_Y = "ARG_ASPECT_Y";

    public static final String ARG_OUT_WIDTH = "ARG_OUT_WIDTH";
    public static final String ARG_OUT_HEIGHT = "ARG_OUT_HEIGHT";

    private CropView cropImageView;

    private View progressGroup;
    private ProgressBar progressBar;

    private Uri outputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_crop_toolbar);
        setSupportActionBar(toolbar);
        initView();
        initData();
    }

    private void initView(){
        cropImageView = (CropView) findViewById(R.id.activity_crop_crop);
        progressGroup = findViewById(R.id.progressGroup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void initData(){
        Intent intent = getIntent();
        String title = intent.getStringExtra(ARG_TITLE);
        if(!TextUtils.isEmpty(title)&&getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(title);
        }
        String path = intent.getStringExtra(ARG_IMAGE_URL);

        int aspectX = intent.getIntExtra(ARG_ASPECT_X,1);
        int aspectY = intent.getIntExtra(ARG_ASPECT_Y,1);

        int outWidth = intent.getIntExtra(ARG_OUT_WIDTH,500);
        int outHeight = intent.getIntExtra(ARG_OUT_HEIGHT,500);

        outputUri = Uri.parse(intent.getStringExtra(ARG_OUTPUT_URL));

        cropImageView.of(Uri.parse(path))
                .withAspect(aspectX,aspectY)
                .withOutputSize(outWidth,outHeight)
                .initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.menu.menu_crop){

            progressGroup.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    CropUtil.saveOutput(CropActivity.this,outputUri,cropImageView.getOutput(),100);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setIndeterminate(false);
                            progressGroup.setVisibility(View.INVISIBLE);

                            setResult(RESULT_OK);

                            onBackPressed();
                        }
                    });

                }
            }).start();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
