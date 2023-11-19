package com.example.sfulounge.ui.setup

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.MainActivity
import com.example.sfulounge.Util
import com.example.sfulounge.databinding.ActivitySetupImagesBinding
import com.example.sfulounge.ui.components.RandomUriManager
import com.example.sfulounge.ui.components.SingleChoiceDialog

class SetupImagesActivity : AppCompatActivity(), SingleChoiceDialog.SingleChoiceDialogListener {

    private lateinit var binding: ActivitySetupImagesBinding
    private lateinit var setupViewModel: SetupViewModel
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var interestsResultLauncher: ActivityResultLauncher<Intent>
    
    private lateinit var randomUriManager: RandomUriManager

    private var cameraTempUri: Uri? = null

    companion object {
        const val MAX_PHOTOS_LIMIT = 4
        const val MIN_PHOTOS_LIMIT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        Util.checkPermissions(this)

        binding = ActivitySetupImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        randomUriManager = RandomUriManager(this)

        setupViewModel = ViewModelProvider(this, SetupViewModelFactory())
            .get(SetupViewModel::class.java)

        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setupViewModel.addPhoto(Photo(localUri = cameraTempUri))
            } else {
                // camera was canceled
                cameraTempUri?.let { uri -> randomUriManager.deleteUri(uri) }
            }
        }
        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data?.let { randomUriManager.saveToRandomUri(it) }
                if (uri != null) {
                    setupViewModel.addPhoto(Photo(localUri = uri))
                }
            }
        }
        interestsResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                onReturnToHomePage()
            }
        }

        val next = binding.next
        val upload = binding.upload
        val loading = binding.loading
        val photosGrid = binding.gridView
        val photoGridAdapter = PhotoGridAdapter(this, setupViewModel.photos, setupViewModel)

        setupViewModel.addPhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.error != null) {
                showErrorOnSave(photoResult.error)
            }
            if (photoResult.photo != null) {
                photoGridAdapter.notifyDataSetChanged()
            }
        })
        setupViewModel.deletePhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.error != null) {
                showErrorOnSave(photoResult.error)
            }
            if (photoResult.photo != null) {
                photoGridAdapter.notifyDataSetChanged()

                // clean up uri if the photo is using a local uri
                if (photoResult.photo.localUri != null) {
                    randomUriManager.deleteUri(photoResult.photo.localUri)
                }
            }
        })
        setupViewModel.replacePhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.error != null) {
                showErrorOnSave(photoResult.error)
            }
            if (photoResult.photo != null && photoResult.replaced != null) {
                photoGridAdapter.notifyDataSetChanged()

                // clean up uri if the photo is using a local uri
                if (photoResult.photo.localUri != null) {
                    randomUriManager.deleteUri(photoResult.photo.localUri)
                }
            }
        })

        photosGrid.adapter = photoGridAdapter

        upload.setOnClickListener {
            if (setupViewModel.photos.size < MAX_PHOTOS_LIMIT) {
                SingleChoiceDialog.newInstance(
                    "Upload photo",
                    arrayOf("Open Camera", "Select from Gallery")
                ).show(supportFragmentManager, "upload_photo")
            } else {
                showMaxPhotosLimitReached()
            }
        }
        next.setOnClickListener {
            if (setupViewModel.photos.size < MIN_PHOTOS_LIMIT) {
                showMinPhotosLimitError()
            } else if (setupViewModel.photos.size > MAX_PHOTOS_LIMIT) {
                showMaxPhotosLimitReached()
            } else {
                loading.visibility = View.VISIBLE
                val intent = Intent(this, SetupInterestsActivity::class.java)
                interestsResultLauncher.launch(intent)
            }
        }

        // get the current user
        setupViewModel.getUser()
    }

    /**
     * wiring to activities
     */

    private fun onReturnToHomePage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * UI
     */
    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    private fun showMinPhotosLimitError() {
        Toast.makeText(this, "Number of photos < $MIN_PHOTOS_LIMIT", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showMaxPhotosLimitReached() {
        Toast.makeText(this, "Number of photos > $MAX_PHOTOS_LIMIT", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Dialog box: Choose photo from camera or gallery
     */
    override fun onDialogItemIsSelected(dialog: DialogInterface, selectedItemIdx: Int) {
        when (selectedItemIdx) {
            0 -> {
                cameraTempUri = randomUriManager.getRandomUri()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri)
                cameraResultLauncher.launch(intent)
            }
            1 -> {
                galleryResultLauncher.launch(
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                )
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        randomUriManager.close()
    }
}