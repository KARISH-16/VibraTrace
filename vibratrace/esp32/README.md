# VibraTrace ESP32 Embedded Node

## Hardware Architecture
- **Microcontroller**: ESP32-WROOM-32 (Dual Core 240MHz, 4MB Flash)
- **Temperature & Humidity**: AHT20 I2C Sensor (Address `0x38`)
- **Ethylene Gas**: Electrochemical Sensor / Analog conditioning circuit connected to GPIO34
- **Ammonia (NH3) Gas**: MQ-137 / Low-power semiconductor sensor connected to GPIO35
- **Container Tamper**: Magnetic Reed Switch on container lid connected to GPIO4 with edge interrupt
- **Vibration Energy Harvesting**: PZT Piezoelectric transducer with full-bridge rectifier, capacitor buffer, and ADC sensing on GPIO36
- **Offline Storage**: MicroSD Card SPI interface (`CS=GPIO5`, `MOSI=GPIO23`, `MISO=GPIO19`, `SCK=GPIO18`)
- **Cellular & MQTT**: SIM800C GSM/GPRS module via UART2 (`RX2=GPIO16`, `TX2=GPIO17`)

## Pinout Map
| Component | ESP32 GPIO | Protocol | Description |
|-----------|------------|----------|-------------|
| AHT20 SDA | GPIO 21 | I2C | Temp/Humidity data |
| AHT20 SCL | GPIO 22 | I2C | Clock |
| Ethylene Sensor | GPIO 34 | ADC1_CH6 | 0 - 2.0 ppm analog output |
| NH3 Sensor | GPIO 35 | ADC1_CH7 | 0 - 10.0 ppm analog output |
| Reed Switch | GPIO 4 | Interrupt | Lid open tamper detection |
| Piezo Harvester | GPIO 36 | ADC1_CH0 | Vibration detection & charge trigger |
| Battery Sense | GPIO 39 | ADC1_CH3 | Lithium battery voltage divider |
| MicroSD CS | GPIO 5 | SPI | Chip Select |
| SIM800C TX | GPIO 16 | UART | AT commands & GPRS data |
| SIM800C RX | GPIO 17 | UART | AT commands response |

## Build Instructions (PlatformIO / Arduino IDE)
1. Install PlatformIO extension in VSCode or install Arduino ESP32 core.
2. Select board `esp32dev`.
3. Set upload speed to `921600` and baud rate to `115200`.
4. Run `pio run -t upload`.
