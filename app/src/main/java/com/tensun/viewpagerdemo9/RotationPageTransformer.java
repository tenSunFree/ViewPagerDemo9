package com.tensun.viewpagerdemo9;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 主要用途:
 */

public class RotationPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE= 1.00f;         // 數值越大, page間距越小, 低於0.80f 基本上是看不到其它page

    @Override
    public void transformPage(View page, float position) {
        float scaleFactor = Math.max(MIN_SCALE,1 - Math.abs(position));
        float rotate = 15 * Math.abs(position);            // 數字越大, 切換page時 旋轉角度越明顯

        if (position <= -1){                               // position小于等于1的时候，代表page已经位于中心item的最左边，此时设置为最小的缩放率以及最大的旋转度数
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
            page.setRotationY(rotate);
        }
        else if (position < 0){                            // position从0变化到-1，page逐渐向左滑动
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setRotationY(rotate);
        }
        else if (position >=0 && position < 1){            // position从0变化到1，page逐渐向右滑动
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setRotationY(-rotate);
        }
        else if (position >= 1){                           // position大于等于1的时候，代表page已经位于中心item的最右边
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setRotationY(-rotate);
        }
    }
}