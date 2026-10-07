# EduNexus Mobile — Starter Project

A working MVVM + Jetpack Compose + Firebase scaffold covering the two scoped features:
**Resource Sharing** and **Module Discussions**.

## What's included

```
app/src/main/java/com/dut/edunexusmobile/
├── MainActivity.kt                    # Entry point, hosts the nav graph
├── data/model/Models.kt               # Resource, DiscussionPost, DiscussionReply
├── data/repository/
│   ├── AuthRepository.kt              # Firebase Authentication wrapper
│   ├── ResourceRepository.kt          # Firestore + Storage for resource sharing
│   └── DiscussionRepository.kt        # Firestore for posts + nested replies
├── ui/viewmodel/ViewModels.kt         # AuthViewModel, ResourceViewModel, DiscussionViewModel
├── ui/screens/
│   ├── LoginScreen.kt                 # Sign up / log in
│   ├── ResourceScreens.kt             # Resource list + upload
│   └── DiscussionScreens.kt           # Discussion list + thread + reply
├── ui/navigation/NavGraph.kt          # Home screen + all navigation routes
└── ui/theme/Theme.kt                  # Material 3 theme

firestore.rules      # Security rules for Firestore
storage.rules         # Security rules for Storage (25MB upload cap)
```

## Setup steps

1. **Open in Android Studio** — open the `EduNexusMobile` folder as an existing project (File → Open).

2. **Create a Firebase project** at https://console.firebase.google.com
   - Add an Android app with package name `com.dut.edunexusmobile`
   - Download the generated `google-services.json` and place it in `app/google-services.json` (this file is required to build — it is not included here since it's project-specific)

3. **Enable these Firebase products** in the console:
   - **Authentication** → Sign-in method → enable Email/Password
   - **Firestore Database** → Create database (start in test mode while developing, then apply `firestore.rules` before submission)
   - **Storage** → Get started, then apply `storage.rules`

4. **Sync Gradle** — Android Studio will prompt this automatically once `google-services.json` is in place.

5. **Run the app** on an emulator (API 26+) or physical device.

## How the two features map to files

| Feature | Repository | ViewModel | Screens |
|---|---|---|---|
| Resource Sharing | `ResourceRepository.kt` | `ResourceViewModel` | `ResourceScreens.kt` |
| Module Discussions | `DiscussionRepository.kt` | `DiscussionViewModel` | `DiscussionScreens.kt` |

Both features share `AuthRepository`/`AuthViewModel` and the same navigation shell (`NavGraph.kt`), matching the "shared login, independent features" split described in the project proposal — which also makes it straightforward for two people to work on each feature in parallel.

## Suggested next steps for your group

- **Resource Sharing dev:** wire up module filtering/search on `ResourceListScreen`, add file-type icons, consider a delete confirmation dialog
- **Module Discussions dev:** add edit/delete for own posts, consider pagination for large threads
- **UI/UX designer:** refine `Theme.kt` colors/typography, add empty-state illustrations, polish spacing
- **QA/Testing:** write unit tests for the ViewModels (mock the repositories), test the upload flow on a real device with a large file to confirm the 25MB cap works, test with wifi toggled off mid-upload
- **Team Lead:** confirm module code list matches actual DUT modules, test the full login → upload → discuss flow end-to-end
