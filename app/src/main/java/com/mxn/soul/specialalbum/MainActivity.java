package com.mxn.soul.specialalbum;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements DiscoverContainerView.ContainerInterface {

    private DiscoverContainerView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = (DiscoverContainerView) findViewById(R.id.contentview);

        initData() ;

    }


    @Override
    public void onFeelOperat(int count) {
        PhotoContent photoContent = new PhotoContent();
        photoContent.setId(String.valueOf(count%3 +1)) ;
        contentView.addNew(photoContent);

    }


    private void initData(){
        List<PhotoContent> dataList = new ArrayList<>();
        PhotoContent photoContent1 =  new PhotoContent() ;
        photoContent1.setId("1") ;
        PhotoContent photoContent2 =  new PhotoContent() ;
        photoContent2.setId("2") ;
        PhotoContent photoContent3 =  new PhotoContent() ;
        photoContent3.setId("3") ;
        dataList.add(photoContent1) ;
        dataList.add(photoContent2) ;
        dataList.add(photoContent3) ;
        contentView.initCardView(MainActivity.this,dataList);
        contentView.setContainerInterface(this) ;


    }
}
