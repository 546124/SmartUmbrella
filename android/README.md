# Android module

Open the `android` directory in Android Studio, use JDK 17, and run the `app` configuration on an Android device or emulator.

## Current prototype

- Kotlin + Jetpack Compose, six fixed umbrella slots
- Room persistence for slot status and borrow/return events
- Transactional offline queue with a WorkManager periodic worker
- ViewModel-owned UI state and operation feedback
- Instrumented Room DAO persistence test (`./gradlew :app:connectedDebugAndroidTest` when a Gradle Wrapper is installed)
- Hardware and sync integrations remain simulated; no physical hardware or backend endpoint is configured

The worker currently acknowledges queued rows using a simulated uploader. Replace that block in `SyncWorker` with an authenticated API call before production use. Configure the Android SDK and JDK 17 in Android Studio to build and run the application.
