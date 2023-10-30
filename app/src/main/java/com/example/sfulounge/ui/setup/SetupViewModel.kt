package com.example.sfulounge.ui.setup

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sfulounge.data.MainRepository
import com.example.sfulounge.data.model.DepthInfo
import com.example.sfulounge.data.model.User

class SetupViewModel(private val repository: MainRepository) : ViewModel() {
    private var _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private var _uploadedUris = MutableLiveData<Uri>()
    val uploadedUris: LiveData<Uri> = _uploadedUris

    var firstName: String? = null
    var lastName: String? = null
    var interests: List<String>? = null
    var depthQuestions: List<DepthInfo>? = null
    var photos: List<Uri> = ArrayList()

    fun getUser() {
        repository.getUser(
            onSuccess = { _user.value = it },
            onError = { }
        )
    }

    fun updateUserProfile() {
        val uploadedPhotos = ArrayList<String>()
        uploadedPhotos.addAll(_user.value!!.photos)

        // upload photos to get url
        // add urls to user.photos

        val totalNumPhotos = uploadedPhotos.size + photos.size
        var id = _user.value!!.photos.size
        for (photo in photos) {
            id++
            repository.uploadPhoto(
                photo,
                id,
                onSuccess = {
                    uploadedPhotos.add(it)
                    _uploadedUris.value = photo

                    // all photos have been uploaded
                    // now update the database
                    if (uploadedPhotos.size == totalNumPhotos) {
                        repository.updateUser(
                            User(
                                userId = _user.value!!.userId,
                                isProfileInitialized = true,
                                firstName = firstName,
                                lastName = lastName,
                                interests = interests ?: ArrayList(),
                                depthQuestions = depthQuestions ?: ArrayList(),
                                photos = uploadedPhotos
                            ),
                            onSuccess = { },
                            onError = { }
                        )
                    }
                },
                onError = { throw it.exception }
            )
        }
    }
}