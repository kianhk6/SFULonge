package com.example.sfulounge.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class RandomUriManager(private val context: Context) : AutoCloseable {

    private var _lastUri: Uri? = null
    val lastUri: Uri? = _lastUri

    private val uriPool = HashSet<Uri>()

    fun saveToRandomUri(sourceUri: Uri): Uri {
        val destUri = make()
        context.contentResolver.openInputStream(sourceUri)?.use { istream ->
            context.contentResolver.openOutputStream(destUri).use { ostream ->
                if (ostream != null) {
                    istream.copyTo(ostream)
                }
            }
        }
        return destUri
    }

    fun getRandomUri() {
        _lastUri = make()
    }

    fun deleteLastUri() {
        val uri = _lastUri
        if (uri != null) {
            delete(uri)
            _lastUri = null
        }
    }
    
    fun deleteUri(uri: Uri) {
        delete(uri)
    }

    private fun delete(uri: Uri) {
        if (uriPool.contains(uri)) {
            context.contentResolver.delete(uri, null, null)
            uriPool.remove(uri)
        }
    }

    private fun make(): Uri {
        val file = File.createTempFile("img", null, context.cacheDir)
        val uri = FileProvider.getUriForFile(context, context.packageName, file)
        uriPool.add(uri)
        return uri
    }

    override fun close() {
        for (uri in uriPool) {
            delete(uri)
        }
    }
}