package com.jph.takephoto.compress_cus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * author: C_CHEUNG
 * created on: 2016/11/23
 * description: 压缩图片
 */
public class CompressImpl {
    private String TAG = CompressImpl.class.getSimpleName();
    CompressParams mCompressParams;
    private CompressListener mCompressListener;
    public CompressImpl(CompressParams compressParams){
        this.mCompressParams = compressParams;
    }

    public CompressListener getCompressListener() {
        return mCompressListener;
    }

    public void setCompressListener(CompressListener compressListener) {
        mCompressListener = compressListener;
    }

    public void startCompress(){
        if (mCompressListener != null) mCompressListener.onStart();
        Observable.just(new File(mCompressParams.getLargeImagePath()))
                .map(new Func1<File, File>() {
                    @Override
                    public File call(File file) {
                        if (mCompressParams.isEnableCusCompress()){
                            //自定义压缩
                            return cusCompress(file,mCompressParams.getThumbFilePath(),mCompressParams.getMaxPixel(),mCompressParams.getMaxSize());
                        }else {
                            //默认压缩
                            return defaultCompress(file,mCompressParams.getThumbFilePath());
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (mCompressListener != null) mCompressListener.onError(throwable);
                    }
                })
                .onErrorResumeNext(Observable.<File>empty())
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return file != null;
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        if (mCompressListener != null) mCompressListener.onSuccess(file);
                    }
                });
    }


    /**
     * 指定参数压缩图片
     * create the thumbnail with the true rotate angle
     *
     * @param largeImagePath the big image path
     * @param thumbFilePath  the thumbnail path
     * @param width          width of thumbnail
     * @param height         height of thumbnail
     * @param angle          rotation angle of thumbnail
     * @param size           the file size of image
     */
    private File compress(String largeImagePath, String thumbFilePath, int width, int height, int angle, long size) {
        Bitmap thbBitmap = compress(largeImagePath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);

        return saveImage(thumbFilePath, thbBitmap, size);
    }

    /**
     * 自定义压缩
     * @param file
     * @param thumbFilePath 若压缩 size >0
     * @param maxPixel 若压缩 maxPixel >1
     * @param size 默认 KB
     * @return
     */
    private File cusCompress(@NonNull File file , String thumbFilePath , int maxPixel,double size) {
        String filePath = file.getAbsolutePath();
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        double scale = ((double) width / height);

        int angle = getImageSpinAngle(filePath);
        int thumbW ,thumbH ;

        thumbW = width % 2 == 1 ? width + 1 : width;
        thumbH = height % 2 == 1 ? height + 1 : height;

        if (size > 0){
            if (file.length() < size) return file;
            if (maxPixel < 1){
                return compress(filePath, thumbFilePath, width, height, angle, (long) size);
            }else {
                //用高或者宽其中较大的一个数据进行计算
                if (width >= height && width > maxPixel){
                    thumbW = maxPixel;
                    if (height>maxPixel){
                        thumbH = (height * maxPixel / width);
                    }else {
                        thumbH = height;
                    }
                }
                if (height > width && height > maxPixel){
                    thumbH = maxPixel;
                    if (width > maxPixel){
                        thumbW = (width * maxPixel / height);
                    }else {
                        thumbW = width;
                    }
                }
                return compress(filePath, thumbFilePath, thumbW, thumbH, angle, (long) size);
            }
        }else {
            if (maxPixel < 1){
                defaultCompress(file,thumbFilePath);
            }else {

                //用高或者宽其中较大的一个数据进行计算
                if (width >= height && width > maxPixel){
                    thumbW = maxPixel;
                    if (height>maxPixel){
                        thumbH = (height * maxPixel / width);
                    }else {
                        thumbH = height;
                    }
                }
                if (height > width && height > maxPixel){
                    thumbH = maxPixel;
                    if (width > maxPixel){
                        thumbW = (width * maxPixel / height);
                    }else {
                        thumbW = width;
                    }
                }


                //默认压缩计算文件大小
                if (scale <= 1 && scale > 0.5625) {
                    if (thumbH < 1664) {
                        if (file.length() / 1024 < 150) return file;
                        size = size < 60 ? 60 : size;
                    } else if (thumbH >= 1664 && thumbH < 4990) {
                        size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                        size = size < 60 ? 60 : size;
                    } else if (thumbH >= 4990 && thumbH < 10240) {
                        size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                        size = size < 100 ? 100 : size;
                    } else {
                        size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                        size = size < 100 ? 100 : size;
                    }
                } else if (scale <= 0.5625 && scale > 0.5) {
                    if (thumbH < 1280 && file.length() / 1024 < 200) return file;
                    size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
                    size = size < 100 ? 100 : size;
                } else {
                    size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
                    size = size < 100 ? 100 : size;
                }
                return compress(filePath, thumbFilePath, thumbW, thumbH, angle, (long) size);

//                if (width >= height && width > maxSize) {//缩放比,用高或者宽其中较大的一个数据进行计算
//                    be = (int) (newOpts.outWidth / maxSize);
//                    be++;
//                } else if (width < height && height > maxSize) {
//                    be = (int) (newOpts.outHeight / maxSize);
//                    be++;
//                }
            }
        }

        return compress(filePath, thumbFilePath, thumbW, thumbH, angle, (long) size);
        /*
        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;



        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (file.length() / 1024 < 150) return file;

                size = (width * height) / Math.pow(1664, 2) * 150;
                size = size < 60 ? 60 : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                size = size < 60 ? 60 : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280 && file.length() / 1024 < 200) return file;

            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
            size = size < 100 ? 100 : size;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            size = size < 100 ? 100 : size;
        }
        return compress(filePath, thumbFilePath, thumbW, thumbH, angle, (long) size);
        */
    }



    private File defaultCompress(@NonNull File file , String thumbFilePath) {
        double size;
        String filePath = file.getAbsolutePath();

        int angle = getImageSpinAngle(filePath);
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;

        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;

        double scale = ((double) width / height);

        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (file.length() / 1024 < 150) return file;

                size = (width * height) / Math.pow(1664, 2) * 150;
                size = size < 60 ? 60 : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                size = size < 60 ? 60 : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280 && file.length() / 1024 < 200) return file;

            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
            size = size < 100 ? 100 : size;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            size = size < 100 ? 100 : size;
        }

        return compress(filePath, thumbFilePath, thumbW, thumbH, angle, (long) size);
    }

    /**
     * obtain the thumbnail that specify the size
     *
     * @param imagePath the target image path
     * @param width     the width of thumbnail
     * @param height    the height of thumbnail
     * @return {@link Bitmap}
     */
    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;

            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * 旋转图片
     * rotate the image with specified angle
     *
     * @param angle  the angle will be rotating 旋转的角度
     * @param bitmap target image               目标图片
     */
    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 保存图片到指定路径
     * Save image with specified size
     *
     * @param filePath the image file save path 储存路径
     * @param bitmap   the image what be save   目标图片
     * @param size     the file size of image   期望大小
     */
    private File saveImage(String filePath, Bitmap bitmap, long size) {
        checkNotNull(bitmap, TAG + "bitmap cannot be null");

        File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));

        if (!result.exists() && !result.mkdirs()) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);

        while (stream.toByteArray().length / 1024 > size && options > 6) {
            stream.reset();
            options -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        }
        bitmap.recycle();

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(filePath);
    }

    static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * obtain the image rotation angle
     *
     * @param path path of target image
     */
    private int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * obtain the image's width and height
     *
     * @param imagePath the path of image
     */
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
