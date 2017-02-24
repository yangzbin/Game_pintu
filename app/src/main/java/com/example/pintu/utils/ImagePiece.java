package com.example.pintu.utils;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/2/15.
 * 拼图碎片
 */

public class ImagePiece {
    private int index;//第几张图片碎片
    private Bitmap bitmap;//图片

    public ImagePiece() {
    }

    public ImagePiece(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "ImagePiece{" +
                "index=" + index +
                ", bitmap=" + bitmap +
                '}';
    }
}
