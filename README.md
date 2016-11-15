# TakephotoLib #
TakephotoLib 定制修改版本


----------

### 添加TakephotoLib gradle依赖 ###

TakephotoLib已经更新到[jitpack](https://jitpack.io/)上，使用AndroidStudio导入即可.

**Step 1** 在项目根目录 <B>build.gradle</B> 中添加
>     allprojects {
>         repositories {
>     	    ...
>     	    maven { url "https://jitpack.io" }
>             }
>         }

**Step 2** 在App项目引用 <B>build.gradle</B> 中添加
>     dependencies {
>             compile 'com.github.24kpk:TakePhotoLib:1.0.1'
>     }



### 使用DEMO ###
		package com.uk.zc.takephotolib;
		
		import android.content.Intent;
		import android.os.Bundle;
		import android.os.Environment;
		import android.support.v7.app.AppCompatActivity;
		import android.text.TextUtils;
		import android.util.Log;
		import android.view.View;
		import android.widget.Button;
		
		import com.jph.takephoto.PictureActivity;
		
		public class MainActivity extends AppCompatActivity {
		    private static final int AVATAR_REQUEST_CODE = 101; // 头像
		
		    @Override
		    protected void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.activity_main);
		        final String ROOT_PATH = String.format("%s%s", Environment.getExternalStorageDirectory().toString(), "/TEST_PHOTO/");
		        Button btn = (Button) findViewById(R.id.button);
		        btn.setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		                Intent intent = new Intent(MainActivity.this, PictureActivity.class);
		                //必须传入的输出临时文件夹路径
		                intent.putExtra(PictureActivity.INTENT_KEY_PHOTO_TMP_PATH_DIR,ROOT_PATH+"/tmp/");
		                //是否裁切图片默认不裁切  传入true 则裁切 裁切参数详见PictureActivity INTENT_KEY说明
		//                intent.putExtra(PictureActivity.INTENT_KEY_CAN_CUT_PHOTO,true);
		                //设置拍照或裁切图片的输出文件格式
		//                intent.putExtra(PictureActivity.INTENT_KEY_PHOTO_TMP_EXT_NAME, ImgTypeUtils.IMG_TYPE_JPG);
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
		                        Log.e("CHONGZI",savePath);
		                    }
		                }
		                break;
		        }
		    }
		}
### 其他 ###
更多TAKE Photo 参数 [请点击这里](https://github.com/crazycodeboy/TakePhoto)
