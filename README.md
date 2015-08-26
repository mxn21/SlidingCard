# SlidingCard [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SlidingCard-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2355)

![Showcase](screen.gif)

Sliding cards with pretty gallery effects.


# Usage

*For a working implementation of this project see the `app/` folder.*

1.Download the latest JAR  via Maven:

Gradle:

    dependencies {
        compile 'com.mxn.soul:slidingcard-core:1.2.0'
    }

2.Add ContainerView to your activity's layout

activity_main.xml:

    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    xmlns:card="http://schemas.android.com/apk/res-auto"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:paddingLeft="@dimen/activity_horizontal_margin"
                    android:paddingRight="@dimen/activity_horizontal_margin"
                    android:paddingTop="@dimen/activity_vertical_margin"
                    android:paddingBottom="@dimen/activity_vertical_margin"
                    tools:context=".MainActivity">

        <com.mxn.soul.slidingcard_core.ContainerView
            android:id="@+id/contentview"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            card:card_item_height = "230dp"
            card:card_item_margin = "10dp"
            />

    </RelativeLayout>

*card_item_height* need to be bigger than the height of your photo or any view you create for
each item .

if you want the first view smaller than the second one to get a beautiful screen,you need to
set *card_item_margin*


3.Create your own layout for the card

sliding_card_item.xml

    <?xml version="1.0" encoding="utf-8"?>
    <RelativeLayout
         android:id="@+id/sliding_card_content_view"
         xmlns:android="http://schemas.android.com/apk/res/android"
         android:layout_width="fill_parent"
         android:layout_height="wrap_content"
         android:gravity="center"
         android:paddingBottom="20dp">

        <ImageView
            android:id="@+id/user_imageview"
            android:layout_width="fill_parent"
            android:layout_height="200dp"
            android:layout_margin="10dp"
            android:background="#ffffff"
            android:padding="5dp"
            android:scaleType="fitXY"
            android:contentDescription="@string/app_name"/>

        <TextView
            android:id="@+id/user_text"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_alignBottom="@id/user_imageview"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="15dp"
            android:layout_marginRight="15dp"
            android:background="#90575E6A"
            android:padding="5dp"
            android:textColor="#ffffff"
            android:textSize="14sp"/>
    </RelativeLayout>


4.implements ContainerView.ContainerInterface  and  override  initCard(),exChangeCard()


    public class MainActivity extends ActionBarActivity implements ContainerView.ContainerInterface {


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
            contentView.initCardView(MainActivity.this,R.layout.sliding_card_item,R.id.sliding_card_content_view);
        }

        @Override
        public void initCard(SlidingCard card, int index) {
            ImageView mImageView = (ImageView) card.findViewById(R.id.user_imageview);
            TextView mTextView = (TextView) card.findViewById(R.id.user_text);
            if (dataList.get(index) != null) {
                mTextView.setText(dataList.get(index).getTitle());
                mImageView.setImageResource(getResourceByReflect(dataList.get(index).getUrl()));
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //the first card index must be 0
                        Toast.makeText(MainActivity.this, dataList.get(0).getTitle(), Toast
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

    // you can ues UniversalImageLoader,Fresco etc to display image ,and replace the method
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

contentView.initCardView() need to set you own layout name ,and the id of the root .

it's easy to create a list to store the data , but make sure the size of the list is bigger than 3 .


# TODO

Optimized memory


### V1.6

make it easier to use.

### V1.5

解决了滑动过快导致最后一张加载没有显示的bug。
加入了一个全局变量 public static boolean sScrolling = false ;来记录全局滑动状态，
在滑动过程中去掉其他卡片的监听，滑动之后再加入监听。

### V1.4

优化滑动动画流畅性，图片可以根据手势旋转。

### V1.3

支持自定义多张照片.可支持任意张照片，但是一次只会显示最前面的三张，随着滑动翻页，依次向后加载，加载完毕后循环从第一张加载。


### V1.2

修复了左滑和右滑图片出现BUG的问题.

### V1.1

解决了锯齿问题。

在SlidingCard的dispatchDraw方法中加入以下代码:

```java
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint
        				.FILTER_BITMAP_FLAG);
        		canvas.setDrawFilter(pfd);
```

在初始化时关闭view级别硬件加速（3.0以后版本有效）。
由于硬件加速并不支持所有的2D图形绘制操作，因此对于自定义的View和绘制调用来说，会造成影响。
对于这个问题，通常是对那些不可见的元素进行了异常或错误的像素渲染。为了避免这种问题，需要关闭硬件加速。

```java
 if(android.os.Build.VERSION.SDK_INT>=11)
        {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
```

以上两个步骤缺一不可。


### V1.0

支持三张照片展示，手势滑动等。

需要改进的地方：

1、抗锯齿（做了基本优化，还需改进）
2、支持自定义多张照片
3、滑动动画流畅性需要优化
4、同时左滑和右滑会有bug
5、滑动过快导致最后一张加载没有显示
6、内存优化

License
=======

    Copyright 2015 soul.mxn

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [1]: https://github.com/JuneLeGency/TinderCardDemo
 [2]: https://github.com/fangyu286/Tinder-For-Android
