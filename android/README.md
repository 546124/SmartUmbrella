# Android module

## Run

Open the `android` directory in Android Studio, use JDK 17, and run the `app` configuration on an Android device or emulator.

## Included prototype

- Kotlin + Jetpack Compose, six fixed umbrella slots
- Room persistence for slot status and borrow/return events
- Transactional offline sync queue for future backend delivery
- Hardware interface for fingerprint verification, lock control, and RFID reading
- Deterministic simulator; no physical hardware or biometric data is accessed

The initial slot state is seeded locally on first launch. Borrow and return actions update the slot, event history, and pending sync queue in one database transaction. A production integration should replace `SimulatedUmbrellaHardware` with the approved device/vendor adapter and add an authenticated sync worker.
