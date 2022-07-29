package com.yuruiyin.richeditor.model;

/**
 * BlockImageSpan 点击的位置信息
 *
 * @author admin
 */
public class SpanTouchBean {

    /**
     * 点击的x、y
     */
    private float x;
    private float y;

    /**
     * 点击的类型
     */
    private int type;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SpanTouchBean{" +
                "x=" + x +
                ", y=" + y +
                ", type=" + type +
                '}';
    }
}
