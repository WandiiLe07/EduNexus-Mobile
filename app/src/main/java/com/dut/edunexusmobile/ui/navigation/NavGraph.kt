@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dut.edunexusmobile.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dut.edunexusmobile.data.model.DiscussionPost
import com.dut.edunexusmobile.ui.screens.*
import com.dut.edunexusmobile.ui.theme.LaserLemon
import com.google.firebase.auth.FirebaseAuth

// ============================================================
// ROUTES
// ============================================================

private object Routes {

    const val LOGIN = "login"
    const val HOME = "home"

    const val RESOURCES = "resources/{moduleCode}"
    const val UPLOAD = "upload/{moduleCode}"
    const val DISCUSSIONS = "discussions/{moduleCode}"

    const val THREAD =
        "thread/{moduleCode}/{postId}/{postTitle}/{postBody}/{authorName}"

    fun resources(moduleCode: String) =
        "resources/$moduleCode"

    fun upload(moduleCode: String) =
        "upload/$moduleCode"

    fun discussions(moduleCode: String) =
        "discussions/$moduleCode"
}

// ============================================================
// MODULE MODEL
// ============================================================

private data class Module(
    val code: String,
    val name: String,
    val year: String,
    val semester: String
)

// ============================================================
// MODULES
// ============================================================

private val modules = listOf(

    // --------------------------------------------------------
    // FIRST YEAR - SEMESTER ONE
    // --------------------------------------------------------

    Module(
        "AADC101",
        "Analog and Digital Circuits",
        "First Year",
        "Semester One"
    ),

    Module(
        "ENMA101",
        "Engineering Mathematics 1A",
        "First Year",
        "Semester One"
    ),

    Module(
        "ICLK101",
        "Information and Communication Literacy and Skills",
        "First Year",
        "Semester One"
    ),

    Module(
        "INPR101",
        "Introduction to Programming",
        "First Year",
        "Semester One"
    ),

    Module(
        "ITOS101",
        "Introduction to Operating Systems",
        "First Year",
        "Semester One"
    ),

    Module(
        "POCC101",
        "Principles of Computer Composition",
        "First Year",
        "Semester One"
    ),

    // --------------------------------------------------------
    // FIRST YEAR - SEMESTER TWO
    // --------------------------------------------------------

    Module(
        "BAAI102",
        "Basic Application of Artificial Intelligence",
        "First Year",
        "Semester Two"
    ),

    Module(
        "BSFN102",
        "Business Fundamentals 1",
        "First Year",
        "Semester Two"
    ),

    Module(
        "CSTN101",
        "Cornerstone 101",
        "First Year",
        "Semester Two"
    ),

    Module(
        "ENMA102",
        "Engineering Mathematics 1B",
        "First Year",
        "Semester Two"
    ),

    Module(
        "SPAT102",
        "Sensor Principles and Technology",
        "First Year",
        "Semester Two"
    ),

    // --------------------------------------------------------
    // SECOND YEAR - SEMESTER ONE
    // --------------------------------------------------------

    Module(
        "BSFN211",
        "Business Fundamentals",
        "Second Year",
        "Semester One"
    ),

    Module(
        "DSST201",
        "Discrete Structures",
        "Second Year",
        "Semester One"
    ),

    Module(
        "EMCC201",
        "Embedded Micro-Controller Technology",
        "Second Year",
        "Semester One"
    ),

    Module(
        "ITIT201",
        "IoT Identification Technology",
        "Second Year",
        "Semester One"
    ),

    Module(
        "PRPD201",
        "Programming Paradigms",
        "Second Year",
        "Semester One"
    ),

    // --------------------------------------------------------
    // SECOND YEAR - SEMESTER TWO
    // --------------------------------------------------------

    Module(
        "DTMG202",
        "Data Management",
        "Second Year",
        "Semester Two"
    ),

    Module(
        "IWNT202",
        "Introduction to Wireless Networking Technology",
        "Second Year",
        "Semester Two"
    ),

    Module(
        "LWLF101",
        "Law for Life",
        "Second Year",
        "Semester Two"
    ),

    Module(
        "MOS202",
        "Mobile Operating System Technology and Application",
        "Second Year",
        "Semester Two"
    ),

    Module(
        "NBIT202",
        "Narrow Band – IoT",
        "Second Year",
        "Semester Two"
    ),

    Module(
        "WBDV202",
        "Web Development",
        "Second Year",
        "Semester Two"
    ),

    // --------------------------------------------------------
    // THIRD YEAR - SEMESTER ONE
    // --------------------------------------------------------

    Module(
        "BSPE301",
        "Business Process Engineering",
        "Third Year",
        "Semester One"
    ),

    Module(
        "CLCM301",
        "Cloud Computing",
        "Third Year",
        "Semester One"
    ),

    Module(
        "ITDA301",
        "IoT Data Analysis",
        "Third Year",
        "Semester One"
    ),

    Module(
        "ITPI301",
        "IoT Project Planning and Implementation",
        "Third Year",
        "Semester One"
    ),

    Module(
        "MBDV301",
        "Mobile Development",
        "Third Year",
        "Semester One"
    ),

    Module(
        "ITRS301",
        "Research Skills",
        "Third Year",
        "Semester One"
    ),

    // --------------------------------------------------------
    // THIRD YEAR - SEMESTER TWO
    // --------------------------------------------------------

    Module(
        "ENSP101",
        "Entrepreneurial Spirit",
        "Third Year",
        "Semester Two"
    ),

    Module(
        "IOTP302",
        "IoT Project",
        "Third Year",
        "Semester Two"
    ),

    Module(
        "IOTS302",
        "IoT Security",
        "Third Year",
        "Semester Two"
    )
)

