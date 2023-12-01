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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sfulounge.data.model.Message
import com.example.sfulounge.data.model.User
import com.example.sfulounge.databinding.ActivityMessagesBinding
import com.example.sfulounge.ui.components.RandomUriManager
import com.example.sfulounge.ui.components.UploadDialog
import com.example.sfulounge.ui.user_profile.UserProfileActivity
import com.google.common.collect.ImmutableList
import java.util.LinkedList

class MessagesActivity : AppCompatActivity(), UploadDialog.UploadDialogListener, AttachmentAdapter.Listener, MessageAdapter.Listener {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var uploadDialog: UploadDialog
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var attachmentsAdapter: AttachmentAdapter

    private lateinit var randomUriManager: RandomUriManager

    private val messages = LinkedList<Message>()
    private val attachments = ArrayList<Attachment>()

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

        val messagesView = binding.recyclerView
        val attachmentsView = binding.attachments
        val send = binding.send
        val more = binding.more
        val input = binding.input
        val messagesAdapter = MessageAdapter(
            messagesViewModel.cachedUsers,
            messagesViewModel.userId,
            this
        )

        messagesView.adapter = messagesAdapter
        messagesView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        attachmentsView.adapter = attachmentsAdapter
        attachmentsView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        attachmentsAdapter.submitList(attachments)

        send.setOnClickListener {
            val text = input.text.toString()
            if (text.isNotEmpty() || attachments.isNotEmpty()) {
                input.text.clear()
                send.isEnabled = false
                messagesViewModel.sendMessage(text, ImmutableList.copyOf(attachments))
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
                val numItems = attachments.size
                attachments.clear()
                attachmentsAdapter.notifyItemRangeRemoved(0, numItems)
            }
        }
        messagesViewModel.messagesResult.observe(this) {
            if (it.error != null) {
                showMessagesFailedToLoad(it.error)
            } else {
                // load the initial messages.
                // guarantees that all members of chatroom are loaded into usersMap
                // so it is safe to go usersMap[userId]!! in the adapter
                messages.addAll(it.messages)
                messagesAdapter.submitList(messages)
            }
        }
        messagesViewModel.pushMessageResult.observe(this) {
            val msg = it ?: return@observe
            messages.addFirst(msg)
            messagesAdapter.notifyItemInserted(0)
        }

        messagesViewModel.getUsers(members)
    }

    private fun showMessageFailedToSend(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }
    private fun showMessagesFailedToLoad(@StringRes errorString: Int) {
        Toast.makeText(this, getString(errorString), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesViewModel.updateLastSeen()
        messagesViewModel.cleanup()
    }

    private fun addImageAttachment(uri: Uri) {
        val position = attachments.size
        attachments.add(
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
        attachments.removeAt(position)
        attachmentsAdapter.notifyItemRemoved(position)
    }

    override fun onProfileImageClicked(user: User?) {
        if (user != null) {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra(UserProfileActivity.INTENT_USER_ID, user.userId)
            startActivity(intent)
        }
    }
}