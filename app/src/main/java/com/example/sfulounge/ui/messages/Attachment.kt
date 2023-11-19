package com.example.sfulounge.ui.messages

import android.graphics.Bitmap
import android.net.Uri

data class Attachment (
    val localUri: Uri,
    val fileType: AttachmentType,
    val videoThumbnail: Bitmap? = null,
    val fileName: String? = null
)