package com.mxn.soul.slidingcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mxn.soul.library.ContainerView;
import com.mxn.soul.library.SlidingCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ContainerView.ContainerInterface {

    private ContainerView contentView;
    private List<PhotoContent> dataList ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = (ContainerView) findViewById(R.id.contentview);
        initData() ;
    }

    private void initData(){
        dataList = new ArrayList<>();
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
        contentView.initCardView(MainActivity.this,R.layout.sliding_card_item,R.id
                .sliding_card_content_view);
    }

    @Override
    public void initCard(SlidingCard card, final int index) {
        ImageView mImageView = (ImageView) card.findViewById(R.id.user_imageview);
        TextView mTextView = (TextView) card.findViewById(R.id.user_text);
        if (dataList.get(index) != null) {
            mTextView.setText(dataList.get(index).getTitle());
            mImageView.setImageResource(getResourceByReflect(dataList.get(index).getUrl()));
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //the first card index must be 0
                    Toast.makeText(MainActivity.this,dataList.get(0).getTitle(),Toast
                            .LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void exChangeCard() {
        PhotoContent item = dataList.get(0);
        dataList.remove(0);
        dataList.add(item);
    }

    public int getResourceByReflect(String imageName) {
        Class drawable = R.drawable.class;
        Field field ;
        int r_id;
        try {
            field = drawable.getField(imageName);
            r_id = field.getInt(field.getName());
        } catch (Exception e) {
            r_id = R.drawable.img1;
            Log.e("ERROR", String.valueOf(e.getMessage()));
        }
        return r_id;
    }
}
