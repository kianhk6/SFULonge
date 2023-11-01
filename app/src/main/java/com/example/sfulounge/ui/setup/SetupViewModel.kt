package com.example.sfulounge.ui.setup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.DepthInfo

class SetupViewModel(private val repository: MainRepository) : ViewModel() {
    private var _userResult = MutableLiveData<UserResult>()
    val userResult: LiveData<UserResult> = _userResult
    private val _addPhotoResult = MutableLiveData<PhotoResult>()
    val addPhotoResult: LiveData<PhotoResult> = _addPhotoResult
    private val _deletePhotoResult = MutableLiveData<PhotoResult>()
    val deletePhotoResult: LiveData<PhotoResult> = _deletePhotoResult
    private val _replacePhotoResult = MutableLiveData<PhotoResult>()
    val replacePhotoResult: LiveData<PhotoResult> = _replacePhotoResult

    private val _saved = MutableLiveData<UnitResult>()
    val saved: LiveData<UnitResult> = _saved

    var firstName: String? = null
    var lastName: String? = null
    var interests: List<String>? = null
    var depthQuestions: List<DepthInfo>? = null

    fun getUser() {
        repository.getUser(
            onSuccess = { user ->
                _userResult.value = UserResult(user = user)
                getPhotos(user.photos)
            },
            onError = { throw IllegalStateException("user cannot be null") }
        )
    }

    fun saveUser(photos: List<Photo>) {
        val user = _userResult.value!!.user!!
        firstName?.let { user.firstName = it }
        lastName?.let { user.lastName = it }
        interests?.let { user.interests = it }
        depthQuestions?.let { user.depthQuestions = it }
        user.photos = photos.map { photo -> photo.downloadUrl!! }

        repository.updateUser(
            user,
            onSuccess = { _saved.value = UnitResult() },
            onError = { _saved.value = UnitResult(error = it.exception) }
        )
    }

    private fun getPhotos(photoUrls: List<String>) {
        for (url in photoUrls) {
            _addPhotoResult.value = PhotoResult(photo = Photo(downloadUrl = url))
        }
    }

    // photo.downloadUrl == null && photo.localUri != null
    fun addPhoto(photo: Photo) {
        repository.uploadPhoto(
            photo.localUri!!,
            onSuccess = {
                _addPhotoResult.value =
                    PhotoResult(photo = Photo(downloadUrl = it, localUri = photo.localUri))
            },
            onError = { error ->
                _addPhotoResult.value = PhotoResult(error = error.exception)
            }
        )
    }

    // photo.downloadUrl != null && photo.localUri == null
    fun deletePhoto(photo: Photo) {
        repository.deletePhoto(
            photo.downloadUrl!!,
            onSuccess = {
                _deletePhotoResult.value = PhotoResult(photo = photo)
            },
            onError = { error ->
                _deletePhotoResult.value = PhotoResult(error = error.exception)
            }
        )
    }

    // oldPhoto.downloadUrl != null && newPhoto.localUri != null
    fun replacePhoto(oldPhoto: Photo, newPhoto: Photo) {
        repository.replacePhoto(
            newPhoto.localUri!!,
            oldPhoto.downloadUrl!!,
            onSuccess = {
                _replacePhotoResult.value = PhotoResult(photo = oldPhoto, replaced = newPhoto)
            },
            onError = { error ->
                _replacePhotoResult.value = PhotoResult(error = error.exception)
            }
        )
    }
}