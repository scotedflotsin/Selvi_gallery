package com.example.selvigallery.Model

import java.io.File

// ImageItem.kt
data class ImageItem(
    val file: File,
    var isSelected: Boolean = false
)
