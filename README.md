# SlidingCard
相册特效

![Showcase](screen.gif)

相册滑动翻页效果

### 1.5版本更新 2015.8.12

解决了滑动过快导致最后一张加载没有显示的bug。
加入了一个全局变量 public static boolean sScrolling = false ;来记录全局滑动状态，
在滑动过程中去掉其他卡片的监听，滑动之后再加入监听。

### 1.4版本更新 2015.8.9

优化滑动动画流畅性，图片可以根据手势旋转。

### 1.3版本更新 2015.8.5

支持自定义多张照片.可支持任意张照片，但是一次只会显示最前面的三张，随着滑动翻页，依次向后加载，加载完毕后循环从第一张加载。


### 1.2版本更新 2015.8.5

修复了左滑和右滑图片出现BUG的问题.


### 1.1版本更新 2015.7.18

解决了锯齿问题。

在SlidingCard的dispatchDraw方法中加入以下代码:

```java
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint
        				.FILTER_BITMAP_FLAG);
        		canvas.setDrawFilter(pfd);
```

同时加入自定义控件SmoothImageView代替ImageView。在初始化时关闭view级别硬件加速（3.0以后版本有效）。
由于硬件加速并不支持所有的2D图形绘制操作，因此对于自定义的View和绘制调用来说，会造成影响。
对于这个问题，通常是对那些不可见的元素进行了异常或错误的像素渲染。为了避免这种问题，需要关闭硬件加速。

```java
 if(android.os.Build.VERSION.SDK_INT>=11)
        {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
```

以上两个步骤缺一不可。


### 1.0版本更新 2015.6.29

支持三张照片展示，手势滑动等。

需要改进的地方：

1、抗锯齿（做了基本优化，还需改进）

2、支持自定义多张照片

3、滑动动画流畅性需要优化

4、同时左滑和右滑会有bug

5、滑动过快导致最后一张加载没有显示

6、内存优化

License
--------

The MIT License (MIT)

Copyright (c) 2015 soul.mxn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
