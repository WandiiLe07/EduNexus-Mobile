@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dut.edunexusmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dut.edunexusmobile.data.model.DiscussionPost
import com.dut.edunexusmobile.data.model.DiscussionReply
import com.dut.edunexusmobile.ui.viewmodel.DiscussionViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ModuleDiscussionsScreen(
    moduleCode: String,
    onPostClick: (DiscussionPost) -> Unit,
    viewModel: DiscussionViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()

    var showNewPostDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(moduleCode) {
        viewModel.loadPosts(moduleCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("$moduleCode Discussions")
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showNewPostDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New post"
                )
            }
        }
    ) { padding ->

        if (posts.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No discussions yet for $moduleCode. Ask the first question!"
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(posts) { post ->

                    PostCard(post) {
                        onPostClick(post)
                    }
                }
            }
        }

        if (showNewPostDialog) {

            NewPostDialog(

                onDismiss = {
                    showNewPostDialog = false
                },

                onSubmit = { title, body ->

                    val user =
                        FirebaseAuth.getInstance().currentUser

                    if (user != null) {

                        viewModel.createPost(
                            moduleCode,
                            title,
                            body,
                            user.uid,
                            user.displayName ?: "Student"
                        )
                    }

                    showNewPostDialog = false
                }
            )
        }
    }
}

@Composable
private fun PostCard(
    post: DiscussionPost,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                post.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                post.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                "${post.authorName} · ${post.replyCount} replies",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun NewPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var title by remember {
        mutableStateOf("")
    }

    var body by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("New discussion post")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = title,

                    onValueChange = {
                        title = it
                    },

                    label = {
                        Text("Title")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = body,

                    onValueChange = {
                        body = it
                    },

                    label = {
                        Text("Your question or topic")
                    },

                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {

            TextButton(

                enabled =
                    title.isNotBlank() &&
                            body.isNotBlank(),

                onClick = {
                    onSubmit(title, body)
                }

            ) {
                Text("Post")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DiscussionThreadScreen(
    post: DiscussionPost,
    viewModel: DiscussionViewModel = viewModel()
) {
    val replies by viewModel.replies.collectAsState()

    var replyText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(post.id) {
        viewModel.loadReplies(post.id)
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(post.title)
                }
            )
        },

        bottomBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = replyText,

                    onValueChange = {
                        replyText = it
                    },

                    label = {
                        Text("Write a reply")
                    },

                    modifier = Modifier.weight(1f)
                )

                IconButton(

                    enabled =
                        replyText.isNotBlank(),

                    onClick = {

                        val user =
                            FirebaseAuth.getInstance()
                                .currentUser

                        if (user != null) {

                            viewModel.addReply(
                                post.id,
                                replyText,
                                user.uid,
                                user.displayName ?: "Student"
                            )

                            replyText = ""
                        }
                    }

                ) {

                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send reply"
                    )
                }
            }
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding =
                PaddingValues(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)

        ) {

            item {

                Column {

                    Text(
                        post.body,
                        style =
                            MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        "Posted by ${post.authorName}",
                        style =
                            MaterialTheme.typography.labelSmall
                    )

                    Divider(
                        modifier =
                            Modifier.padding(top = 12.dp)
                    )
                }
            }

            items(replies) { reply ->
                ReplyRow(reply)
            }
        }
    }
}

@Composable
private fun ReplyRow(
    reply: DiscussionReply
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Text(
            reply.authorName,
            style =
                MaterialTheme.typography.labelMedium
        )

        Text(
            reply.body,
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}