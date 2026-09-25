# MotorBoy Tech - Motorcycle Diagnostic & OBD2 BLE Scanner 🏍️⚡

**MotorBoy Tech** เป็นแอปพลิเคชัน Android สำหรับช่างซ่อมรถจักรยานยนต์และสาย DIY ปรับแต่งรถ มอบระบบวินิจฉัยข้อมูลกล่อง ECU มอเตอร์ไซค์แบบ Real-time ผ่านการเชื่อมต่อ **Bluetooth Low Energy (BLE)** ร่วมกับกล่องสแกน OBD2 / K-Line อ่านและลบโค้ดแจ้งเตือนความผิดปกติ (DTC Fault Codes) พร้อมชุดคู่มือและเครื่องมือประจำช่างครบวงจร

---

## 🌟 ฟีเจอร์หลัก (Key Features)

- 📡 **BLE OBD2 / K-Line Live Telemetry**:
  - รองรับการเชื่อมต่อแบบ GATT ผ่านอุปกรณ์ BLE เช่น ELM327, HM-10, Vgate, K-Line Adapters
  - ดึงข้อมูลสตรีมสด (250ms interval loop): รอบเครื่องยนต์ (RPM), ความเร็ว (Speed), องศาลิ้นเร่ง (TPS), อุณหภูมิน้ำยาชะลอความร้อน (Coolant Temp), Engine Load, และ แรงดัน O2 Sensor
- ⚠️ **DTC Fault Codes Scanner & Clearer**:
  - อ่านโค้ดข้อผิดพลาด ECU คำนวณรหัสปัญหาภาษาไทย พร้อมข้อแนะนำการแก้ไขเฉพาะรุ่น (เช่น Honda Wave, Yamaha Grand Filano)
  - ระบบสั่งลบไฟเตือนเครื่องยนต์ (Clear MIL / Check Engine)
- 📊 **Live Waveform Oscilloscope**:
  - กราฟคลื่นสัญญาณไฟฟ้าและเซนเซอร์แบบเรียลไทม์ ตรวจสอบความผิดปกติของลิ้นเร่งและรอบเครื่อง
- 🧰 **Repair Guides & Spec Database**:
  - ระบบฐานข้อมูลการซ่อม ค่าแรงขันปอนด์น็อต (Torque Spec) ลำดับขั้นตอนวินิจฉัยปัญหาเครื่องยนต์ไม่ติด/รถสะดุด
  - บันทึกประวัติและจัดการกล่องเครื่องมือช่างในตัว
- 🎨 **Paint Maintenance & Mixing (คู่มืองานสี, ซ่อมผิว & ผสมสี 2K ระดับมืออาชีพ)**:
  - **เทคนิคการเทียบสี (Color Matching Techniques)**: 5 ขั้นตอนค้นหารหัสสีโรงงาน (OEM Code), วิเคราะห์มิติสี (Hue/Value/Chroma), การทำแผ่นพ่นทดสอบ (Spray-Out Cards), ตารางสูตรปรับจูนแม่สี (Micro-Tinting Matrix) และการตรวจสอบแสงป้องกันภาวะเมทาเมอริซึม (Metamerism Check / High-CRI 96+)
  - **ขั้นตอนการซ่อมผิวระดับมืออาชีพ (Professional Surface Repair Steps)**: 6 ขั้นตอนมาตรฐานช่างศูนย์ ตั้งแต่ล้างไขมัน (Two-Cloth Degreasing), ขัดสโลปขนนก (Featheredging), โป๊ว 2K ปรับระนาบด้วยไกด์โค้ท, พ่นรองพื้น 2K High-Build, พ่นสีจริงเรียงเม็ดมุก จนถึงเคลียร์ 2K & ขัดเงาระดับกระจก
  - **ความปลอดภัยในการสัมผัสสารเคมี (Safety Precautions for Handling Chemicals)**: มาตรการป้องกันอันตรายจากสาร Isocyanate ในฮาร์ดเดนเนอร์ 2K, ไอระเหยตัวทำละลายอินทรีย์ (VOCs), ฝุ่นขัดสี พร้อมเมทริกซ์อุปกรณ์ PPE (หน้ากาก A2P3, ถุงมือไนไตรล์) และการปฐมพยาบาล
  - **อภิธานศัพท์เครื่องมือทำสีเฉพาะทาง (Glossary of Specialized Paint Tools)**: แคตตาล็อกและสเปคเครื่องมือทำสีครบครัน เช่น กาพ่นสี HVLP/LVLP, ขนาดหัวฉีด (Nozzle Size Guide), ระบบกรองดักน้ำ 3 สเตจ, เกจวัดแรงดันลม, บล็อกขัดระนาบ, ผงไกด์โค้ท, ถ้วยวัดความหนืด Ford Cup #4, ไฟฉาย Sun Gun CRI 96+ และผ้าเหนียว Tack Cloth
  - **เครื่องคำนวณสัดส่วนสี 2K (Interactive 2K Mixing Calculator)**: คำนวณสูตร 2:1, 4:1, 4:1:1 พร้อมตัวเลื่อน % ทินเนอร์แบบไดนามิก
