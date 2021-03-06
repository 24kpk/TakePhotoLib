package com.uk.zc.takephotolib;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jph.takephoto.PictureActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int AVATAR_REQUEST_CODE = 101; // 头像

    private TextView fileSize;
    private TextView imageSize;
    private TextView thumbFileSize;
    private TextView thumbImageSize;
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileSize = (TextView) findViewById(R.id.file_size);
        imageSize = (TextView) findViewById(R.id.image_size);
        thumbFileSize = (TextView) findViewById(R.id.thumb_file_size);
        thumbImageSize = (TextView) findViewById(R.id.thumb_image_size);
        image = (ImageView) findViewById(R.id.image);

        Button fab = (Button) findViewById(R.id.fab);

        final String ROOT_PATH = String.format("%s%s", Environment.getExternalStorageDirectory().toString(), "/TEST_PHOTO/");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PictureActivity.class);
                //必须传入的输出临时文件夹路径
                intent.putExtra(PictureActivity.INTENT_KEY_PHOTO_TMP_PATH_DIR,ROOT_PATH+"/tmp/");
                //是否裁切图片 默认不裁切
//                intent.putExtra(PictureActivity.INTENT_KEY_CAN_CUT_PHOTO,true);

//                intent.putExtra(PictureActivity.INTENT_KEY_CUT_PHOTO_OUTPUTX,50);
//                intent.putExtra(PictureActivity.INTENT_KEY_CUT_PHOTO_OUTPUTY,60);
                //设置拍照或裁切图片的输出文件格式
                //intent.putExtra(PictureActivity.INTENT_KEY_PHOTO_TMP_EXT_NAME, ImgTypeUtils.IMG_TYPE_JPG);
                //设置压缩 默认不压缩
                //intent.putExtra(PictureActivity.INTENT_KEY_COMPRESS_PHOTO,true);

                //启用自定义压缩方式
                //intent.putExtra(PictureActivity.INTENT_KEY_ENABLE_CUSCOMPRESS,true);
                //限制最大宽高 单位PX
                //intent.putExtra(PictureActivity.INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL,600);
                //限制压缩后文件大小 建议不设置
                //intent.putExtra(PictureActivity.INTENT_KEY_COMPRESS_PHOTO_MAXSIZE,2*1024);
                startActivityForResult(intent, AVATAR_REQUEST_CODE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case AVATAR_REQUEST_CODE:// 头像的返回
                if (resultCode == RESULT_OK && data != null) {
                    String savePath = data.getStringExtra(PictureActivity.INTENT_KEY_RETURN_SAVE_PATH);
                    if (!TextUtils.isEmpty(savePath)) {
                        Log.e("CHONGZI","URL:"+savePath);
                        Glide.with(MainActivity.this).load(new File(savePath)).into(image);
                        File imgFile = new File(savePath);
                        thumbFileSize.setText(imgFile.length() / 1024 + "k");
                        thumbImageSize.setText(getImageSize(imgFile.getPath())[0] + " * " + getImageSize(imgFile.getPath())[1]);
                    }
                }
                break;
        }
    }
    public int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);

        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }
}
