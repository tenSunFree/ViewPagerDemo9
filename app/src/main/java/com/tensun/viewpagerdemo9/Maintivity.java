package com.tensun.viewpagerdemo9;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;

/**
 * Simple TouchGallery demo based on ViewPager and Photoview
 * 基於網路上分享的模板 加以修改
 *
 * 記得在module的build.gradle, 添加以下編譯:
 * compile project(':PhotoViewlibrary')
 *  compile 'com.github.bumptech.glide:glide:3.6.1'
 *
 *  Q: 如何修改系統通知欄的顏色?
 *  A: https://github.com/tenSunFree/Translucent-System-Bar
 *
 * Q: 如果想單獨搬運library至別的project, 卻發生錯誤, 該如何解決?
 * A: 1. 透過Windows介面, 直接以資料夾拖移的方式, 複製過去
 *      2. 在project的 settings.gradle, 新增該library全名, 例如 , ':library'
 *      3. rebuild project 即可
 *
 * Q: 顯示不出Log 相關訊息?
 * A: 可以試著檢查Monitor右上角, 不能選擇Firebase
 *
 * Q: 執行多點觸控 放大縮小, 操作自己所繪製的圖形時, 有機會出現pointerIndex out of range 異常
 * A: 問題出現原因: android.support.v4.view.ViewPager, 本身有Bug
 *      解決方式: 自定義ViewPager, 重寫onTouchEvent() 和onInterceptTouchEvent()
 *                         並在xml裡面使用這個自定義ViewPager
 *                          https://github.com/tenSunFree/pointerIndex-out-of-range/blob/master/%E8%87%AA%E5%AE%9A%E7%BE%A9ViewPager
 *
 * Q: 當發生報錯時, 如何簡化不相關的訊息, 精簡找到問題所在?
 * A: 在Android Monitor, 選擇專案名稱 --> Verbose --> Show only selected application, 這樣只會顯示關鍵問題
 */

public class Maintivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private ArrayList<String> urlList;                  // 儲存手機圖片的路徑-2
    private ArrayList<String> urlCheckList;             // 儲存勾選的圖片路徑
    private int currentPosition;                        // 儲存當前頁面的position

    private List<String> pathList;                      // 儲存手機圖片的路徑-1
    private String [] urls2;                            // 為了 ArrayList to 數組

    private CircleButton circleButton;

    private static final int CUSTOM_NUMBER = 1;     //

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleButton = (CircleButton) findViewById(R.id.circleButton);
        circleButton.bringToFront();                    // 讓circleButton 保持最上層顯示
        circleButton.setOnClickListener(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {    // 對於6.0版本的手機, 進行獲取 讀取權限
            ActivityCompat.requestPermissions(Maintivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CUSTOM_NUMBER);
        } else {                                                          // 未滿5.X版本 以下的手機, 只要在AndroidManifest 增加權限即可
            updateView();
        }
    }

    private void updateView() {

        getSDImagePath();                                                      // 取得手機裡面的所有圖片路徑

        urlList = new ArrayList<>();                                           // 實體化 儲存本機所有圖片的空間
        urlCheckList = new ArrayList<>();                                      // 實體化 儲存所有勾選圖片的空間
        for (int i = 0; i < pathList.size(); i++) {
            urlList.add(pathList.get(i));
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PictureSlidePagerAdapter(getSupportFragmentManager()));

        viewPager.setPageTransformer(true, new RotationPageTransformer());     // 實現滑動動畫效果
        viewPager.setOffscreenPageLimit(3);                                    // 設置左右預加載的數量為3

        /** 當切換頁面時, 想要做哪些事情 */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {  // 當翻頁的時候,

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;                                        // 目的是: onClick()需要

                if (!urlCheckList.contains(urlList.get(currentPosition))) {        // 如果urlCheckList裡面, 還沒有存入當前圖片
                    circleButton.setImageResource(R.drawable.b_05);
                } else {                                                           // 如果urlCheckList裡面, 已存入當前圖片
                    circleButton.setImageResource(R.drawable.b_04);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CUSTOM_NUMBER) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {    //假如允許了
                updateView();
            } else {
                Toast.makeText(this, "需要給我權限才能做事情唷！", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /** 對circleButton 進行監聽控制 */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.circleButton:

                if (!urlCheckList.contains(urlList.get(currentPosition)))  {       // 如果urlCheckList裡面, 還沒有存入當前圖片uri
                    urlCheckList.add(urlList.get(currentPosition));                // 把當前圖片uri 放入urlCheckList
                    circleButton.setImageResource(R.drawable.b_04);
                } else {
                    urlCheckList.remove(urlList.get(currentPosition));             // 把當前圖片uri 從urlCheckList移除
                    circleButton.setImageResource(R.drawable.b_05);
                }

                Toast.makeText(this, "目前已選取的圖片uri: " + urlCheckList, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /** 取得手機裡面的所有圖片路徑 */
    private List<String> getSDImagePath(){

        pathList = new ArrayList<String>();
        try {
            final String[] columns = {MediaStore.Images.Media.DATA};
            final String orderBy = MediaStore.Images.Media.DATE_ADDED;
            Cursor imageCursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy + " DESC");
            if (imageCursor != null) {
                while (imageCursor.moveToNext()) {
                    int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String picPath = imageCursor.getString(dataColumnIndex);
                    if(picPath != null){
                        pathList.add(picPath);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return pathList;
    }

    private  class PictureSlidePagerAdapter extends FragmentStatePagerAdapter {

        public PictureSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return PictureSlideFragment.newInstance(urlList.get(position));
        }

        @Override
        public int getCount() {
            return urlList.size();
        }
    }
}
