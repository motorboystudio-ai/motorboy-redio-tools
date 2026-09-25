package com.example.domain

data class ManualStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val imageUrl: String? = null,
    val cautionText: String? = null,
    val toolsRequired: List<String> = emptyList(),
    val torqueSpec: String? = null
)

data class Manual(
    val id: String,
    val title: String,
    val category: String = "ทั่วไป",
    val bikeModel: String = "มอเตอร์ไซค์ทั่วไป",
    val difficulty: String = "ปานกลาง",
    val estimatedTimeMinutes: Int = 30,
    val imageUrl: String,
    val description: String,
    val toolsNeeded: List<String> = emptyList(),
    val steps: List<ManualStep> = emptyList()
)

object ManualRepository {
    val sampleManuals: List<Manual> = listOf(
        Manual(
            id = "valve_clearance",
            title = "การปรับตั้งระยะห่างวาล์วไอดี-ไอเสีย (Valve Clearance)",
            category = "เครื่องยนต์ (Engine)",
            bikeModel = "Honda Wave 110i / 125i, Dream, Super Cub",
            difficulty = "ระดับช่าง (Medium-Hard)",
            estimatedTimeMinutes = 45,
            imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=1200&q=80",
            description = "คู่มือขั้นตอนการตั้งวาล์วไอดีและไอเสียอย่างถูกต้อง ป้องกันวาล์วยัน วาล์วรั่ว และเพิ่มกำลังอัดเครื่องยนต์",
            toolsNeeded = listOf(
                "ฟิลเลอร์เกจ (Feeler Gauge 0.10mm, 0.15mm)",
                "ประแจแหวนเบอร์ 9 หรือเบอร์ 10",
                "เครื่องมือขันตั้งวาล์วตัว T หรือคีมปากจิ้งจก",
                "ประแจบล็อกเบอร์ 14 หรือ 17 (หมุนเพลาข้อเหวี่ยง)",
                "ประแจหกเหลี่ยมเบอร์ 6 (เปิดฝาเช็กมาร์ก)"
            ),
            steps = listOf(
                ManualStep(
                    stepNumber = 1,
                    title = "ดับเครื่องยนต์และรอให้เครื่องเย็นสนิท",
                    instruction = "การตั้งวาล์วต้องทำขณะอุณหภูมิเครื่องยนต์ต่ำกว่า 35°C (อุณหภูมิห้อง) เสมอ เพื่อป้องกันระยะห่างวาล์วคลาดเคลื่อนจากการขยายตัวของโลหะ",
                    imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ห้ามตั้งวาล์วขณะเครื่องยนต์ยังร้อนอยู่เด็ดขาด จะทำให้วาล์วยันเมื่อเครื่องเย็น",
                    toolsRequired = listOf("เทอร์โมมิเตอร์ หรือสัมผัสเสื้อสูบด้วยมือเปล่า")
                ),
                ManualStep(
                    stepNumber = 2,
                    title = "เปิดฝาครอบตั้งวาล์วและเปิดช่องมาร์กจานไฟ",
                    instruction = "ใช้ประแจขันเปิดฝาครอบวาล์วไอดีและไอเสียด้านบนและล่างของฝาสูบ จากนั้นเปิดฝาตรวจมาร์กจานไฟขนาดเล็กที่แคร้งเครื่องฝั่งซ้าย",
                    imageUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ระวังโอริงยางฝาครอบวาล์วฉีกขาด ตรวจสอบสภาพหากแข็งหรือบวมควรเปลี่ยนใหม่",
                    toolsRequired = listOf("ประแจแหวนเบอร์ 17 หรือ 24", "ไขควงแบนใหญ่ หรือเหรียญบาท")
                ),
                ManualStep(
                    stepNumber = 3,
                    title = "หมุนหาจังหวะอัดสุด (Top Dead Center - TDC)",
                    instruction = "ใช้ประแจบล็อกหมุนเพลาข้อเหวี่ยงทวนเข็มนาฬิกาจนมาร์กตัว 'T' บนล้อแม่เหล็กจานไฟตรงกับรอยบากที่แคร้งเครื่อง และกระเดื่องวาล์วทั้งสองตัวต้องให้ตัวได้ (จังหวะอัดสุด)",
                    imageUrl = "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "หากกระเดื่องกดแน่นแสดงว่าเป็นจังหวะคาย ให้หมุนข้อเหวี่ยงทวนเข็มอีก 1 รอบ (360 องศา)",
                    toolsRequired = listOf("ประแจบล็อกเบอร์ 14 หรือ 17")
                ),
                ManualStep(
                    stepNumber = 4,
                    title = "สอดฟิลเลอร์เกจและวัดระยะห่างวาล์ว",
                    instruction = "สอดแผ่นฟิลเลอร์เกจระหว่างปลายกระเดื่องกดกับปลายก้านวาล์ว สเปกมาตรฐาน Wave 110i: วาล์วไอดี = 0.10 ± 0.02 mm, วาล์วไอเสีย = 0.15 ± 0.02 mm แผ่นฟิลเลอร์เกจต้องสอดผ่านได้พอดีแบบมีความหนืดเล็กน้อย (Sliding fit)",
                    imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ถ้าฟิลเลอร์เกจหลวมเกินไปเครื่องจะมีเสียงเคาะแก๊กๆ ถ้าแน่นเกินไปจะเกิดอาการวาล์วยันสตาร์ทติดยาก",
                    toolsRequired = listOf("ฟิลเลอร์เกจ 0.10 mm", "ฟิลเลอร์เกจ 0.15 mm")
                ),
                ManualStep(
                    stepNumber = 5,
                    title = "ขันล็อกน็อตตั้งวาล์วและตรวจซ้ำ",
                    instruction = "ใช้เครื่องมือจับหัวสกรูตั้งวาล์วให้อยู่กับที่ แล้วใช้ประแจแหวนขันล็อกน็อตตัวเมียให้แน่น จากนั้นเสียบแผ่นฟิลเลอร์เกจตรวจซ้ำอีกครั้งว่าระยะยังหนืดพอดีหรือไม่",
                    imageUrl = "https://images.unsplash.com/photo-1504222490345-c075b6008014?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "จังหวะขันล็อกน็อต สกรูปรับมักจะหมุนตาม ต้องจับเครื่องมือตั้งให้มั่นคง",
                    toolsRequired = listOf("ประแจแหวนเบอร์ 9", "เครื่องมือจับหัวสกรูตั้งวาล์ว"),
                    torqueSpec = "10 - 12 Nm (ขันตึงมือแน่นพอดี ระวังเกลียวขาด)"
                ),
                ManualStep(
                    stepNumber = 6,
                    title = "ปิดฝาครอบวาล์วและทดสอบสตาร์ทเครื่องยนต์",
                    instruction = "ทาจาระบีบางๆ ที่โอริงฝาครอบวาล์ว ขันปิดฝาครอบวาล์วและฝาปิดจานไฟให้เรียบร้อย สตาร์ทเครื่องยนต์ฟังเสียงการทำงานที่รอบเดินเบาและเร่งเครื่องตรวจดูว่าไม่มีน้ำมันเครื่องรั่วซึม",
                    imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ตรวจเช็กคราบน้ำมันเครื่องรอบๆ ฝาครอบ หากพบน้ำมันซึมให้ดับเครื่องและตรวจโอริงทันที",
                    toolsRequired = listOf("ผ้าสะอาดเช็ดทำความสะอาด")
                )
            )
        ),
        Manual(
            id = "engine_oil_change",
            title = "การเปลี่ยนถ่ายน้ำมันเครื่อง & ตรวจเช็กไส้กรอง (Engine Oil Service)",
            category = "บำรุงรักษาตามระยะ (Service & Lube)",
            bikeModel = "รถจักรยานยนต์เกียร์ธรรมดาและออโตเมติกทุกรุ่น",
            difficulty = "ระดับเริ่มต้น (Easy)",
            estimatedTimeMinutes = 20,
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1200&q=80",
            description = "ขั้นตอนการถ่ายน้ำมันเครื่องเก่า เปลี่ยนแหวนรองอลูมิเนียม และเติมน้ำมันเครื่องใหม่ตามปริมาตรสเปกคู่มือโรงงาน",
            toolsNeeded = listOf(
                "ประแจแหวนเบอร์ 12 หรือ 17 (น็อตถ่าย)",
                "ถาดรองรับน้ำมันเครื่องเก่า",
                "กรวยเติมน้ำมันเครื่อง",
                "แหวนรองอลูมิเนียมตัวใหม่ (Crush Washer 12mm)",
                "น้ำมันเครื่องเกรด 10W-30 / 10W-40 JASO MA/MB",
                "ผ้าสะอาด"
            ),
            steps = listOf(
                ManualStep(
                    stepNumber = 1,
                    title = "อุ่นเครื่องยนต์ 2-3 นาที",
                    instruction = "สตาร์ทเครื่องยนต์ทิ้งไว้ 2-3 นาทีเพื่อให้น้ำมันเครื่องอุ่นตัว ไหลเวียนพาคราบตะกอนปนออกมาได้ง่าย จากนั้นดับเครื่องยนต์และตั้งรถด้วยขาตั้งคู่บนพื้นราบ",
                    imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ระวังท่อไอเสียและแคร้งเครื่องร้อน ลวกมือขณะเอื้อมถอดน็อต",
                    toolsRequired = listOf("ขาตั้งคู่")
                ),
                ManualStep(
                    stepNumber = 2,
                    title = "เปิดก้านวัดน้ำมันเครื่องและคลายน็อตถ่าย",
                    instruction = "เปิดฝาก้านวัดน้ำมันเครื่องออกเพื่อให้อากาศระบาย นำถาดรองวางใต้แคร้งเครื่อง ใช้ประแจเบอร์ 12 หรือ 17 คลายน็อตถ่ายน้ำมันใต้แคร้งเครื่องทวนเข็มนาฬิกา",
                    imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ระวังน้ำมันเครื่องร้อนพุ่งใส่มือขณะคลายน็อตเกลียวสุดท้าย",
                    toolsRequired = listOf("ประแจแหวนเบอร์ 12 หรือ 17", "ถาดรองน้ำมันเครื่อง")
                ),
                ManualStep(
                    stepNumber = 3,
                    title = "ปล่อยให้น้ำมันเครื่องเก่าไหลจนหมด",
                    instruction = "รอให้น้ำมันเครื่องเก่าไหลออกจนหมด สามารถเอียงรถไปทางซ้ายและขวาเบาๆ เพื่อช่วยให้น้ำมันที่ค้างในซอกแคร้งไหลออกมาจนหมด",
                    imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ไม่แนะนำให้ใช้ลมแรงเป่าอัดเข้าไปในช่องเติม เพราะอาจพัดเอาฝุ่นผงและความชื้นเข้าไปในห้องเกียร์",
                    toolsRequired = listOf("ถาดรองน้ำมัน")
                ),
                ManualStep(
                    stepNumber = 4,
                    title = "เปลี่ยนแหวนรองตัวใหม่และขันน็อตถ่ายกลับ",
                    instruction = "ทำความสะอาดน็อตถ่ายและเปลี่ยนแหวนรองอลูมิเนียมตัวใหม่ทุกครั้ง ขันน็อตเข้าด้วยมือจนเกลียวสุดเพื่อป้องกันเกลียวหวาน แล้วใช้ประแจกวดแน่นตามแรงบิดสเปก",
                    imageUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ห้ามใช้ประแจกวดแรงเกินไป แคร้งเครื่องเป็นอลูมิเนียมเกลียวจะรูดได้ง่ายมาก",
                    toolsRequired = listOf("แหวนรองอลูมิเนียมตัวใหม่", "ประแจปอนด์"),
                    torqueSpec = "24 - 28 Nm (หรือขันตึงมือบวกเพิ่มอีก 1/8 รอบ)"
                ),
                ManualStep(
                    stepNumber = 5,
                    title = "เติมน้ำมันเครื่องใหม่ตามปริมาตรสเปก",
                    instruction = "ใช้กรวยเติมน้ำมันเครื่องใหม่ตามปริมาตรคู่มือ (เช่น Wave 110i ใช้ 0.8 ลิตร, PCX160 ใช้ 0.8 ลิตร) ปิดฝาก้านวัดน้ำมันเครื่องให้แน่น",
                    imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ตรวจสอบมาตรฐานน้ำมัน: เกียร์ธรรมดาใช้ JASO MA/MA2, ออโตเมติกใช้ JASO MB",
                    toolsRequired = listOf("กรวยเติมน้ำมัน", "น้ำมันเครื่องใหม่")
                ),
                ManualStep(
                    stepNumber = 6,
                    title = "ตรวจวัดระดับน้ำมันเครื่องด้วยก้านวัด",
                    instruction = "สตาร์ทเครื่องยนต์เดินเบา 1 นาที ดับเครื่องแล้วรอ 2 นาที ดึงก้านวัดออกมาเช็ดให้สะอาด เสียบก้านวัดลงไปโดย 'ไม่ต้องขันเกลียว' ระดับน้ำมันต้องอยู่ระหว่างขีดล่าง (Min) และขีดบน (Max)",
                    imageUrl = "https://images.unsplash.com/photo-1504222490345-c075b6008014?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "หากระดับน้ำมันต่ำกว่า Min ให้เติมเพิ่มทีละน้อย อย่าเติมเกินระดับ Max จะทำให้เครื่องอืด",
                    toolsRequired = listOf("ผ้าสะอาด")
                )
            )
        ),
        Manual(
            id = "drive_chain_adjustment",
            title = "การตั้งความตึงโซ่ & หล่อลื่นโซ่ขับเคลื่อน (Drive Chain Slack & Lube)",
            category = "ระบบขับเคลื่อน & โซ่ (Drive & Chain)",
            bikeModel = "มอเตอร์ไซค์โซ่สเตอร์ทุกขนาด (Street, Sport, Enduro)",
            difficulty = "ระดับเริ่มต้น-ปานกลาง (Easy-Medium)",
            estimatedTimeMinutes = 25,
            imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=1200&q=80",
            description = "ขั้นตอนการวัดระยะหย่อนโซ่ การปรับตั้งหางปลาสองฝั่งให้ตรงมาร์ก และการฉีดน้ำมันหล่อลื่นโซ่ O-Ring/X-Ring",
            toolsNeeded = listOf(
                "ประแจแหวนเบอร์ 19 หรือ 22 (น็อตเพลาล้อหลัง)",
                "ประแจแหวนเบอร์ 10 และ 12 (น็อตปรับหางปลาโซ่)",
                "ไม้บรรทัดหรือตลับเมตรวัดระยะ",
                "สเปรย์ล้างโซ่และสเปรย์หล่อลื่นโซ่สังเคราะห์",
                "แปรงขัดโซ่ 3 ด้าน"
            ),
            steps = listOf(
                ManualStep(
                    stepNumber = 1,
                    title = "ตรวจสอบระยะหย่อนโซ่เดิม",
                    instruction = "จอดรถบนขาตั้งคู่หรือขาตั้งข้าง (ตามสเปกคู่มือรุ่นนั้นๆ) ใช้มือกดและยกโซ่ช่วงกึ่งกลางระหว่างสเตอร์หน้าและหลัง วัดระยะยุบตัวจากจุดต่ำสุดถึงจุดสูงสุด ระยะมาตรฐานรถถนนคือ 25 - 35 mm",
                    imageUrl = "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "หมุนล้อตรวจหาระยะหย่อนหลายๆ จุด เพราะโซ่อาจยืดตัวไม่เท่ากัน ให้ตั้งที่จุดตึงที่สุด",
                    toolsRequired = listOf("ไม้บรรทัด / ตลับเมตร")
                ),
                ManualStep(
                    stepNumber = 2,
                    title = "คลายน็อตเพลาล้อหลัง",
                    instruction = "ใช้ประแจจับหัวเพลาล้อฝั่งหนึ่ง และใช้ประแจอีกตัวคลายน็อตเพลาล้อหลังทวนเข็มนาฬิกาประมาณ 1-2 รอบ (ไม่ต้องถอดออก แค่คลายให้ล้อเลื่อนได้)",
                    imageUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "หากมีสลักล็อก (Cotter pin) ให้ดึงสลักออกก่อนคลายน็อต",
                    toolsRequired = listOf("ประแจแหวนเบอร์ 19 และ 22")
                ),
                ManualStep(
                    stepNumber = 3,
                    title = "ปรับหมุนหางปลาตั้งโซ่ทั้งสองฝั่งให้เท่ากัน",
                    instruction = "คลายน็อตล็อกตัวนอก แล้วขันน็อตตัวในของหางปลาตั้งโซ่ตามเข็มนาฬิกาทีละ 1/4 รอบสลับกันทั้งฝั่งซ้ายและขวา ตรวจดูขีดมาร์กบนสวิงอาร์มให้ตรงกันทั้งสองข้างเพื่อป้องกันล้อเอียง",
                    imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "มาร์กสองฝั่งต้องตรงกันเป๊ะ ล้อที่เอียงจะทำให้กินยางข้างเดียวและโซ่สเตอร์สึกหรอเร็วผิดปกติ",
                    toolsRequired = listOf("ประแจปากตายเบอร์ 10 และ 12")
                ),
                ManualStep(
                    stepNumber = 4,
                    title = "ขันล็อกน็อตเพลาล้อหลังและขันน็อตล็อกหางปลา",
                    instruction = "ดันล้อหลังไปข้างหน้าให้ชิดหางปลา ขันล็อกน็อตเพลาล้อหลังให้แน่นตามแรงบิดสเปก จากนั้นขันล็อกน็อตตัวนอกของหางปลาตั้งโซ่ให้แน่น เสียบสลักล็อกกลับคืน",
                    imageUrl = "https://images.unsplash.com/photo-1504222490345-c075b6008014?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ตรวจวัดระยะหย่อนซ้ำหลังจากขันน็อตล้อแน่น เพราะระยะหย่อนอาจตึงขึ้นเล็กน้อย",
                    toolsRequired = listOf("ประแจปอนด์", "คีมดัดสลักล็อก"),
                    torqueSpec = "85 - 95 Nm (น็อตเพลาล้อหลัง)"
                ),
                ManualStep(
                    stepNumber = 5,
                    title = "ทำความสะอาดและฉีดสเปรย์หล่อลื่นโซ่",
                    instruction = "ฉีดน้ำยาล้างโซ่ ใช้แปรงขัดคราบดินทรายและจาระบีเก่าออก เช็ดให้แห้ง แล้วฉีดสเปรย์หล่อลื่นโซ่ที่ด้านในของข้อต่อโซ่ขณะหมุนล้อหลังช้าๆ ทิ้งไว้ 15 นาทีก่อนนำรถไปใช้งาน",
                    imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ห้ามสตาร์ทรถเข้าเกียร์แล้วใช้มือจับโซ่เด็ดขาด นิ้วอาจถูกสเตอร์หนีบขาดได้!",
                    toolsRequired = listOf("สเปรย์ล้างโซ่", "สเปรย์หล่อลื่นโซ่ Chain Lube", "แปรงขัด")
                )
            )
        ),
        Manual(
            id = "brake_pads_replacement",
            title = "การเปลี่ยนผ้าเบรกดิสก์หน้า & ไล่ลมน้ำมันเบรก (Front Brake Pads & Bleeding)",
            category = "ระบบเบรก (Braking System)",
            bikeModel = "มอเตอร์ไซค์ดิสก์เบรกไฮดรอลิกทุกรุ่น",
            difficulty = "ระดับช่าง (Medium-Hard)",
            estimatedTimeMinutes = 40,
            imageUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=1200&q=80",
            description = "ขั้นตอนการถอดคาลิปเปอร์ กดลูกสูบเบรก เปลี่ยนผ้าเบรกใหม่ และการไล่ฟองอากาศในระบบน้ำมันเบรก DOT4",
            toolsNeeded = listOf(
                "ประแจแหวนเบอร์ 12 หรือ 14 (น็อตยึดคาลิปเปอร์)",
                "ประแจหกเหลี่ยมเบอร์ 5 หรือ 6 (สลักล็อกผ้าเบรก)",
                "ประแจแหวนเบอร์ 8 (น็อตไล่ลมเบรก)",
                "สายยางซิลิโคนใสและขวดดักน้ำมันเบรก",
                "น้ำมันเบรกมาตรฐาน DOT 3 หรือ DOT 4",
                "จาระบีทาสลักเบรกทนความร้อนสูง (Silicone Brake Grease)"
            ),
            steps = listOf(
                ManualStep(
                    stepNumber = 1,
                    title = "คลายสลักล็อกผ้าเบรกก่อนถอดคาลิปเปอร์",
                    instruction = "ใช้ประแจหกเหลี่ยมคลายสลักยึดผ้าเบรกออกเล็กน้อยขณะที่คาลิปเปอร์ยังยึดอยู่กับกระบอกโช้ค เพื่อให้มีแรงต้านในการคลายเกลียวได้ง่ายขึ้น",
                    imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "อย่าถอดสลักออกหมดในขั้นตอนนี้ แค่คลายให้หลวม",
                    toolsRequired = listOf("ประแจหกเหลี่ยมเบอร์ 5 หรือ 6")
                ),
                ManualStep(
                    stepNumber = 2,
                    title = "ถอดน็อตยึดคาลิปเปอร์และถอดผ้าเบรกเก่า",
                    instruction = "ถอดน็อตยึดคาลิปเปอร์ 2 ตัว ดึงคาลิปเปอร์ออกจากจานดิสก์ จากนั้นถอดสลักล็อกและนำผ้าเบรกเก่าพร้อมแผ่นกันเสียงออกมา",
                    imageUrl = "https://images.unsplash.com/photo-1504222490345-c075b6008014?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ห้ามบีบก้านเบรกเล่นขณะถอดคาลิปเปอร์ออก เพราะจะทำให้ลูกสูบเบรกหลุดทะลักออกมา",
                    toolsRequired = listOf("ประแจแหวนเบอร์ 12")
                ),
                ManualStep(
                    stepNumber = 3,
                    title = "ทำความสะอาดและดันลูกสูบเบรกกลับเข้าที่",
                    instruction = "ใช้น้ำยาล้างเบรกทำความสะอาดคราบผงผ้าเบรกรอบลูกสูบ จากนั้นใช้เครื่องมือกดลูกสูบหรือผ้าเบรกเก่าวางรองแล้วดันลูกสูบกลับเข้าไปจนสุดระนาบ",
                    imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "เปิดฝากระปุกน้ำมันเบรกด้านบนก่อนดันลูกสูบ ระวังระดับน้ำมันเบรกล้นกระปุก ท่วมใส่สีรถ (น้ำมันเบรกกัดสี)",
                    toolsRequired = listOf("สเปรย์ล้างเบรก", "เครื่องมือกดลูกสูบ หรือคีมล็อกรองผ้า")
                ),
                ManualStep(
                    stepNumber = 4,
                    title = "ทาจาระบีสลักสไลด์และใส่ผ้าเบรกชุดใหม่",
                    instruction = "ทำความสะอาดสลักสไลด์คาลิปเปอร์ ทาจาระบีซิลิโคนทนความร้อนบางๆ ประกอบผ้าเบรกชุดใหม่ ใส่สลักล็อก ขันสลักให้ตึงมือ ประกอบคาลิปเปอร์เข้ากับจานดิสก์และขันน็อตยึดตามสเปก",
                    imageUrl = "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "ห้ามให้จาระบีหรือน้ำมันเปื้อนหน้าสัมผัสผ้าเบรกและจานดิสก์เด็ดขาด จะทำให้เบรกไม่อยู่",
                    toolsRequired = listOf("จาระบีเบรกซิลิโคน", "ประแจปอนด์"),
                    torqueSpec = "30 - 35 Nm (น็อตยึดคาลิปเปอร์)"
                ),
                ManualStep(
                    stepNumber = 5,
                    title = "ปั๊มก้านเบรกและไล่ลมน้ำมันเบรก (Bleeding)",
                    instruction = "บีบย้ำก้านเบรก 4-5 ครั้งจนรู้สึกว่าก้านเบรกแข็งตึงมือ เสียบสายยางใสที่น็อตไล่ลม บีบก้านเบรกค้างไว้แล้วคลายน็อตไล่ลมเบอร์ 8 ออก 1/4 รอบ ปล่อยให้น้ำมันและฟองอากาศพุ่งออก แล้วขันน็อตปิดก่อนปล่อยก้านเบรก ทำซ้ำจนไม่มีฟองอากาศ",
                    imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=1000&q=80",
                    cautionText = "คอยเติมน้ำมันเบรกในกระปุกด้านบนอย่าให้แห้งเด็ดขาด มิฉะนั้นอากาศจะถูกดูดเข้าสู่ระบบใหม่",
                    toolsRequired = listOf("สายยางใส", "ขวดรอง", "ประแจแหวนเบอร์ 8", "น้ำมันเบรก DOT4")
                )
            )
        )
    )
}
