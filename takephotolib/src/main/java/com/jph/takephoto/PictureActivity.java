package com.jph.takephoto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress_cus.CompressImpl;
import com.jph.takephoto.compress_cus.CompressListener;
import com.jph.takephoto.compress_cus.CompressParams;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.uitl.ImgTypeUtils;

import java.io.File;



/**
 * author: C_CHEUNG
 * created on: 2016/11/15
 * description: 处理拍照、选择本地图片的界面
 * 注意：必须指定输出的文件夹路径 文件名默认为  photoTmpPath + System.currentTimeMillis() + ImgTypeUtils.IMG_TYPE_JPG
 */
public class PictureActivity extends TakePhotoActivity {
    private String TAG = PictureActivity.class.getSimpleName();
    /**
     * 类型String
     * 必须传入文件路径 @LIKE String TEMP_FILE_PATH = String.format("%stemp/", ROOT_PATH)
     */
    public static final String INTENT_KEY_PHOTO_TMP_PATH_DIR = "intent_key_photo_tmp_path_dir";


    /**
     * 返回的文件路径的INTENT_KEY
     * 返回的为拍照后 （裁切后）图片路径*"PATH"路径*  ——说明：该图片为指定的路径(指定的文件类型)
     * 返回的为图库选择的 （裁切后）图片路径*"PATH"路径*  ——说明：该图片为指定的路径(指定的文件类型)
     */
    public static final String INTENT_KEY_RETURN_SAVE_PATH = "intent_key_return_save_path";


    /**
     * 若裁切图片则传入true
     * 默认不裁切图片
     */
    public static final String INTENT_KEY_CAN_CUT_PHOTO = "intent_key_can_cut_photo";

    /**
     * 裁切图片时 传入的裁剪参数
     * 默认 裁剪图片宽高(任意)比例裁切
     * int aspectX=0
     * int aspectY=0
     * int cropOutputX = -1
     * int cropOutputY = -1
     * 若 cropOutputX > 0 && cropOutputY > 0 按图片宽高(cropOutputX、cropOutputY)裁剪
     */
    public static final String INTENT_KEY_CUT_PHOTO_ASPECTX = "intent_key_cut_photo_aspectX";
    public static final String INTENT_KEY_CUT_PHOTO_ASPECTY = "intent_key_cut_photo_aspectY";
    public static final String INTENT_KEY_CUT_PHOTO_OUTPUTX = "intent_key_cut_photo_outputx";
    public static final String INTENT_KEY_CUT_PHOTO_OUTPUTY = "intent_key_cut_photo_outputy";


    /**
     * 输出的文件类型使用以下指定文件类型
     * 若需要指定文件类型，请传入文件类型参数 默认文件类型为 ImgTypeUtils.IMG_TYPE_IMGJ
     * 文件类型限制为以下几种文件类型
     * ImgTypeUtils.IMG_TYPE_JPG
     * ImgTypeUtils.IMG_TYPE_GIF
     * ImgTypeUtils.IMG_TYPE_PNG
     * ImgTypeUtils.IMG_TYPE_BMP
     * ImgTypeUtils.IMG_TYPE_JPEG
     * ImgTypeUtils.IMG_TYPE_WEBP
     * ImgTypeUtils.IMG_TYPE_IMGJ
     * ImgTypeUtils.IMG_TYPE_IMGP
     * ImgTypeUtils.IMG_TYPE_IMGG
     */
    public static final String INTENT_KEY_PHOTO_TMP_EXT_NAME = "intent_key_photo_tmp_ext_name";




    /**
     * 是否压缩
     * 默认false
     */
    public static final String INTENT_KEY_COMPRESS_PHOTO = "intent_key_compress_photo";

    /**
     * 是否需要自定义压缩
     * 自定义压缩才可传入压缩参数
     */
    public static final String INTENT_KEY_ENABLE_CUSCOMPRESS = "intent_key_enable_cuscompress";

    /**
     * 压缩参数
     * 默认 maxSizeCompress = -1; //设置文件质量限制2M setMaxSize(maxSize) 传入参数单位KB
     * 默认 maxPixelCompress = -1 ;//宽高不超过maxPixel px
     */
    public static final String INTENT_KEY_COMPRESS_PHOTO_MAXSIZE = "intent_key_compress_photo_maxsize";
    public static final String INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL = "intent_key_compress_photo_maxpixel";


    private TakePhoto takePhoto;
    private String photoTmpPath = null;
    private boolean cutPhoto = false;

    private int aspectX = 1;
    private int aspectY = 1;
    private int cropOutputX = -1;
    private int cropOutputY = -1;

    public String fileExtensionName = ImgTypeUtils.IMG_TYPE_IMGJ;
    private String tempPhotoPath;
    private int maxSizeCompress = -1;
    private int maxPixelCompress = -1;

