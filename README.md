# SlidingCard 

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SlidingCard-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2355)
[![travis-ic](https://travis-ci.org/mxn21/SlidingCard.svg?branch=master)](https://travis-ci.org/mxn21/SlidingCard)

![Showcase](http://baobaoloveyou.com/slidingcard.gif)

Sliding cards with pretty gallery effects.


# Download

Include the following dependency in your build.gradle file.

Gradle:

```Gradle
    repositories {
        jcenter()
    }

    dependencies {
        compile 'com.mxn.soul:slidingcard-core:1.3.0'
    }
```


# Usage

*For a working implementation of this project see the `app/` folder.*


1.Add the com.mxn.soul.slidingcard_core.ContainerView to your layout XML file.

activity_main.xml:

```xml
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

```

*card_item_height* need to be bigger than the height of your photo or any view you create for
each item .

if you want the first view smaller than the second one to get a beautiful screen,you need to
set *card_item_margin*


2.Create your own layout for the card

sliding_card_item.xml

```xml
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
```

3.implements ContainerView.ContainerInterface  and  override  initCard(),exChangeCard()


```java

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
        String[] titles = getResources().getStringArray(R.array.title) ;
        String[] imgs = getResources().getStringArray(R.array.imgs) ;
        for(int n = 0 ; n < titles.length;n++){
            PhotoContent photoContent = new PhotoContent(String.valueOf(n),titles[n],imgs[n]) ;
            dataList.add(photoContent) ;
             }
           contentView.initCardView(MainActivity.this,R.layout.sliding_card_item,R.id
                .sliding_card_content_view);
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
```

contentView.initCardView() need to set you own layout's name ,and the root's id .
example: initCardView(ContainerInterface mContainerInterface,int layoutId,int rootId).

it's easy to set new ArrayList() to store the data , but make sure the list's size is more
than 3 .

if you put it in scrollable viewgroup like listview ,viewpager etc..,drag will not work normally,
I provide a method to avoid scroll conflict:

```java
    public void setScrollableGroups(ViewGroup...args)
```

do this after contentView's Initialization:

```java
    contentView = (ContainerView) view.findViewById(R.id.contentview);
    contentView.setScrollableGroups(viewPager, listview) ;
    contentView.initCardView(MyFragment.this, R.layout.sliding_card_item, R.id
                .sliding_card_content_view);
```

# TODO

Optimized memory

### V1.7

support SlidingCard with Fragment. Add method to solve scroll conflict.

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
