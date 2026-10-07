
package com.dut.edunexusmobile.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dut.edunexusmobile.data.model.DiscussionPost
import com.dut.edunexusmobile.data.model.DiscussionReply
import com.dut.edunexusmobile.data.model.Resource
import com.dut.edunexusmobile.data.repository.AuthRepository
import com.dut.edunexusmobile.data.repository.DiscussionRepository
import com.dut.edunexusmobile.data.repository.ResourceRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiStatus {
    object Idle : UiStatus()
    object Loading : UiStatus()
    object Success : UiStatus()
    data class Error(val message: String) : UiStatus()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _status = MutableStateFlow<UiStatus>(UiStatus.Idle)
    val status: StateFlow<UiStatus> = _status.asStateFlow()

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    fun signUp(email: String, password: String, displayName: String) {
        _status.value = UiStatus.Loading

        viewModelScope.launch {
            _status.value = repository.signUp(
                email, password, displayName
            ).fold(
                onSuccess = { UiStatus.Success },
                onFailure = {
                    UiStatus.Error(it.message ?: "Sign up failed")
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        _status.value = UiStatus.Loading

        viewModelScope.launch {
            _status.value = repository.signIn(
                email, password
            ).fold(
                onSuccess = { UiStatus.Success },
                onFailure = {
                    UiStatus.Error(it.message ?: "Sign in failed")
                }
            )
        }
    }

    fun signOut() {
        repository.signOut()
    }
}

class ResourceViewModel(
    private val repository: ResourceRepository = ResourceRepository()
) : ViewModel() {

    private val _resources =
        MutableStateFlow<List<Resource>>(emptyList())

    val resources: StateFlow<List<Resource>> =
        _resources.asStateFlow()

    private val _uploadStatus =
        MutableStateFlow<UiStatus>(UiStatus.Idle)

    val uploadStatus: StateFlow<UiStatus> =
        _uploadStatus.asStateFlow()

    private val _deleteStatus =
        MutableStateFlow<UiStatus>(UiStatus.Idle)

    val deleteStatus: StateFlow<UiStatus> =
        _deleteStatus.asStateFlow()

    private var resourceJob: Job? = null

    fun loadResources(moduleCode: String) {
        resourceJob?.cancel()

        resourceJob = viewModelScope.launch {
            repository.observeResourcesByModule(moduleCode)
                .collect {
                    _resources.value = it
                }
        }
    }

    fun uploadResource(
        contentResolver: ContentResolver,
        fileUri: Uri,
        fileName: String,
        fileSizeBytes: Long,
        moduleCode: String,
        title: String,
        uploaderUid: String,
        uploaderName: String
    ) {
        _uploadStatus.value = UiStatus.Loading

        viewModelScope.launch {
            _uploadStatus.value = repository.uploadResource(
                contentResolver,
                fileUri,
                fileName,
                fileSizeBytes,
                moduleCode,
                title,
                uploaderUid,
                uploaderName
            ).fold(
                onSuccess = { UiStatus.Success },
                onFailure = {
                    UiStatus.Error(it.message ?: "Upload failed")
                }
            )
        }
    }

    fun deleteResource(resource: Resource) {
        if (_deleteStatus.value is UiStatus.Loading) return

        _deleteStatus.value = UiStatus.Loading

        viewModelScope.launch {
            _deleteStatus.value = repository.deleteResource(resource).fold(
                onSuccess = {
                    _resources.value = _resources.value.filterNot {
                        it.id == resource.id
                    }
                    UiStatus.Success
                },
                onFailure = {
                    UiStatus.Error(it.message ?: "Delete failed")
                }
            )
        }
    }
}

class DiscussionViewModel(
    private val repository: DiscussionRepository =
        DiscussionRepository()
) : ViewModel() {

    private val _posts =
        MutableStateFlow<List<DiscussionPost>>(emptyList())

    val posts: StateFlow<List<DiscussionPost>> =
        _posts.asStateFlow()

    private val _replies =
        MutableStateFlow<List<DiscussionReply>>(emptyList())

    val replies: StateFlow<List<DiscussionReply>> =
        _replies.asStateFlow()

    private val _actionStatus =
        MutableStateFlow<UiStatus>(UiStatus.Idle)

    val actionStatus: StateFlow<UiStatus> =
        _actionStatus.asStateFlow()

    fun loadPosts(moduleCode: String) {
        viewModelScope.launch {
            repository.observePostsByModule(moduleCode)
                .collect {
                    _posts.value = it
                }
        }
    }

    fun loadReplies(postId: String) {
        viewModelScope.launch {
            repository.observeReplies(postId)
                .collect {
                    _replies.value = it
                }
        }
    }

    fun createPost(
        moduleCode: String,
        title: String,
        body: String,
        authorUid: String,
        authorName: String
    ) {
        _actionStatus.value = UiStatus.Loading

        viewModelScope.launch {
            _actionStatus.value = repository.createPost(
                moduleCode,
                title,
                body,
                authorUid,
                authorName
            ).fold(
                onSuccess = { UiStatus.Success },
                onFailure = {
                    UiStatus.Error(
                        it.message ?: "Could not create post"
                    )
                }
            )
        }
    }

    fun addReply(
        postId: String,
        body: String,
        authorUid: String,
        authorName: String
    ) {
        _actionStatus.value = UiStatus.Loading

        viewModelScope.launch {
            _actionStatus.value = repository.addReply(
                postId,
                body,
                authorUid,
                authorName
            ).fold(
                onSuccess = { UiStatus.Success },
                onFailure = {
                    UiStatus.Error(
                        it.message ?: "Could not send reply"
                    )
                }
            )
        }
    }
}
