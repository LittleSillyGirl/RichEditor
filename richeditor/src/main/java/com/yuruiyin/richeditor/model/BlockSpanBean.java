package com.yuruiyin.richeditor.model;

import android.text.TextUtils;

import com.yuruiyin.richeditor.enumtype.BlockSpanEnum;

/**
 * BlockImageSpan 对应的相关数据, 这里我们可以认为它是一个基类
 *
 * @author admin
 */
public class BlockSpanBean<T> {

    /**
     * 行内标签类型
     */
    @BlockSpanEnum
    private String blockType;

    /**
     * 图片宽高
     */
    private int width;
    private int height;

    /**
     * 图片最大宽高
     */
    private int maxHeight;
    private int maxWidth;

    /**
     * 是否gif
     */
    private boolean isGif;

    /**
     * 是否长图
     */
    private boolean isLong;

    /**
     * 是否为相册图片（用于判断是否给ImageSpan添加圆角）
     */
    private boolean isPhoto;

    /**
     * 是否显示删除图标
     */
    private boolean isShowDel;

    /**
     * 是否来自草稿，（若来自草稿，则插入BlockImageSpan的时候，不在图片前面插入换行符'\n'）
     */
    private boolean isFromDraft;

    /**
     * 图片对应的id
     */
    private String id;

    /**
     * 图片对应的url包括本地路径和网络路径
     */
    private String path;

    /**
     * 块级扩展对象
     */
    private T spanObtainObject;

    public BlockSpanBean() {
        this.blockType = getType();
    }

    public BlockSpanBean(String id, String path) {
        this.blockType = getType();
        this.id = id;
        this.path = path;
    }

    public BlockSpanBean(int width, int height) {
        this.blockType = getType();
        this.width = width;
        this.height = height;
    }

    public BlockSpanBean(int width, int height, String id, String path) {
        this.blockType = getType();
        this.width = width;
        this.height = height;
        this.id = id;
        this.path = path;
    }

    public BlockSpanBean(int width, int height, int maxHeight, int maxWidth, String id, String path) {
        this.blockType = getType();
        this.width = width;
        this.height = height;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        this.id = id;
        this.path = path;
    }

    public BlockSpanBean(@BlockSpanEnum String blockType) {
        this.blockType = blockType;
    }

    public BlockSpanBean(@BlockSpanEnum String blockType, String id, String path) {
        this.blockType = BlockSpanEnum.IMAGE;
        this.blockType = blockType;
        this.id = id;
        this.path = path;
    }

    public BlockSpanBean(@BlockSpanEnum String blockType, int width, int height) {
        this.blockType = blockType;
        this.width = width;
        this.height = height;
    }

    public BlockSpanBean(@BlockSpanEnum String blockType, int width, int height, String id, String path) {
        this.blockType = blockType;
        this.width = width;
        this.height = height;
        this.id = id;
        this.path = path;
    }

    public BlockSpanBean(@BlockSpanEnum String blockType, int width, int height, int maxHeight, int maxWidth, String id, String path) {
        this.blockType = blockType;
        this.width = width;
        this.height = height;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        this.id = id;
        this.path = path;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    public boolean isLong() {
        return isLong;
    }

    public boolean isShowDel() {
        return isShowDel;
    }

    public void setShowDel(boolean showDel) {
        isShowDel = showDel;
    }

    public void setLong(boolean aLong) {
        isLong = aLong;
    }

    public boolean isPhoto() {
        return isPhoto;
    }

    public void setPhoto(boolean photo) {
        isPhoto = photo;
    }

    public boolean isFromDraft() {
        return isFromDraft;
    }

    public void setFromDraft(boolean fromDraft) {
        isFromDraft = fromDraft;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public T getSpanObtainObject() {
        return spanObtainObject;
    }

    public void setSpanObtainObject(T spanObtainObject) {
        this.spanObtainObject = spanObtainObject;
    }

    @Override
    public String toString() {
        return "BlockSpanBean{" +
                "blockType='" + blockType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", maxHeight=" + maxHeight +
                ", maxWidth=" + maxWidth +
                ", isGif=" + isGif +
                ", isLong=" + isLong +
                ", isPhoto=" + isPhoto +
                ", isShowDel=" + isShowDel +
                ", isFromDraft=" + isFromDraft +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", spanObtainObject=" + spanObtainObject +
                '}';
    }

    /**
     * 获取块级标签的类型，比如图片、自定义、段落等
     *
     * @return 块级标签类型
     */
    @BlockSpanEnum
    public String getType() {
        return TextUtils.isEmpty(blockType) ? BlockSpanEnum.IMAGE : blockType;
    }
}
