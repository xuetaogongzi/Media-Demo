package com.wangjw.mediademo.global;

import java.io.File;

/**
 * Created by hjy on 3/24/16.<br>
 */
public class AppConfig {

    /* 存储配置 */
    // 默认本地存储路径
    public static final String DEFAULT_STORAGE_PATH_NAME = "MediaDemo";

    // 缓存路径
    public static final String CACHE_PATH = DEFAULT_STORAGE_PATH_NAME + File.separator + "cache";

    // 下载路径
    public static final String DOWNLOAD_PATH = DEFAULT_STORAGE_PATH_NAME + File.separator + "download";

    // 图片缓存路径
    public static final String IMAGE_CACHE_PATH = CACHE_PATH + File.separator + "image";

    // 视频缓存路径
    public static final String VIDEO_CACHE_PATH = CACHE_PATH + File.separator + "video";

    // 音频缓存路径
    public static final String AUDIO_CACHE_PATH = CACHE_PATH + File.separator + "audio";

}
