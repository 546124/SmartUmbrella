# Android module

Open the `android` directory in Android Studio, use JDK 17, and run the `app` configuration on an Android device or emulator.

## Build and tests

The checked-in `gradlew` and `gradlew.bat` bootstrap Gradle 8.9 from the official Gradle distribution when it is not already cached. On Unix, run `chmod +x gradlew` once if the checkout did not preserve the executable bit, then run `./gradlew :app:assembleDebug` or `./gradlew :app:testDebugUnitTest`. The Room instrumented test can be run with `./gradlew :app:connectedDebugAndroidTest` on a connected device/emulator.

## Backend sync configuration

The WorkManager worker POSTs queued events as JSON to an HTTPS endpoint and acknowledges each queue row only after a 2xx response. Configure the endpoint with a Gradle property, for example `./gradlew -PSYNC_API_URL=https://api.example.com/v1/events :app:assembleDebug`. An optional `SYNC_API_TOKEN` property supplies a Bearer token for local development; do not put long-lived secrets in a mobile APK. Production authentication should use short-lived user/device credentials and the backend must honor the `Idempotency-Key` header.

Payload fields: `eventId`, `operation`, `slotNumber`, `hardwareTag`, and `occurredAt`. No backend URL is configured by default, so the worker will not upload until one is supplied.

## Hardware integration

`ControllerUmbrellaHardware` is the production adapter boundary. Implement `UmbrellaController` using the actual approved board/vendor SDK or controller protocol, then inject that implementation in `SmartUmbrellaApplication`. The repository does not include a vendor SDK, device protocol, or hardware credentials; the app continues to use the safe local simulator until a concrete controller is available. Fingerprint templates must remain on the authorized biometric device and must not be sent to the backend.
