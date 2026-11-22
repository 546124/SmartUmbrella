# Technical Architecture

## Android application

- Kotlin and Jetpack Compose
- Room for local durable storage
- Hilt for dependency injection
- WorkManager for cloud synchronization
- Ktor or Retrofit for REST communication
- Hardware interfaces isolated behind adapters

## Core entities

- User: ID, name, phone and fingerprint template reference
- Umbrella: RFID UID and inventory state
- Slot: index, lock state, presence state and light state
- Loan: borrower, umbrella, slot, borrowed/returned timestamps and sync state

## Loan flow

1. Detect person and capture fingerprint.
2. Match against the local fingerprint library.
3. Select one empty slot.
4. Open its lock and mark the umbrella as checked out.
5. Keep a local loan record and red-light state.

## Return flow

1. Place an umbrella into any empty slot.
2. Presence sensor detects the umbrella.
3. RFID reader identifies the umbrella.
4. Lock the slot.
5. Update loan as returned and remove the temporary local fingerprint binding.
6. Set red light off and green light on.

## Offline behavior

No cloud connection is required for borrowing or returning. Changes are queued locally and synchronized in a later batch.
