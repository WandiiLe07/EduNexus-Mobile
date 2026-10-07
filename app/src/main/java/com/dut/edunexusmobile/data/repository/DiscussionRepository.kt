package com.dut.edunexusmobile.data.repository

import com.dut.edunexusmobile.data.model.DiscussionPost
import com.dut.edunexusmobile.data.model.DiscussionReply
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles discussion threads. Replies are stored as a subcollection under each
 * post (discussionPosts/{postId}/replies) so a thread and its replies are
 * always fetched/scoped together.
 */
class DiscussionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val postsCollection = firestore.collection("discussionPosts")

    fun observePostsByModule(moduleCode: String): Flow<List<DiscussionPost>> = callbackFlow {
        val listener = postsCollection
            .whereEqualTo("moduleCode", moduleCode)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiscussionPost::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun observeReplies(postId: String): Flow<List<DiscussionReply>> = callbackFlow {
        val listener = postsCollection.document(postId).collection("replies")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val replies = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiscussionReply::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(replies)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(
        moduleCode: String,
        title: String,
        body: String,
        authorUid: String,
        authorName: String
    ): Result<Unit> {
        return try {
            val post = DiscussionPost(
                moduleCode = moduleCode,
                title = title,
                body = body,
                authorUid = authorUid,
                authorName = authorName
            )
            postsCollection.add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReply(
        postId: String,
        body: String,
        authorUid: String,
        authorName: String
    ): Result<Unit> {
        return try {
            val reply = DiscussionReply(
                body = body,
                authorUid = authorUid,
                authorName = authorName
            )
            postsCollection.document(postId).collection("replies").add(reply).await()
            // Keep a running reply count on the parent post for quick display in lists.
            postsCollection.document(postId).update("replyCount", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
