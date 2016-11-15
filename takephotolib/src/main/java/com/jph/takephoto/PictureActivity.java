package com.jph.takephoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress.CompressConfig;
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
     * 默认
     * int aspectX=1
     * int aspectY=1
     * int cropOutputX = 300
     * int cropOutputY = 300
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
     * 压缩参数
     * 默认 maxSizeCompress = 2 * 1024 * 1024; //设置文件质量限制2M setMaxSize(maxSize) 传入参数单位B
     * 默认 maxPixelCompress = 540;//宽高不超过maxPixel px
     */
    public static final String INTENT_KEY_COMPRESS_PHOTO_MAXSIZE = "intent_key_compress_photo_maxsize";
    public static final String INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL = "intent_key_compress_photo_maxpixel";


    private TakePhoto takePhoto;
    private String photoTmpPath = null;
    private boolean cutPhoto = false;

    private int aspectX = 1;
    private int aspectY = 1;
    private int cropOutputX = 300;
    private int cropOutputY = 300;

    public String fileExtensionName = ImgTypeUtils.IMG_TYPE_IMGJ;
    private String tempPhotoPath;
    private int maxSizeCompress = 2 * 1024 * 1024;
    private int maxPixelCompress = 540;

    private TextView tvCamer, tvSelPic, tvCancel;
    private boolean isCompressImg;

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
        aspectX = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_ASPECTX, 1);
        aspectY = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_ASPECTY, 1);
        cropOutputX = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_OUTPUTX,300);
        cropOutputY = getIntent().getIntExtra(INTENT_KEY_CUT_PHOTO_OUTPUTY,300);


        isCompressImg = getIntent().getBooleanExtra(INTENT_KEY_COMPRESS_PHOTO,false);
        maxSizeCompress = getIntent().getIntExtra(INTENT_KEY_COMPRESS_PHOTO_MAXSIZE,2 * 1024 * 1024);
        maxPixelCompress = getIntent().getIntExtra(INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL,540);



        takePhoto = getTakePhoto();

        tvCamer = (TextView) findViewById(R.id.tv_camer);
        tvSelPic = (TextView) findViewById(R.id.tv_select_pic);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);

        if (isCompressImg){
            CompressConfig config = new CompressConfig.Builder().setMaxSize(maxSizeCompress).setMaxPixel(maxPixelCompress).create();
            takePhoto.onEnableCompress(config, true);
        }

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
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        if (result != null && result.getImage() != null) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_KEY_RETURN_SAVE_PATH, result.getImage().getPath());
            setResult(RESULT_OK, intent);
            this.finish();

        }
    }


    /**
     * 照相时的临时路径
     */
    private String makeTmpPhotoPath() {
        return photoTmpPath + System.currentTimeMillis() + fileExtensionName;

    }

    private CropOptions getCropOptions() {
        return new CropOptions.Builder().setAspectX(aspectX).setAspectY(aspectY).setOutputX(cropOutputX).setOutputY(cropOutputY).setWithOwnCrop(false).create();
    }
}
