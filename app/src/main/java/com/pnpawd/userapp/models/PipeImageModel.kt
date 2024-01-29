package com.pnpawd.userapp.models

import android.graphics.Bitmap

class PipeImageModel(image: Bitmap, rotate: Int, index: Int, path: String) {
    private var image: Bitmap
    private var rotate: Int
    private var index: Int
    private var path: String

    init {
        this.image = image
        this.rotate = rotate
        this.index = index
        this.path = path
    }

    fun getImage(): Bitmap {
        return image
    }

    fun setImage(image: Bitmap) {
        this.image = image
    }

    fun getRotate(): Int {
        return rotate
    }

    fun setRotate(rotate: Int) {
        this.rotate = rotate
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }


    fun getPath(): String {
        return path
    }

    fun setPath(path: String) {
        this.path = path
    }
}