package com.pnpawd.userapp.models

import android.graphics.Bitmap

class ImageModel (image: Bitmap, rotate: Int) {
    private var image: Bitmap
    private var rotate: Int

    init {
        this.image = image
        this.rotate = rotate
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
}