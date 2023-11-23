package com.example.sfulounge.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.sfulounge.R
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.User

class SetupViewModel(private val repository: MainRepository) : ViewModel() {
    val userResult: LiveData<UserResult> = repository.currentUser.map { UserResult(user = it) }
    private val _addPhotoResult = MutableLiveData<PhotoResult>()
    val addPhotoResult: LiveData<PhotoResult> = _addPhotoResult
    private val _deletePhotoResult = MutableLiveData<PhotoResult>()
    val deletePhotoResult: LiveData<PhotoResult> = _deletePhotoResult
    private val _replacePhotoResult = MutableLiveData<PhotoResult>()
    val replacePhotoResult: LiveData<PhotoResult> = _replacePhotoResult

    private val _saved = MutableLiveData<UnitResult>()
    val saved: LiveData<UnitResult> = _saved

    var firstName: String? = null
    var gender: Int? = null

    private val _photos: ArrayList<Photo> = ArrayList()
    val photos: List<Photo> = _photos

    fun loadUser(user: User) {
        firstName = user.firstName
        gender = user.gender
        getPhotos(user.photos)
    }

    fun saveUser() {
        if (firstName == null) {
            _saved.value = UnitResult(error = R.string.error_message_name_is_null)
        }
        else if (gender == null) {
            _saved.value = UnitResult(error = R.string.error_message_gender_is_null)
        } else {
            repository.updateUserBasicInfo(
                firstName!!,
                gender!!,
                onSuccess = { _saved.value = UnitResult() },
                onError = { _saved.value = UnitResult(error = it.exception) }
            )
        }
    }

    private fun getPhotos(photoUrls: List<String>) {
        for (url in photoUrls) {
            val photoResult = PhotoResult(photo = Photo(downloadUrl = url))
            _photos.add(photoResult.photo!!)
            _addPhotoResult.value = photoResult
        }
    }

    // photo.downloadUrl == null && photo.localUri != null
    fun addPhoto(photo: Photo) {
        repository.uploadPhoto(
            photo.localUri!!,
            onSuccess = {
                val photoResult =
                    PhotoResult(photo = Photo(downloadUrl = it, localUri = photo.localUri))
                _photos.add(photoResult.photo!!)
                _addPhotoResult.value = photoResult
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
                val photoResult = PhotoResult(photo = photo)
                _photos.remove(photoResult.photo!!)
                _deletePhotoResult.value = photoResult
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
                val photoResult = PhotoResult(photo = oldPhoto, replaced = newPhoto)
                val idx = _photos.indexOf(photoResult.photo!!)
                _photos[idx] = photoResult.photo
                _replacePhotoResult.value = photoResult
            },
            onError = { error ->
                _replacePhotoResult.value = PhotoResult(error = error.exception)
            }
        )
    }
}