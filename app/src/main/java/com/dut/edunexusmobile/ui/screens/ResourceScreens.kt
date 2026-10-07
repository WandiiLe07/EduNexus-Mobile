
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dut.edunexusmobile.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dut.edunexusmobile.data.model.Resource
import com.dut.edunexusmobile.ui.viewmodel.ResourceViewModel
import com.dut.edunexusmobile.ui.viewmodel.UiStatus
import com.google.firebase.auth.FirebaseAuth

// ---------- Resource List ----------

@Composable
fun ResourceListScreen(
    moduleCode: String,
    onUploadClick: () -> Unit,
    viewModel: ResourceViewModel = viewModel()
) {
    val resources by viewModel.resources.collectAsState()
    val context = LocalContext.current
    val deleteStatus by viewModel.deleteStatus.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var pendingDelete by remember {
        mutableStateOf<Resource?>(null)
    }

    pendingDelete?.let { resource ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete resource?") },
            text = {
                Text(
                    "Remove '${resource.title}' from EduNexus? " +
                            "The Supabase file will remain stored."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteResource(resource)
                        pendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(moduleCode) {
        viewModel.loadResources(moduleCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("$moduleCode Resources")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onUploadClick
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Upload resource"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (deleteStatus is UiStatus.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (deleteStatus is UiStatus.Error) {
                Text(
                    (deleteStatus as UiStatus.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (resources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment =
                        androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "No resources yet for $moduleCode. " +
                                "Be the first to share one!"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(resources) { resource ->
                        ResourceCard(
                            resource = resource,
                            canDelete =
                                currentUid != null &&
                                        currentUid == resource.uploadedByUid,
                            onDelete = {
                                pendingDelete = resource
                            },
                            onDownload = {
                                val intent =
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        Uri.parse(resource.fileUrl)
                                    )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---------- Resource Card ----------

@Composable
private fun ResourceCard(
    resource: Resource,
    onDownload: () -> Unit,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    resource.title,
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Text(
                    resource.fileName,
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Text(
                    "Uploaded by ${resource.uploadedByName}",
                    style =
                        MaterialTheme.typography.labelSmall
                )
            }

            Column {
                IconButton(onClick = onDownload) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download"
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete resource"
                        )
                    }
                }
            }
        }
    }
}

// ---------- Upload Resource ----------

@Composable
fun UploadResourceScreen(
    moduleCode: String,
    onUploaded: () -> Unit,
    viewModel: ResourceViewModel = viewModel()
) {
    var title by remember {
        mutableStateOf("")
    }

    var selectedUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedFileName by remember {
        mutableStateOf("")
    }

    var selectedFileSize by remember {
        mutableStateOf(0L)
    }

    val context = LocalContext.current

    val status by viewModel.uploadStatus.collectAsState()

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                selectedUri = uri

                context.contentResolver
                    .query(uri, null, null, null, null)
                    ?.use { cursor ->
                        val nameIndex =
                            cursor.getColumnIndex(
                                android.provider.OpenableColumns.DISPLAY_NAME
                            )

                        val sizeIndex =
                            cursor.getColumnIndex(
                                android.provider.OpenableColumns.SIZE
                            )

                        if (cursor.moveToFirst()) {
                            selectedFileName =
                                if (nameIndex >= 0) {
                                    cursor.getString(nameIndex)
                                } else {
                                    "file"
                                }

                            selectedFileSize =
                                if (sizeIndex >= 0) {
                                    cursor.getLong(sizeIndex)
                                } else {
                                    0L
                                }
                        }
                    }
            }
        }

    LaunchedEffect(status) {
        if (status is UiStatus.Success) {
            onUploaded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Upload resource to $moduleCode",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            label = {
                Text(
                    "Resource title " +
                            "(e.g. \"Topic 3 Notes\")"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        ) {
            Text(
                if (selectedFileName.isEmpty()) {
                    "Choose file"
                } else {
                    selectedFileName
                }
            )
        }

        if (status is UiStatus.Loading) {
            CircularProgressIndicator()
        }

        if (status is UiStatus.Error) {
            Text(
                (status as UiStatus.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            enabled =
                selectedUri != null &&
                        title.isNotBlank() &&
                        status !is UiStatus.Loading,
            onClick = {
                val user =
                    FirebaseAuth.getInstance().currentUser
                        ?: return@Button

                viewModel.uploadResource(
                    contentResolver =
                        context.contentResolver,
                    fileUri =
                        selectedUri!!,
                    fileName =
                        selectedFileName,
                    fileSizeBytes =
                        selectedFileSize,
                    moduleCode =
                        moduleCode,
                    title =
                        title,
                    uploaderUid =
                        user.uid,
                    uploaderName =
                        user.displayName ?: "Student"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload")
        }
    }
}
