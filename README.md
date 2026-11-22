# SmartUmbrella

An Android smart umbrella cabinet system with local/offline operation and cloud synchronization.

## Planned modules

- Android device application: Jetpack Compose, Room, hardware abstraction, 6-slot borrow/return flow
- Fingerprint interface: vendor SDK adapter
- Umbrella slot interface: RS-485/control board adapter, electric lock and position sensor
- RFID interface: umbrella identification and exact return association
- Cloud API: synchronization and remote administration
- Admin web backend: personnel, devices, slots and borrowing records

## Initial hardware assumptions

- 6 umbrella positions
- Android 11+ panel or industrial all-in-one device
- 485-controlled slot board with electric locks and position sensors
- USB/serial fingerprint module with an Android SDK
- RFID reader and one tag per umbrella
- Independent power supply for locks and controller

## Development status

Initial repository only. Implementation is being developed in stages.
