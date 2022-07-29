package com.yuruiyin.richeditor.enumtype;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 图片类型标识（gif、长图）枚举类
 *
 * @author admin
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({ImageTypeMarkEnum.GIF, ImageTypeMarkEnum.LONG})
public @interface ImageTypeMarkEnum {

    String GIF = "GIF";

    String LONG = "长图";

}
