package com.mxn.soul.specialalbum;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private DiscoverContainerView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = (DiscoverContainerView) findViewById(R.id.contentview);
        initData() ;
    }

    private void initData(){
        List<PhotoContent> dataList = new ArrayList<>();
        PhotoContent photoContent1 =  new PhotoContent() ;
        photoContent1.setId("1") ;
        photoContent1.setTitle("当红小花旦越来越惊艳了");
        photoContent1.setUrl("img1");
        PhotoContent photoContent2 =  new PhotoContent() ;
        photoContent2.setId("2") ;
        photoContent2.setTitle("早秋长袖连衣裙刮起了唯美浪漫风") ;
        photoContent2.setUrl("img2") ;
        PhotoContent photoContent3 =  new PhotoContent() ;
        photoContent3.setId("3") ;
        photoContent3.setTitle("高品质裙装美照让你一次看过瘾") ;
        photoContent3.setUrl("img3") ;
        PhotoContent photoContent4 =  new PhotoContent() ;
        photoContent4.setId("4") ;
        photoContent4.setTitle("蕾丝防晒衫棒球服外套") ;
        photoContent4.setUrl("img4") ;
        PhotoContent photoContent5 =  new PhotoContent() ;
        photoContent5.setId("5") ;
        photoContent5.setTitle("纯色开衫薄款外套防嗮衫") ;
        photoContent5.setUrl("img5") ;
        dataList.add(photoContent1) ;
        dataList.add(photoContent2) ;
        dataList.add(photoContent3) ;
        dataList.add(photoContent4) ;
        dataList.add(photoContent5) ;
        contentView.initCardView(MainActivity.this, dataList);
    }

}
