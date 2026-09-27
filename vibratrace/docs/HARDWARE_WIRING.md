# VibraTrace Hardware Wiring & Schematics

## Components Required
1. **ESP32 DevKit v1 (30-pin or 38-pin)**
2. **AHT20 Sensor Module (I2C)**: VCC -> 3.3V, GND -> GND, SDA -> GPIO 21, SCL -> GPIO 22
3. **Electrochemical Ethylene Sensor / Op-Amp breakout**: VCC -> 5V, GND -> GND, Analog OUT -> GPIO 34 (ADC1_CH6)
4. **Ammonia NH3 Gas Sensor (MQ-137 / Low-power analog)**: VCC -> 5V, GND -> GND, Analog OUT -> GPIO 35 (ADC1_CH7)
5. **Magnetic Reed Switch**: Terminal 1 -> GPIO 4, Terminal 2 -> GND (Internal pull-up enabled)
6. **Piezoelectric Vibration Harvester**: PZT Ceramic wafer -> Schottky bridge rectifier -> 470uF low-leakage capacitor -> GPIO 36 (ADC1_CH0) & trickle charging circuit
7. **MicroSD Card Module (SPI)**: CS -> GPIO 5, MOSI -> GPIO 23, MISO -> GPIO 19, SCK -> GPIO 18
8. **SIM800C GSM/GPRS Module**: VCC -> 4.0V LiPo / buck converter, GND -> GND, TXD -> GPIO 16 (ESP32 RX2), RXD -> GPIO 17 (ESP32 TX2)
9. **Power System**: 3.7V 2500mAh 18650 Li-ion battery + TP4056 charge controller + DW01 protection IC.
