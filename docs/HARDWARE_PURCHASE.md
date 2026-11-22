# Hardware Bill of Materials

## Required

| Component | Initial specification |
|---|---|
| Android host | 10–12 inch industrial panel, Android 11+, USB, RS-485 or USB-to-RS-485 |
| Fingerprint module | Android SDK, 1:N matching, secure template storage |
| 485 controller | 6-slot or daisy-chainable module; lock and sensor reads |
| Electric locks | 12 VDC, normally open/locked mode compatible with controller |
| Position sensors | Infrared or Hall sensor with anti-bounce support |
| RFID reader | USB or serial, EPC/UID reading |
| Umbrella tags | One durable RFID tag per umbrella |
| Status indicators | Red and green LEDs per slot or equivalent output |
| Power supply | Separate regulated lock supply plus stable host supply |
| Network | Ethernet preferred; Wi-Fi as fallback |

## 6-slot wiring model

- 6 lock outputs
- 6 presence sensor inputs
- 6 red indicators
- 6 green indicators
- 1 slot controller bus
- 1 fingerprint module
- 1 optional RFID reader
- 1 display panel

## Procurement note

Do not purchase the fingerprint and controller models until their Android SDK/API documentation is available.
