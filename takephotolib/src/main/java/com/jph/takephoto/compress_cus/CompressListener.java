package com.jph.takephoto.compress_cus;

import java.io.File;

/**
 * author: C_CHEUNG
 * created on: 2016/11/23
 * description: 压缩监听
 */
public interface CompressListener {
    /**
     * 开始压缩 在其中展示Dialog
     */
    void onStart();

    /**
     * 压缩成功 返回压缩后的文件
     */
    void onSuccess(File file);

    /**
     * 压缩失败
     * @param e
     */
    void onError(Throwable e);
}
