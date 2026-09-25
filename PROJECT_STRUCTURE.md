# Project Structure - MotorBoy Tech (บอย อะไหล่ยนต์ - หนองตาไก้)

## Overview
MotorBoy Tech is an offline-first technical database and utility application designed specifically for motorcycle mechanics in Thailand. It operates entirely offline without requiring model connections, featuring repair troubleshooting guides, motorcycle specifications, calculation tools (CC, compression, gear ratio), mechanic secret tricks, and workshop price guides.

## File Tree & Components
- `metadata.json`: Platform metadata (App Name: MotorBoy Tech).
- `app/src/main/AndroidManifest.xml`: Android manifest configuring permissions and app entry point.
- `app/build.gradle.kts`: Gradle build configuration with Compose, Room, Coroutines, Material 3, and Serialization.
- `app/src/main/res/values/strings.xml`: String resources (`app_name` set to "MotorBoy Tech").
- `app/src/main/java/com/example/MainActivity.kt`: Main entry point containing the Jetpack Compose UI, offline technical database (`TECH_DATABASE`), search engine, calculators, troubleshooting wizard, spec viewer, secret tips, and workshop price guide.
- `PROJECT_STRUCTURE.md`: Detailed documentation and update history log (updated continuously).

## Changelog & Update History
- **v1.1.8 (Current Update)**:
  - Uncommented `firebase-firestore` dependency in `build.gradle.kts`.
  - Created `RoomToFirestoreSyncHelper.kt` to synchronize scan results and repair records directly to Firebase Firestore cloud collections (`motorboy_repair_records`).
- **v1.1.7**:
  - Created a dedicated Bluetooth Pairing Screen displaying OBD2 connectivity status, discovered devices list, signal strength indicator, and direct tap-to-connect workflow.
- **v1.1.6**:
  - Added **OBD2 Bluetooth Scanner & Actuator Tests** feature (เชื่อมต่อ ELM327 / OBD2, อ่าน/ลบโค้ด DTC ECU, สตรีมเซ็นเซอร์เรียลไทม์ RPM/ECT/TPS, และสั่งทดสอบแอคทูเอเตอร์ พัดลม/ปั๊มติ๊ก/หัวฉีด/คอยล์).
  - Added **Cloud Database Sync** feature (บันทึกและซิงค์ข้อมูลประวัติการซ่อมและสเปครถลูกค้าขึ้นสู่ระบบคลาวด์).
- **v1.1.5**:
  - Added visual feedback animations (scanning laser line and viewfinder reticle overlay) when auto camera scanning is active for DTC diagnostics.
- **v1.1.4**:
  - Added **Camera Scan LED DTC** feature on the home dashboard with both **Auto (AI Cam simulation)** and **Manual (Blink Counter)** modes, plus built-in flashlight toggle.
- **v1.1.3**:
  - Created a dedicated Material 3 Main Dashboard UI with clear interactive navigation cards for **'DTC Search'**, **'Specs Database'**, **'Calculators'**, and **'Repair Guides'**.
- **v1.1.2**:
  - Added comprehensive Engine Transmission & Gear Ratio Calculator (คำนวณอัตราทดเฟืองเกียร์และความเร็วตามรอบเครื่องยนต์ตามหลักการทำงานจริง):
    - Primary Reduction (เฟืองปฐมภูมิข้อเหวี่ยง/คลัตช์)
    - Gearbox Ratio (อัตราทดเกียร์ 1-4 พร้อมปุ่มลัดมาตรฐาน Wave/Dream)
    - Final Sprocket Drive (สเตอร์หน้า/หลัง)
    - Engine RPM & Tire Circumference
    - Overall Reduction Ratio & Estimated Speed (km/h) with mechanic character analysis.
- **v1.1.1**:
  - Fixed ClassNotFoundException for `MotorBoyApplication` by cleaning up Application class reference in `AndroidManifest.xml`.
  - Verified successful compilation.
- **v1.1.0**:
  - Complete fresh start: Removed all legacy cryptocurrency and trading signal app components as requested.
  - Implemented the full MotorBoy Offline Technical Database (บอย อะไหล่ยนต์ - หนองตาไก้).
  - Added bottom navigation with 5 specialized mechanic sections: Troubleshooting (แก้ปัญหา), Specifications (สเปครถ), Calculations (คำนวณช่าง), Secret Tricks (สูตรลับ), and Price Guide (เรทราคา).
  - Added instant offline search filtering across all repair symptoms, motorcycle models, and mechanical tricks.
  - Updated platform metadata and project name.
