package com.mxn.soul.slidingcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mxn.soul.slidingcard_core.ContainerView;
import com.mxn.soul.slidingcard_core.SlidingCard;

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
        try {
            initData() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() throws Exception {
        dataList = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.title) ;
        String[] imgs = getResources().getStringArray(R.array.imgs) ;
        for(int n = 0 ; n < titles.length;n++){
            PhotoContent photoContent = new PhotoContent(String.valueOf(n),titles[n],imgs[n]) ;
            dataList.add(photoContent) ;
        }
        if(dataList == null || dataList.size()<3)
            throw new Exception("list'size must be more than 3") ;

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
