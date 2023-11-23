package com.example.sfulounge.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.sfulounge.data.model.Message
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// from tutorial: https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data
class MessagesDataSource(private val chatRoomId: String) : PagingSource<DocumentSnapshot, Message>() {

    private val db = Firebase.firestore

    companion object {
        const val PAGE_SIZE = 10
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Message>): DocumentSnapshot? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Message> {
        try {
            val nextPageNumber = params.key
            var query = db.collection("chat_rooms")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timeCreated", Query.Direction.DESCENDING)

            if (nextPageNumber != null) {
                query = query.startAfter(nextPageNumber)
            }

            // get page
            val documentSnapshot = query.limit(PAGE_SIZE.toLong())
                .get()
                .await()

            val docs = documentSnapshot.documents
            return LoadResult.Page(
                data = docs.map { x -> x.toObject(Message::class.java)!! },
                prevKey = null,
                nextKey = if (docs.isEmpty()) docs.last() else null
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            Log.e("error", "Paging error: ${e.message}")
            return LoadResult.Error(e)
        }
    }
}