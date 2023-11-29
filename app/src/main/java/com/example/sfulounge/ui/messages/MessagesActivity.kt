package com.example.sfulounge.ui.messages

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sfulounge.databinding.ActivityMessagesBinding
import com.example.sfulounge.ui.components.RandomUriManager
import com.example.sfulounge.ui.components.UploadDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity(), UploadDialog.UploadDialogListener, AttachmentAdapter.Listener {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var uploadDialog: UploadDialog
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var attachmentsAdapter: AttachmentAdapter

    private lateinit var randomUriManager: RandomUriManager

    companion object {
        const val INTENT_CHATROOM_ID = "chatroom_id"
        const val INTENT_MEMBER_IDS = "member_ids"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatRoomId = intent.getStringExtra(INTENT_CHATROOM_ID)
            ?: throw IllegalStateException("chat room id is null")
        val members = intent.getStringArrayListExtra(INTENT_MEMBER_IDS)
            ?: throw IllegalStateException("chat room members list is null")

        messagesViewModel = ViewModelProvider(this, MessagesViewModelFactory(chatRoomId))
            .get(MessagesViewModel::class.java)

        randomUriManager = RandomUriManager(this)
        uploadDialog = UploadDialog()

        attachmentsAdapter = AttachmentAdapter(this)

        cameraResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            val uri = randomUriManager.lastUri
            if (result.resultCode == RESULT_OK) {
                if (uri != null) {
                    addImageAttachment(uri)
                }
            } else {
                // camera was canceled
                uri?.let { randomUriManager.deleteUri(it) }
            }
        }
        galleryResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data?.let { randomUriManager.saveToRandomUri(it) }
                if (uri != null) {
                    addImageAttachment(uri)
                }
            }
        }

        val messages = binding.recyclerView
        val attachmentsView = binding.attachments
        val send = binding.send
        val more = binding.more
        val input = binding.input
        val pagingAdapter = MessageAdapter(messagesViewModel.cachedUsers, messagesViewModel.userId)

        messages.adapter = pagingAdapter
        messages.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        attachmentsView.adapter = attachmentsAdapter
        attachmentsView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        attachmentsAdapter.submitList(messagesViewModel.attachments)

        send.setOnClickListener {
            val text = input.text.toString()
            if (text.isNotEmpty() || messagesViewModel.attachments.isNotEmpty()) {
                input.text.clear()
                send.isEnabled = false
                messagesViewModel.sendMessage(text)
            }
        }

        more.setOnClickListener {
            uploadDialog.show(supportFragmentManager, "upload_dialog")
        }

        messagesViewModel.sendResult.observe(this) { result ->
            send.isEnabled = true
            if (result.error != null) {
                showMessageFailedToSend(result.error)
            } else {
                attachmentsAdapter.submitList(messagesViewModel.attachments)
            }
        }

        lifecycleScope.launch {
            messagesViewModel.flow.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

        messagesViewModel.getUsers(members)
        messagesViewModel.registerMessagesListener()
    }

    private fun showMessageFailedToSend(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesViewModel.updateLastSeen()
        messagesViewModel.unregisterMessagesListener()
    }

    private fun addImageAttachment(uri: Uri) {
        val position = messagesViewModel.attachments.size
        messagesViewModel.attachments.add(
            Attachment(localUri = uri, fileType = AttachmentType.IMAGE)
        )
        attachmentsAdapter.notifyItemInserted(position)
    }

    override fun onGalleryClick() {
        galleryResultLauncher.launch(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
    }

    override fun onCameraClick() {
        randomUriManager.getRandomUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, randomUriManager.lastUri)
        cameraResultLauncher.launch(intent)
    }

    override fun onRemoveAttachment(position: Int) {
        messagesViewModel.attachments.removeAt(position)
        attachmentsAdapter.notifyItemRemoved(position)
    }
}