// ============================================================
// HOME SCREEN
// ============================================================

@Composable
private fun HomeScreen(
    onOpenResources: (String) -> Unit,
    onOpenDiscussions: (String) -> Unit,
    onLogout: () -> Unit
) {

    var selectedModule by remember {
        mutableStateOf<Module?>(null)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val firebaseUser =
        FirebaseAuth.getInstance().currentUser

    val firstName =
        firebaseUser
            ?.displayName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Student"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.background
                ),

                title = {

                    Column {

                        Text(
                            text = "EduNexus",
                            style =
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Your learning, connected.",
                            style =
                                MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                actions = {

                    // User initial

                    Surface(
                        shape = CircleShape,
                        color =
                            MaterialTheme.colorScheme
                                .primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text =
                                    firstName
                                        .firstOrNull()
                                        ?.uppercase()
                                        ?: "S",

                                fontWeight = FontWeight.Bold,

                                color =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Logout

                    IconButton(
                        onClick = onLogout
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Logout,

                            contentDescription =
                                "Log out",

                            tint =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            // =================================================
            // GREETING
            // =================================================

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text =
                        "Welcome back, $firstName",

                    style =
                        MaterialTheme.typography
                            .headlineSmall,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "What are you studying today?",

                    style =
                        MaterialTheme.typography.bodyLarge,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            // =================================================
            // MODULE SELECTOR
            // =================================================

            ExposedDropdownMenuBox(
                expanded = expanded,

                onExpandedChange = {
                    expanded = !expanded
                }
            ) {

                OutlinedTextField(
                    value =
                        selectedModule?.let {
                            "${it.code} — ${it.name}"
                        } ?: "",

                    onValueChange = {},

                    readOnly = true,

                    singleLine = true,

                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.School,

                            contentDescription = null,

                            tint =
                                MaterialTheme.colorScheme.primary
                        )
                    },

                    label = {
                        Text("Module")
                    },

                    placeholder = {
                        Text("Select your module")
                    },

                    trailingIcon = {

                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
                                expanded = expanded
                            )
                    },

                    shape =
                        RoundedCornerShape(14.dp),

                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,

                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    modules
                        .groupBy {
                            "${it.year} • ${it.semester}"
                        }
                        .forEach {
                                (groupName, groupModules) ->

                            DropdownMenuItem(
                                text = {

                                    Text(
                                        text = groupName,

                                        style =
                                            MaterialTheme.typography
                                                .labelLarge,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            MaterialTheme.colorScheme
                                                .primary
                                    )
                                },

                                onClick = {},

                                enabled = false
                            )

                            groupModules.forEach { module ->

                                DropdownMenuItem(
                                    text = {

                                        Column {

                                            Text(
                                                text =
                                                    module.code,

                                                fontWeight =
                                                    FontWeight
                                                        .SemiBold
                                            )

                                            Text(
                                                text =
                                                    module.name,

                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodySmall,

                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .onSurfaceVariant
                                            )
                                        }
                                    },

                                    onClick = {

                                        selectedModule =
                                            module

                                        expanded =
                                            false
                                    }
                                )
                            }
                        }
                }
            }

            // =================================================
            // SELECTED MODULE CARD
            // =================================================

            selectedModule?.let { module ->

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(18.dp),

                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {

                    Row(
                        modifier =
                            Modifier.padding(16.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            shape =
                                RoundedCornerShape(12.dp),

                            color =
                                MaterialTheme.colorScheme
                                    .primary
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.School,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme
                                        .onPrimary,

                                modifier =
                                    Modifier.padding(12.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    module.code,

                                style =
                                    MaterialTheme.typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer
                            )

                            Text(
                                text =
                                    module.name,

                                style =
                                    MaterialTheme.typography
                                        .bodyMedium,

                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "${module.year} • ${module.semester}",

                                style =
                                    MaterialTheme.typography
                                        .labelMedium,

                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer
                                        .copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // =================================================
            // QUICK ACCESS HEADER
            // =================================================

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "Quick access",

                    style =
                        MaterialTheme.typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (selectedModule == null) {
                            "Select a module to continue."
                        } else {
                            "Continue with ${selectedModule!!.code}."
                        },

                    style =
                        MaterialTheme.typography
                            .bodyMedium,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            // =================================================
            // RESOURCE CARD
            // =================================================

            HomeActionCard(
                title =
                    "Resource Sharing",

                description =
                    "Access notes, documents and learning materials.",

                enabled =
                    selectedModule != null,

                icon = {

                    Icon(
                        imageVector =
                            Icons.Default.Storage,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme.colorScheme
                                .primary
                    )
                },

                onClick = {

                    selectedModule?.let {
                        onOpenResources(it.code)
                    }
                }
            )

            // =================================================
            // DISCUSSION CARD
            // =================================================

            HomeActionCard(
                title =
                    "Module Discussions",

                description =
                    "Ask questions and collaborate with classmates.",

                enabled =
                    selectedModule != null,

                accent =
                    LaserLemon,

                icon = {

                    Icon(
                        imageVector =
                            Icons.Default.Forum,

                        contentDescription =
                            null,

                        tint =
                            if (selectedModule != null) {
                                Color(0xFF354500)
                            } else {
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            }
                    )
                },

                onClick = {

                    selectedModule?.let {
                        onOpenDiscussions(it.code)
                    }
                }
            )

            // =================================================
            // FOOTER
            // =================================================

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                color =
                    MaterialTheme.colorScheme
                        .surfaceVariant
                        .copy(alpha = 0.55f)
            ) {

                Row(
                    modifier =
                        Modifier.padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Description,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Text(
                        text =
                            "Everything for your modules, in one place.",

                        style =
                            MaterialTheme.typography
                                .bodyMedium,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }
    }
}

// ============================================================
// HOME ACTION CARD
// ============================================================

@Composable
private fun HomeActionCard(
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    accent: Color? = null
) {

    val containerColor =
        if (accent != null && enabled) {
            accent.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surface
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    containerColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (enabled) 2.dp else 0.dp
            )
    ) {

        Row(
            modifier =
                Modifier.padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier =
                    Modifier.size(48.dp),

                shape =
                    RoundedCornerShape(14.dp),

                color =
                    if (enabled) {
                        MaterialTheme.colorScheme
                            .primaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .surfaceVariant
                    }
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {
                    icon()
                }
            }

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        title,

                    style =
                        MaterialTheme.typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme
                                .onSurface
                        } else {
                            MaterialTheme.colorScheme
                                .onSurface
                                .copy(alpha = 0.45f)
                        }
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        description,

                    style =
                        MaterialTheme.typography
                            .bodySmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                            .copy(
                                alpha =
                                    if (enabled) {
                                        1f
                                    } else {
                                        0.5f
                                    }
                            )
                )
            }

            Icon(
                imageVector =
                    Icons.Default.ArrowForward,

                contentDescription =
                    null,

                tint =
                    if (enabled) {
                        MaterialTheme.colorScheme
                            .primary
                    } else {
                        MaterialTheme.colorScheme
                            .onSurface
                            .copy(alpha = 0.25f)
                    }
            )
        }
    }
}

// ============================================================
// NAVIGATION
// ============================================================

@Composable
fun EduNexusNavGraph() {

    val navController: NavHostController =
        rememberNavController()

    val startDestination =
        if (
            FirebaseAuth
                .getInstance()
                .currentUser != null
        ) {
            Routes.HOME
        } else {
            Routes.LOGIN
        }

    NavHost(
        navController =
            navController,

        startDestination =
            startDestination
    ) {

        // ====================================================
        // LOGIN
        // ====================================================

        composable(
            Routes.LOGIN
        ) {

            LoginScreen(

                onLoggedIn = {

                    navController.navigate(
                        Routes.HOME
                    ) {

                        popUpTo(
                            Routes.LOGIN
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ====================================================
        // HOME
        // ====================================================

        composable(
            Routes.HOME
        ) {

            HomeScreen(

                onOpenResources = {
                        moduleCode ->

                    navController.navigate(
                        Routes.resources(
                            moduleCode
                        )
                    )
                },

                onOpenDiscussions = {
                        moduleCode ->

                    navController.navigate(
                        Routes.discussions(
                            moduleCode
                        )
                    )
                },

                onLogout = {

                    FirebaseAuth
                        .getInstance()
                        .signOut()

                    navController.navigate(
                        Routes.LOGIN
                    ) {

                        popUpTo(
                            Routes.HOME
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ====================================================
        // RESOURCES
        // ====================================================

        composable(
            Routes.RESOURCES,

            arguments =
                listOf(

                    navArgument(
                        "moduleCode"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { backStackEntry ->

            val moduleCode =
                backStackEntry
                    .arguments
                    ?.getString(
                        "moduleCode"
                    )
                    ?: ""

            ResourceListScreen(
                moduleCode =
                    moduleCode,

                onUploadClick = {

                    navController.navigate(
                        Routes.upload(
                            moduleCode
                        )
                    )
                }
            )
        }

        // ====================================================
        // UPLOAD
        // ====================================================

        composable(
            Routes.UPLOAD,

            arguments =
                listOf(

                    navArgument(
                        "moduleCode"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { backStackEntry ->

            val moduleCode =
                backStackEntry
                    .arguments
                    ?.getString(
                        "moduleCode"
                    )
                    ?: ""

            UploadResourceScreen(
                moduleCode =
                    moduleCode,

                onUploaded = {
                    navController
                        .popBackStack()
                }
            )
        }

        // ====================================================
        // DISCUSSIONS
        // ====================================================

        composable(
            Routes.DISCUSSIONS,

            arguments =
                listOf(

                    navArgument(
                        "moduleCode"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { backStackEntry ->

            val moduleCode =
                backStackEntry
                    .arguments
                    ?.getString(
                        "moduleCode"
                    )
                    ?: ""

            ModuleDiscussionsScreen(
                moduleCode =
                    moduleCode,

                onPostClick = { post ->

                    val encodedBody =
                        java.net.URLEncoder
                            .encode(
                                post.body,
                                "UTF-8"
                            )

                    val encodedTitle =
                        java.net.URLEncoder
                            .encode(
                                post.title,
                                "UTF-8"
                            )

                    val encodedAuthor =
                        java.net.URLEncoder
                            .encode(
                                post.authorName,
                                "UTF-8"
                            )

                    navController.navigate(
                        "thread/" +
                                "$moduleCode/" +
                                "${post.id}/" +
                                "$encodedTitle/" +
                                "$encodedBody/" +
                                encodedAuthor
                    )
                }
            )
        }

        // ====================================================
        // DISCUSSION THREAD
        // ====================================================

        composable(
            Routes.THREAD,

            arguments =
                listOf(

                    navArgument(
                        "moduleCode"
                    ) {
                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "postId"
                    ) {
                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "postTitle"
                    ) {
                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "postBody"
                    ) {
                        type =
                            NavType.StringType
                    },

                    navArgument(
                        "authorName"
                    ) {
                        type =
                            NavType.StringType
                    }
                )
        ) { backStackEntry ->

            val args =
                backStackEntry.arguments!!

            val post =
                DiscussionPost(

                    id =
                        args.getString(
                            "postId"
                        ) ?: "",

                    moduleCode =
                        args.getString(
                            "moduleCode"
                        ) ?: "",

                    title =
                        java.net.URLDecoder
                            .decode(
                                args.getString(
                                    "postTitle"
                                ) ?: "",
                                "UTF-8"
                            ),

                    body =
                        java.net.URLDecoder
                            .decode(
                                args.getString(
                                    "postBody"
                                ) ?: "",
                                "UTF-8"
                            ),

                    authorName =
                        java.net.URLDecoder
                            .decode(
                                args.getString(
                                    "authorName"
                                ) ?: "",
                                "UTF-8"
                            )
                )

            DiscussionThreadScreen(
                post = post
            )
        }
    }
}