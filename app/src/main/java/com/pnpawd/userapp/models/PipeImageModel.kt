package com.pnpawd.userapp.models

import android.graphics.Bitmap

class PipeImageModel(image: String, rotate: Int, index: Int, path: String) {
    private var image: String
    private var rotate: Int
    private var index: Int
    private var path: String

    init {
        this.image = image
        this.rotate = rotate
        this.index = index
        this.path = path
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image: String) {
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