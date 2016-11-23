package com.jph.takephoto.compress_cus;

/**
 * author: C_CHEUNG
 * created on: 2016/11/23
 * description: 第三方 压缩配置类
 */
public class CompressParams {
    private String TAG = CompressParams.class.getSimpleName();
    /**
     * 传入的图片地址
     */
    private String largeImagePath;
    /**
     * 输出的图片地址
     */
    private String thumbFilePath;

    /**
     * 是否启动自定义压缩
     */
    private boolean enableCusCompress = false;
    /**
     * 自定义压缩 参数
     * 长或宽不超过的最大像素,单位px
     */
    private int maxPixel  = 1200;

    /**
     * 自定义压缩 参数
     * 压缩到的最大大小，单位KB
     */
    private double maxSize=100;



    public String getLargeImagePath() {
        return largeImagePath;
    }

    public CompressParams setLargeImagePath(String largeImagePath) {
        this.largeImagePath = largeImagePath;
        return this;
    }

    public String getThumbFilePath() {
        return thumbFilePath;
    }

    public CompressParams setThumbFilePath(String thumbFilePath) {
        this.thumbFilePath = thumbFilePath;
        return this;
    }

    public int getMaxPixel() {
        return maxPixel;
    }

    public CompressParams setMaxPixel(int maxPixel) {
        this.maxPixel = maxPixel;
        return this;
    }

    public double getMaxSize() {
        return maxSize;
    }

    public CompressParams setMaxSize(double maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public boolean isEnableCusCompress() {
        return enableCusCompress;
    }

    public CompressParams setEnableCusCompress(boolean enableCusCompress) {
        this.enableCusCompress = enableCusCompress;
        return this;
    }
}
