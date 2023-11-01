package com.example.sfulounge.ui.setup

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.Util
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivitySetupBasicInfoBinding
import com.example.sfulounge.ui.components.SingleChoiceDialog
import java.io.File

/**
 * Step 1 of setting up user profile. When user clicks next it will start Step 2
 * and then Step 2 will start Step 3
 */
class SetupBasicInfoActivity : AppCompatActivity(), SingleChoiceDialog.SingleChoiceDialogListener {

    private lateinit var binding: ActivitySetupBasicInfoBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var setupViewModel: SetupViewModel
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>

    private var tempFileUid = 0
    private val uriPool = HashSet<Uri>()
    private var cameraTempUri: Uri? = null

    private val photos: ArrayList<Photo> = ArrayList()

    companion object {
        const val MAX_PHOTOS_LIMIT = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Util.checkPermissions(this)

        binding = ActivitySetupBasicInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel = ViewModelProvider(this, SetupViewModelFactory())
            .get(SetupViewModel::class.java)

        launcher = registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                finish()
            }
        }
        cameraResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setupViewModel.addPhoto(Photo(localUri = cameraTempUri))
            } else {
                // camera was canceled
                cameraTempUri?.let { uri -> deleteUri(uri) }
            }
        }
        galleryResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data?.let { saveToRandomUri(it) }
                if (uri != null) {
                    setupViewModel.addPhoto(Photo(localUri = cameraTempUri))
                }
            }
        }

        val next = binding.next
        val upload = binding.upload
        val loading = binding.loading
        val photosGrid = binding.gridView
        val photoGridAdapter = PhotoGridAdapter(this, photos)

        var isInitialized = false
        setupViewModel.userResult.observe(this, Observer {
            val userResult = it ?: return@Observer
            if (!isInitialized) {
                loadUser(userResult.user!!)
                isInitialized = true
            }
        })
        setupViewModel.saved.observe(this, Observer {
            val unitResult = it ?: return@Observer
            loading.visibility = View.GONE
            if (unitResult.error != null) {
                showErrorOnSave(unitResult.error)
            } else {
                launcher.launch(Intent(this, SetupInterestsActivity::class.java))
            }
        })
        setupViewModel.addPhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.photo != null) {
                photos.add(photoResult.photo)
                photoGridAdapter.notifyDataSetChanged()
            }
        })
        setupViewModel.deletePhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.photo != null) {
                photos.remove(photoResult.photo)
                photoGridAdapter.notifyDataSetChanged()

                // clean up uri if the photo is using a local uri
                if (photoResult.photo.localUri != null) {
                    deleteUri(photoResult.photo.localUri)
                }
            }
        })
        setupViewModel.replacePhotoResult.observe(this, Observer {
            val photoResult = it ?: return@Observer
            if (photoResult.photo != null && photoResult.replaced != null) {
                val idx = photos.indexOf(photoResult.photo)
                photos[idx] = photoResult.photo
                photoGridAdapter.notifyDataSetChanged()

                // clean up uri if the photo is using a local uri
                if (photoResult.photo.localUri != null) {
                    deleteUri(photoResult.photo.localUri)
                }
            }
        })

        photosGrid.adapter = photoGridAdapter

        upload.setOnClickListener {
            if (photos.size < MAX_PHOTOS_LIMIT) {
                SingleChoiceDialog.newInstance(
                    "Upload photo",
                    arrayOf("Open Camera", "Select from Gallery")
                ).show(supportFragmentManager, "upload_photo")
            } else {
                showMaxPhotosLimitReached()
            }
        }
        next.setOnClickListener {
            loading.visibility = View.VISIBLE
            setupViewModel.saveUser(photos)
        }

        // get the current user
        setupViewModel.getUser()
    }

    private fun loadUser(user: User) {
        binding.firstName.setText(user.firstName)
        binding.lastName.setText(user.lastName)
        setupViewModel.firstName = user.firstName
        setupViewModel.lastName = user.lastName
    }

    private fun showErrorOnSave(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    private fun showMaxPhotosLimitReached() {
        Toast.makeText(this, "Number of photos > ${MAX_PHOTOS_LIMIT-1}", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Getting temporary uri to store the images when the image
     * is selected from camera or gallery
     */
    private fun saveToRandomUri(sourceUri: Uri): Uri {
        val destUri = getRandomUri()
        contentResolver.openInputStream(sourceUri)?.use { istream ->
            contentResolver.openOutputStream(destUri).use { ostream ->
                if (ostream != null) {
                    istream.copyTo(ostream)
                }
            }
        }
        return destUri
    }
    private fun getRandomUri(): Uri {
        val file = File.createTempFile("$tempFileUid", ".jpg", cacheDir)
        tempFileUid++
        val uri = FileProvider.getUriForFile(this, packageName, file)
        uriPool.add(uri)
        return uri
    }
    private fun deleteUri(uri: Uri) {
        if (uriPool.contains(uri)) {
            contentResolver.delete(uri, null, null)
            uriPool.remove(uri)
        }
    }

    /**
     * Dialog box: Choose photo from camera or gallery
     */
    override fun onDialogItemIsSelected(dialog: DialogInterface, selectedItemIdx: Int) {
        when (selectedItemIdx) {
            0 -> {
                cameraTempUri = getRandomUri()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraTempUri)
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
        for (uri in uriPool) {
            deleteUri(uri)
        }
    }
}