# Android module

Open the `android` directory in Android Studio, use JDK 17, and run the `app` configuration on an Android device or emulator.

## Build and tests

The checked-in `gradlew` and `gradlew.bat` bootstrap Gradle 8.9 from the official Gradle distribution when it is not already cached. On Unix, run `chmod +x gradlew` once if the checkout did not preserve the executable bit, then run `./gradlew :app:assembleDebug` or `./gradlew :app:testDebugUnitTest`. The Room instrumented test can be run with `./gradlew :app:connectedDebugAndroidTest` on a connected device/emulator.

## Backend sync configuration

The WorkManager worker POSTs queued events as JSON to an HTTPS endpoint and acknowledges each queue row only after a 2xx response. Configure the endpoint with a Gradle property, for example `./gradlew -PSYNC_API_URL=https://api.example.com/v1/events :app:assembleDebug`. An optional `SYNC_API_TOKEN` property supplies a Bearer token for local development; do not put long-lived secrets in a mobile APK. Production authentication should use short-lived user/device credentials and the backend must honor the `Idempotency-Key` header.

Payload fields: `eventId`, `operation`, `slotNumber`, `hardwareTag`, and `occurredAt`. No backend URL is configured by default, so the worker will not upload until one is supplied.

## Generic Android-host hardware framework

`UmbrellaHardware` is the application boundary; `ControllerUmbrellaHardware` validates slot numbers and delegates to `UmbrellaController`. `AndroidUsbBulkTransport` is a generic USB-host transport for a device that exposes bulk IN/OUT endpoints. It requires the user/system to grant USB permission first and currently uses newline-terminated ASCII. `LineProtocolUmbrellaController` demonstrates a placeholder command protocol (`VERIFY_FINGERPRINT`, `UNLOCK n`, `READ_RFID n`); **this is not a vendor protocol** and must be changed to match the purchased controller's documented SDK/protocol before connecting real equipment.

The app intentionally remains wired to `SimulatedUmbrellaHardware`; physical unlocks are not enabled. Select the actual Android host, USB/RS-485 bridge, fingerprint module, RFID reader, and lock controller first, then implement the vendor-specific transport and permission lifecycle. Never power a cabinet lock from Android USB/GPIO: use a rated lock-control board with electrical protection and state feedback. Keep fingerprint templates on the authorized biometric device; expose only a verification result to the app.

## Hardware integration

The repository does not include vendor SDKs, device protocol documents, hardware credentials, or a backend URL. No real device integration is claimed until the specific models have been tested.
