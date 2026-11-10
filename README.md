# Sohna Thadlu Admin — Native Android App (Kotlin + Jetpack Compose)

This is the native Android Admin Command Center for **Aaqib Hussain's Security Portfolio**. Every change you make in this Android app communicates directly with your website's live REST APIs (`/api/admin/*`) and **Firebase Cloud Firestore**, updating your live website worldwide in real time.

## Features Included
- **Server-Side Operator Login**: Authenticates with `/api/admin/login` using Bearer token session management.
- **All 9 Admin Modules**:
  1. **Hero & Telemetry Metrics** (Read-only cards + Edit Copy & Add/Edit Metric popup dialogs)
  2. **About, Biography & Focus Areas** (Read-only cards + Add/Edit popup dialogs)
  3. **Career Journey Timeline** (Reorderable milestones + Add/Edit popup dialogs)
  4. **Skills & Technical Arsenal** (Categories & skills + Add/Edit popup dialogs)
  5. **Verified Certifications** (Add/Edit certifications + upload certificate image from Android gallery)
  6. **Bug Bounty Platforms & Logos** (Add/Edit platforms + upload custom platform logo from Android gallery)
  7. **Social Media Channels** (Add/Edit social links)
  8. **Write-up Studio & Attachment Vault (`{I24239}`)** (Create/Edit write-ups in popup modal, upload PoC images/videos from phone storage to server, insert `{I24239}` tags into Markdown)
  9. **Encrypted Dispatch Inbox & Account Security** (Read/manage contact form messages, update operator credentials, or reset baseline data)

## How to Build the Installable `.apk`

### Method A: Build in Android Studio (Local)
1. Extract `SohnaThadlu-Android-Admin-App.zip`.
2. Open **Android Studio** -> **Open** -> select the `android-admin-app` folder.
3. Let Gradle sync finish, then click **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
4. Install `app/build/outputs/apk/debug/app-debug.apk` on your Android phone.

### Method B: Build `.apk` Automatically on GitHub (No Android Studio Needed)
1. Create a new private repository on GitHub and upload the contents of this folder (including `.github/workflows/build-apk.yml`).
2. Go to the **Actions** tab in your GitHub repository.
3. Click the **Build Sohna Thadlu Admin APK** workflow run and download **`SohnaThadluAdmin-APK`** from the **Artifacts** section.
