package com.example.sfulounge.ui.setup

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sfulounge.Util
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
        cameraResultLauncher = registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                //TODO(display and upload photo)
            }
        }
        galleryResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data?.let { copyToTempUri(it) }
                if (uri != null) {
                    //TODO(display and upload photo)

                }
            }
        }

        val next = binding.next
        val firstName = binding.firstName
        val lastName = binding.lastName
        val upload = binding.upload
        val photosGrid = binding.gridView

        var isInitialized = false
        setupViewModel.user.observe(this, Observer {
            val user = it ?: return@Observer
            if (!isInitialized) {
                firstName.setText(user.firstName)
                lastName.setText(user.lastName)
                updatePhotos(user.photos)
                setupViewModel.firstName = user.firstName
                setupViewModel.lastName = user.lastName
                isInitialized = true
            }
        })
        setupViewModel.uploadedUris.observe(this, Observer {
            val uri = it ?: return@Observer
            cleanUpTempUri(uri)
        })

        upload.setOnClickListener {
            SingleChoiceDialog.newInstance(
                "Upload photo",
                arrayOf("Open Camera", "Select from Gallery")
            ).show(supportFragmentManager, "upload_photo")
        }
        next.setOnClickListener {
            launcher.launch(Intent(this, SetupInterestsActivity::class.java))
        }
    }

    private fun updatePhotos(photos: List<String>) {
        //TODO
    }

    private fun copyToTempUri(sourceUri: Uri): Uri {
        val destUri = useTempUri()
        contentResolver.openInputStream(sourceUri)?.use { istream ->
            contentResolver.openOutputStream(destUri).use { ostream ->
                if (ostream != null) {
                    istream.copyTo(ostream)
                }
            }
        }
        return destUri
    }
    private fun useTempUri(): Uri {
        val file = File.createTempFile("$tempFileUid", ".jpg", cacheDir)
        tempFileUid++
        val uri = FileProvider.getUriForFile(this, packageName, file)
        uriPool.add(uri)
        return uri
    }
    private fun cleanUpTempUri(uri: Uri) {
        if (uriPool.contains(uri)) {
            contentResolver.delete(uri, null, null)
        }
    }

    override fun onDialogItemIsSelected(dialog: DialogInterface, selectedItemIdx: Int) {
        when (selectedItemIdx) {
            0 -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, useTempUri())
                cameraResultLauncher.launch(intent)
            }
            1 -> {
                galleryResultLauncher.launch(
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                )
            }
        }
    }
}