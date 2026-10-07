package com.dut.edunexusmobile.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a single uploaded academic resource (e.g. notes, past paper, slides).
 * The actual file lives in Firebase Storage; this document stores its metadata
 * and is what gets queried/filtered/displayed in the app.
 */
data class Resource(
    var id: String = "",
    var title: String = "",
    var moduleCode: String = "",          // e.g. "MOSA202"
    var fileName: String = "",
    var fileUrl: String = "",             // Firebase Storage download URL
    var fileSizeBytes: Long = 0L,
    var uploadedByUid: String = "",
    var uploadedByName: String = "",
    @ServerTimestamp
    var uploadedAt: Date? = null
)

/**
 * A discussion thread starter within a specific module.
 */
data class DiscussionPost(
    var id: String = "",
    var moduleCode: String = "",
    var title: String = "",
    var body: String = "",
    var authorUid: String = "",
    var authorName: String = "",
    @get:PropertyName("replyCount") @set:PropertyName("replyCount")
    var replyCount: Int = 0,
    @ServerTimestamp
    var createdAt: Date? = null
)

/**
 * A reply within a discussion thread. Stored as a subcollection under the parent post:
 * discussionPosts/{postId}/replies/{replyId}
 */
data class DiscussionReply(
    var id: String = "",
    var body: String = "",
    var authorUid: String = "",
    var authorName: String = "",
    @ServerTimestamp
    var createdAt: Date? = null
)
