package com.pandalibs.pandacanvaslib

import android.graphics.Path

class CanvasPojo {
    private var _path: Path? = null
    var paintColor: Int
    var paintWidth: Float
    var shadowRadius: Float
    var shadowX: Float
    var shadowY: Float

    constructor(
        _path: Path?,
        paintColor: Int,
        paintwidth: Float,
        shadowRadius: Float,
        shadowX: Float,
        shadowY: Float
    ) {
        this._path = _path
        this.paintColor = paintColor
        paintWidth = paintwidth
        this.shadowRadius = shadowRadius
        this.shadowX = shadowX
        this.shadowY = shadowY
    }

    constructor(
        paintColor: Int,
        paintWidth: Float,
        shadowRadius: Float,
        shadowX: Float,
        shadowY: Float
    ) {
        this.paintColor = paintColor
        this.paintWidth = paintWidth
        this.shadowRadius = shadowRadius
        this.shadowX = shadowX
        this.shadowY = shadowY
    }

    fun getPath(): Path? {
        return _path
    }

    fun setPath(_path: Path?) {
        this._path = _path
    }
}