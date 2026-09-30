package com.example.data.repository

import android.content.Context
import com.example.MotorBoyDatabase
import com.example.ProductPriceDao
import com.example.ProductPriceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductPriceRepository private constructor(context: Context) {
    private val database = MotorBoyDatabase.getDatabase(context)
    private val dao: ProductPriceDao = database.productPriceDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultProductsIfEmpty()
        }
    }

    val allProducts: Flow<List<ProductPriceEntity>> = dao.getAllProducts()

    suspend fun getAllProductsList(): List<ProductPriceEntity> = withContext(Dispatchers.IO) {
        dao.getAllProductsList()
    }

    suspend fun searchProducts(query: String): List<ProductPriceEntity> = withContext(Dispatchers.IO) {
        dao.searchProducts(query)
    }

    suspend fun getProductByName(name: String): ProductPriceEntity? = withContext(Dispatchers.IO) {
        dao.getProductByName(name)
    }

    suspend fun insertProduct(product: ProductPriceEntity): Long = withContext(Dispatchers.IO) {
        dao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductPriceEntity) = withContext(Dispatchers.IO) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductPriceEntity) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun deleteByName(name: String): Boolean = withContext(Dispatchers.IO) {
        val affected = dao.deleteByName(name)
        affected > 0
    }

    /**
     * Updates an existing product price by name or partial match
     */
    suspend fun updatePriceByName(
        name: String,
        newRetailPrice: Double? = null,
        newCostPrice: Double? = null,
        newInstalledPrice: Double? = null
    ): Pair<Boolean, ProductPriceEntity?> = withContext(Dispatchers.IO) {
        val directMatch = dao.getProductByName(name)
        val target = directMatch ?: dao.searchProducts(name).firstOrNull()

        if (target != null) {
            val updated = target.copy(
                retailPrice = newRetailPrice ?: target.retailPrice,
                costPrice = newCostPrice ?: target.costPrice,
                installedPrice = newInstalledPrice ?: target.installedPrice
            )
            dao.updateProduct(updated)
            Pair(true, updated)
        } else {
            Pair(false, null)
        }
    }

    /**
     * Generates a concise summary text of current prices to inject into AI context
     */
    suspend fun getPriceSummaryForAi(): String = withContext(Dispatchers.IO) {
        val products = dao.getAllProductsList()
        if (products.isEmpty()) return@withContext "ยังไม่มีรายการสินค้าในระบบ"

        val sb = StringBuilder()
        sb.append("ตารางรายการสินค้าและราคาอัปเดตล่าสุดของอู่:\n")
        val grouped = products.groupBy { it.category }
        for ((cat, list) in grouped) {
            sb.append("[$cat]\n")
            for (p in list) {
                val installed = if (p.installedPrice > 0) " | พร้อมติดตั้ง: ${p.installedPrice.toInt()} บ." else ""
                sb.append("• ${p.name}: ขายปลีก ${p.retailPrice.toInt()} บ. (ทุน ${p.costPrice.toInt()} บ.)$installed\n")
            }
        }
        sb.toString()
    }

    private suspend fun seedDefaultProductsIfEmpty() {
        if (dao.getCount() > 0) return

        val initialProducts = listOf(
            // 1. น้ำมันเครื่อง
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T ฮอนด้า 0.7L", costPrice = 80.0, retailPrice = 100.0, installedPrice = 110.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T ฮอนด้า 0.8L", costPrice = 95.0, retailPrice = 115.0, installedPrice = 120.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4AT ฮอนด้า 0.8L", costPrice = 95.0, retailPrice = 115.0, installedPrice = 120.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T ฮอนด้า 1.0L", costPrice = 105.0, retailPrice = 120.0, installedPrice = 130.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T ปตท. 0.8L", costPrice = 80.0, retailPrice = 95.0, installedPrice = 100.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T ปตท. 1.0L", costPrice = 100.0, retailPrice = 120.0, installedPrice = 120.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4AT ยามาฮ่า 0.8L", costPrice = 95.0, retailPrice = 120.0, installedPrice = 130.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T 20W-40 คาสตอล 0.8L", costPrice = 100.0, retailPrice = 120.0, installedPrice = 130.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T 10W-30 คาสตอล 0.8L(กระป๋องทอง)", costPrice = 150.0, retailPrice = 160.0, installedPrice = 170.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง 4T 20W-40 คาสตอล 1.0L", costPrice = 115.0, retailPrice = 140.0, installedPrice = 150.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง คูโบต้าตราช้าง 3.0L", costPrice = 300.0, retailPrice = 320.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง เทรน 1.0L", costPrice = 100.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง เพาว์ซ่า 1.0L", costPrice = 105.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง คาวเทค 1.0L", costPrice = 100.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง เพาว์ซ่า 5+1.0L", costPrice = 580.0, retailPrice = 620.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเครื่อง", name = "น้ำมันเครื่อง เทรน 3.0L", costPrice = 280.0, retailPrice = 300.0, installedPrice = 0.0),

            // 2. น้ำมัน 2T
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T สเตทร์ 0.5L", costPrice = 50.0, retailPrice = 60.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T ปตท. 0.5L", costPrice = 50.0, retailPrice = 60.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T มอลลี่ 0.3L", costPrice = 30.0, retailPrice = 35.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T เชล 0.5L", costPrice = 80.0, retailPrice = 95.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T คาสตอล 0.5L", costPrice = 70.0, retailPrice = 85.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมัน 2T", name = "2T คาสตอล 0.1L", costPrice = 100.0, retailPrice = 130.0, installedPrice = 0.0),

            // 3. น้ำมันเสริมอื่นๆ
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเกียร์ 999 4.5L", costPrice = 220.0, retailPrice = 240.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเกียร์ เทรน 1.0L", costPrice = 90.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันไฮดรอลิก เทรน 1.0L", costPrice = 95.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันพาวเวอร์ เทรน 1.0L", costPrice = 115.0, retailPrice = 140.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันหยอดทิ้งเลี้ยงโซ่ 4.0L", costPrice = 140.0, retailPrice = 160.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเบรค Boss 0.5L", costPrice = 50.0, retailPrice = 70.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเบรค เชล 0.5L", costPrice = 95.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเบรค ปตท. 0.5L", costPrice = 95.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันเฟืองท้าย 0.120L", costPrice = 35.0, retailPrice = 50.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำยาหม้อน้ำ 1.0L", costPrice = 80.0, retailPrice = 120.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำยาหม้อน้ำ 0.2L", costPrice = 20.0, retailPrice = 50.0, installedPrice = 0.0),
            ProductPriceEntity(category = "น้ำมันเสริมอื่นๆ", name = "น้ำมันโช๊ค 0.2L", costPrice = 35.0, retailPrice = 50.0, installedPrice = 0.0),

            // 4. อะไหล่ที่ได้เปลี่ยนถ่ายประจำ
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "หลอดไฟ หน้า-หลัง มอเตอร์ไซค์", costPrice = 10.0, retailPrice = 25.0, installedPrice = 40.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ผ้าเบรค (ดีส)", costPrice = 35.0, retailPrice = 45.0, installedPrice = 60.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ผ้าเบรค (ดั้ม) เล็ก", costPrice = 60.0, retailPrice = 70.0, installedPrice = 90.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ผ้าเบรค (ดั้ม) ใหญ่", costPrice = 60.0, retailPrice = 90.0, installedPrice = 120.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ชุดโซ่สเตอร์อย่างดี", costPrice = 370.0, retailPrice = 420.0, installedPrice = 450.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ชุดโซ่สเตอร์ธรรมดา", costPrice = 250.0, retailPrice = 320.0, installedPrice = 350.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "สเตอร์หน้าอย่างดี", costPrice = 50.0, retailPrice = 70.0, installedPrice = 80.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "สเตอร์หน้าธรรมดา", costPrice = 35.0, retailPrice = 50.0, installedPrice = 60.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "สเตอร์หลังอย่างดี", costPrice = 180.0, retailPrice = 220.0, installedPrice = 250.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "สเตอร์หลังธรรมดา", costPrice = 120.0, retailPrice = 150.0, installedPrice = 180.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "โซ่อย่างดี", costPrice = 180.0, retailPrice = 220.0, installedPrice = 250.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "โซ่ธรรมดา", costPrice = 120.0, retailPrice = 150.0, installedPrice = 180.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 200/17 (50/90-50/100)", costPrice = 220.0, retailPrice = 250.0, installedPrice = 280.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 225-250/17 (60-70/90-60-70/100)", costPrice = 230.0, retailPrice = 270.0, installedPrice = 300.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 275/17 (80/80-80/90)", costPrice = 320.0, retailPrice = 350.0, installedPrice = 380.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 300/17 (90/90-90/100)", costPrice = 350.0, retailPrice = 390.0, installedPrice = 420.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 250/14 (70/90-70/100)", costPrice = 280.0, retailPrice = 330.0, installedPrice = 360.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 275/14 (80/90-80/100)", costPrice = 320.0, retailPrice = 350.0, installedPrice = 380.0),
            ProductPriceEntity(category = "อะไหล่เปลี่ยนประจำ", name = "ยางนอก 300/14 (90/90-90/100)", costPrice = 330.0, retailPrice = 390.0, installedPrice = 420.0),

            // 5. Ball Bearing (ลูกปืน)
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6200", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6201", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6202", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6203", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6204", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6205", costPrice = 40.0, retailPrice = 60.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6300", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6301", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6302", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6303", costPrice = 30.0, retailPrice = 50.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),
            ProductPriceEntity(category = "ลูกปืน (Ball Bearing)", name = "ลูกปืน 6304", costPrice = 40.0, retailPrice = 60.0, installedPrice = 150.0, unitNote = "ต่อคู่/ต่อครั้ง"),

            // 6. งานบริการ & ค่าแรงมาตรฐาน
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ปะยาง (ปะสตรีม / ตัวหนอน)", costPrice = 10.0, retailPrice = 50.0, installedPrice = 50.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "เปลี่ยนยางใน มอเตอร์ไซค์", costPrice = 60.0, retailPrice = 80.0, installedPrice = 100.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "เปลี่ยนลูกปืนแผงคอ (ถ้วยคอ + ลูกปืน)", costPrice = 120.0, retailPrice = 180.0, installedPrice = 280.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ล้างคาร์บูเรเตอร์ / ล้างเรือนลิ้นเร่ง", costPrice = 20.0, retailPrice = 120.0, installedPrice = 150.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ล้างหัวฉีดด้วยเครื่องล้าง", costPrice = 30.0, retailPrice = 150.0, installedPrice = 180.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ตั้งโซ่ + หยอดน้ำมันโซ่", costPrice = 5.0, retailPrice = 30.0, installedPrice = 30.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ตั้งวาล์ว ไอดี-ไอเสีย", costPrice = 10.0, retailPrice = 100.0, installedPrice = 120.0),
            ProductPriceEntity(category = "งานบริการ/ค่าแรง", name = "ผ่าเครื่องฟิตใหม่ (ค่าแรงมาตรฐาน)", costPrice = 100.0, retailPrice = 800.0, installedPrice = 1000.0)
        )

        dao.insertAll(initialProducts)
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductPriceRepository? = null

        fun getInstance(context: Context): ProductPriceRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProductPriceRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