    private TextView tvCamer, tvSelPic, tvCancel;
    private boolean isCompressImg;
    private boolean enableCuscompress = false;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_picture_layout);

        photoTmpPath = getIntent().getStringExtra(INTENT_KEY_PHOTO_TMP_PATH_DIR);

        String tmpExtName = getIntent().getStringExtra(INTENT_KEY_PHOTO_TMP_EXT_NAME);
        if (!TextUtils.isEmpty(tmpExtName)){
            fileExtensionName = tmpExtName;
        }

        cutPhoto = getIntent().getBooleanExtra(INTENT_KEY_CAN_CUT_PHOTO, false);
        aspectX = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_ASPECTX, 0);
        aspectY = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_ASPECTY, 0);
        cropOutputX = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_OUTPUTX,-1);
        cropOutputY = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_OUTPUTY,-1);


        isCompressImg = getIntent().getBooleanExtra(INTENT_KEY_COMPRESS_PHOTO,false);
        enableCuscompress = getIntent().getBooleanExtra(INTENT_KEY_ENABLE_CUSCOMPRESS,false);

        maxSizeCompress = getIntent().getIntExtra(INTENT_KEY_COMPRESS_PHOTO_MAXSIZE,-1);
        maxPixelCompress = getIntent().getIntExtra(INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL,-1);



        takePhoto = getTakePhoto();

        tvCamer = (TextView) findViewById(R.id.tv_camer);
        tvSelPic = (TextView) findViewById(R.id.tv_select_pic);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);

        tvCamer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamara();
            }
        });
        tvSelPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAlbum();
            }


        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void startAlbum() {
        tempPhotoPath = makeTmpPhotoPath();
        File file = new File(tempPhotoPath);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        if (!cutPhoto) {
            takePhoto.onPickFromGallery();
        } else {
            takePhoto.onPickFromGalleryWithCrop(Uri.fromFile(new File(tempPhotoPath)), getCropOptions());
        }
    }


    /**
     * 启动照相机
     */
    public void startCamara() {

        tempPhotoPath = makeTmpPhotoPath();
        File file = new File(tempPhotoPath);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

        if (!cutPhoto) {
            takePhoto.onPickFromCapture(Uri.fromFile(new File(tempPhotoPath)));
        } else {
            takePhoto.onPickFromCaptureWithCrop(Uri.fromFile(new File(tempPhotoPath)), getCropOptions());
        }
    }


    @Override
    public void takeSuccess(final TResult result) {
        super.takeSuccess(result);
        if (result != null && result.getImage() != null) {
            if (isCompressImg){
                String largeImagePath = result.getImage().getPath();
                String thumFilePath = photoTmpPath+System.currentTimeMillis()+fileExtensionName;
                CompressParams params = new CompressParams();
                params.setLargeImagePath(largeImagePath);
                if (enableCuscompress){
                    params.setEnableCusCompress(true);//开启自定义压缩模式
                    if (maxSizeCompress > 0) //设置最大宽高
                    params.setMaxPixel(maxSizeCompress);
                    if (maxPixelCompress > 0) //设置最大文件大小限制
                    params.setMaxPixel(maxPixelCompress);
                }
                if (!fileExtensionName.equals(largeImagePath.substring(largeImagePath.lastIndexOf(".")))){
                    params.setThumbFilePath(thumFilePath);
                }else {
                    params.setThumbFilePath(largeImagePath);
                }

                CompressImpl compress = new CompressImpl(params);
                compress.setCompressListener(new CompressListener() {
                    @Override
                    public void onStart() {
                        String title = getResources().getString(R.string.tip_tips);
                        mProgressDialog = new ProgressDialog(PictureActivity.this);
                        mProgressDialog.setTitle(title);
                        mProgressDialog.setMessage(getResources().getString(R.string.tip_compress));
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                    }

                    @Override
                    public void onSuccess(File file) {
                        if (mProgressDialog!=null && mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        Intent intent = new Intent();
                        intent.putExtra(INTENT_KEY_RETURN_SAVE_PATH, file.getAbsolutePath());
                        setResult(RESULT_OK, intent);
                        PictureActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mProgressDialog!=null && mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        Toast.makeText(PictureActivity.this,"压缩失败",Toast.LENGTH_SHORT).show();
                        Log.e(TAG,e.getMessage());
                    }
                });
                compress.startCompress();
            }else {
                Intent intent = new Intent();
                intent.putExtra(INTENT_KEY_RETURN_SAVE_PATH, result.getImage().getPath());
                setResult(RESULT_OK, intent);
                this.finish();
            }
        }
    }


    /**
     * 照相时的临时路径
     */
    private String makeTmpPhotoPath() {
        return photoTmpPath + System.currentTimeMillis() + fileExtensionName;

    }

    private CropOptions getCropOptions() {
        CropOptions.Builder builder = null;

        if (cropOutputX >0 && cropOutputY >0){
            builder = new CropOptions.Builder().setOutputX(cropOutputX).setOutputY(cropOutputY).setWithOwnCrop(false);
        }else if (aspectX > 0 && aspectY > 0){
            builder = new CropOptions.Builder().setAspectX(aspectX).setAspectY(aspectY).setWithOwnCrop(false);
        }else {
            builder = new CropOptions.Builder().setWithOwnCrop(false);
        }
        return builder.create();
    }

}
