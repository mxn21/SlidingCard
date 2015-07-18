# SpecialAlbum
相册特效

![Showcase](screen.gif)

相册滑动翻页效果

### 1.0版本更新 2015.6.29

支持三张照片展示，手势滑动等。

需要改进的地方：

1、抗锯齿（做了基本优化，还需改进）

2、支持自定义多张照片

3、滑动动画流畅性需要优化

4、同时左滑和右滑会有bug

5、滑动过快导致最后一张加载没有显示

6、内存优化

### 1.1版本更新 2015.7.18

解决了锯齿问题。

在SlidingCard的dispatchDraw方法中加入以下代码:

```java
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint
        				.FILTER_BITMAP_FLAG);
        		canvas.setDrawFilter(pfd);
```

同时加入自定义控件SmoothImageView代替ImageView。在初始化时加入view级别硬件加速（3.0以后版本有效）。

```java
 if(android.os.Build.VERSION.SDK_INT>=11)
        {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
```

以上两个步骤缺一不可。