- 🤖 **Automated GitHub Actions CI/CD (Auto Build APK)**:
  - ระบบสร้างไฟล์แอปพลิเคชัน `.apk` อัตโนมัติทันทีที่มีการ `git push` หรือ `pull request`
  - รองรับการสร้าง GitHub Release พร้อมแนบไฟล์ APK อัตโนมัติเมื่อ Push Git Tag เช่น `v1.0.0`
  - ดาวน์โหลดไฟล์ APK พร้อมใช้งานได้ทันทีจากแท็บ **Artifacts** บน GitHub Actions

---

## 🏗️ โครงสร้างโปรเจกต์ (Project Architecture)

```
motorboy-tech/
├── .github/
│   └── workflows/
│       └── android.yml            # CI/CD Script สำหรับ Build APK อัตโนมัติ
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt                # หน้าหลักของแอปพลิเคชัน (Compose Scaffold)
│   │   ├── ObdBleManager.kt               # ระบบจัดการสแกนและเชื่อมต่อ Bluetooth BLE GATT
│   │   ├── OBD2AndDtcScannerComponent.kt   # หน้าจอแสดงผล OBD2 สตรีมสด และ DTC Scanner
│   │   ├── FastTroubleShooterComponent.kt # ระบบวิเคราะห์ปัญหาด่วนสำหรับช่าง
│   │   ├── RepairGuideComponent.kt        # คู่มือการซ่อมและค่าขันปอนด์น็อต
│   │   ├── RepairToolOrganizerComponent.kt# ระบบจัดการและเช็คสต็อกเครื่องมือ
│   │   └── db/                            # Room Database, DAOs และ ViewModels
│   └── build.gradle.kts                   # การกำหนดค่า Android App Gradle
├── build.gradle.kts                       # Root Build Gradle
├── settings.gradle.kts                    # Root Settings Gradle
└── README.md                              # เอกสารโปรเจกต์
```

---

## 🚀 วิธีการคอมไพล์และรันแอปพลิเคชัน (How to Build)

### ข้อกำหนดเบื้องต้น (Prerequisites)
- **JDK**: Java 17 ขึ้นไป
- **Android SDK**: API Level 34 (Android 14)
- **Gradle**: 8.x

### คำสั่งคอมไพล์ Build APK

```bash
# Build Debug APK
gradle assembleDebug

# ไฟล์ APK จะถูกสร้างขึ้นที่
app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ ระบบ CI/CD สร้างไฟล์ APK ออนไลน์ผ่าน GitHub (GitHub Actions)

ระบบได้ติดตั้ง **CI/CD Pipeline ตามมาตรฐานระดับสากล** สำหรับ Build ไฟล์ APK ผ่านคลาวด์ของ GitHub โดยอัตโนมัติ โดยที่คุณ**ไม่ต้องติดตั้งโปรแกรม Android Studio บนคอมพิวเตอร์ของคุณเลย**:

### 🌐 วิธีที่ 1: สั่ง Build APK ออนไลน์ผ่านหน้าเว็บ GitHub (Manual Dispatch)
1. เปิดหน้าเว็บ GitHub Repository ของคุณ (เช่น `https://github.com/sirisakboy/motorboy-tech`)
2. คลิกที่แท็บ **"Actions"** บนแถบเมนูด้านบน
3. ที่แถบซ้ายมือ เลือกเวิร์กโฟลว์ **"Android CI/CD - Auto Build & Release APK"**
4. คลิกปุ่ม **"Run workflow"** (สีฟ้าทางขวา)
5. เลือกประเภทการ Build (`debug`, `release`, หรือ `all`) แล้วกดปุ่มสีเขียว **"Run workflow"**
6. รอระบบคลาวด์คอมไพล์ประมาณ 2-3 นาที เมื่อขึ้นเครื่องหมายถูกสีเขียว (✅) ให้คลิกเข้าไปที่รอบการทำงานนั้น
7. เลื่อนลงมาล่างสุดที่หัวข้อ **Artifacts** แล้วคลิกดาวน์โหลดไฟล์ `MotorBoy-Android-APK-*` นำไปติดตั้งลงมือถือ Android ได้ทันที

### ⚡ วิธีที่ 2: Build อัตโนมัติทุกครั้งที่บันทึกโค้ด (Auto Push Trigger)
- เมื่อคุณทำการ `git push` โค้ดขึ้นบน branch ใดๆ ระบบ GitHub Actions จะตรวจจับและทำการ Build APK ให้คุณทันทีโดยอัตโนมัติ

### 🏷️ วิธีที่ 3: สร้างเวอร์ชันปล่อยจริง (Release APK) ด้วย Git Tag
- เมื่อคุณต้องการออกเวอร์ชันใหม่ เพียงสร้างและ push tag เช่น `v1.0.0`:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
- GitHub Actions จะสร้าง **GitHub Release** หน้าดาวน์โหลดพร้อมแนบไฟล์ APK ให้คนอื่นโหลดใช้งานได้ทันทีในหน้า **Releases** ของ GitHub

---

## 📄 License & Maintainer

พัฒนาและดูแลโดย **sirisakboy** (sirisakkhacha@gmail.com)  
โปรเจกต์ **MotorBoy Tech** มุ่งพัฒนาเพื่อยกระดับงานช่างซ่อมมอเตอร์ไซค์ยุคใหม่ด้วยเทคโนโลยี OBD2 & BLE
