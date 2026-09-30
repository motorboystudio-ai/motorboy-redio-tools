package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "maintenance_records")
data class MaintenanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bikeModel: String,          // e.g. "Wave110i", "Exciter155", "Ninja400"
    val brand: String,              // Honda, Yamaha, Suzuki, Kawasaki, GPX, Lifan
    val serviceType: String,        // e.g. "เปลี่ยนถ่ายน้ำมันเครื่อง", "เช็คยางและลมยาง", "ตั้งโซ่", "เปลี่ยนสายพาน"
    val mileage: Int,               // เลขไมล์รถ (กิโลเมตร)
    val cost: Double,               // ค่าบริการ (บาท)
    val note: String,               // หมายเหตุเพิ่มเติม
    val date: String                // วันที่รับบริการ
)

@Entity(tableName = "dtc_codes")
data class DtcCodeEntity(
    @PrimaryKey val code: String,
    val description: String,
    val severity: String,
    val component: String
)

@Entity(tableName = "bookmarked_dtcs")
data class BookmarkedDtcEntity(
    @PrimaryKey val code: String
)

@Entity(tableName = "product_prices")
data class ProductPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String,           // e.g. "น้ำมันเครื่อง", "น้ำมัน 2T", "น้ำมันเสริมอื่นๆ", "อะไหล่เปลี่ยนประจำ", "ลูกปืน (Ball Bearing)", "งานบริการ/ค่าแรง"
    val name: String,               // e.g. "น้ำมันเครื่อง 4T ฮอนด้า 0.7L", "ปะยาง", "เปลี่ยนลูกปืนแผงคอ"
    val costPrice: Double = 0.0,    // ราคาต้นทุน
    val retailPrice: Double = 0.0,  // ราคาขายปลีก
    val installedPrice: Double = 0.0, // ราคาพร้อมติดตั้ง/ค่าแรง
    val unitNote: String = "บาท"    // e.g. "บาท", "ต่อคู่", "ต่อครั้ง"
)

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<MaintenanceRecordEntity>>

    @Query("SELECT * FROM maintenance_records WHERE bikeModel = :model ORDER BY id DESC")
    fun getRecordsByModel(model: String): Flow<List<MaintenanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MaintenanceRecordEntity)

    @Delete
    suspend fun deleteRecord(record: MaintenanceRecordEntity)

    @Query("DELETE FROM maintenance_records")
    suspend fun clearAll()
}

@Dao
interface DtcDao {
    @Query("SELECT * FROM dtc_codes WHERE code = :code")
    suspend fun getDtcByCode(code: String): DtcCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDtc(dtcCodes: List<DtcCodeEntity>)

    @Query("SELECT * FROM bookmarked_dtcs")
    fun getBookmarkedDtcCodes(): Flow<List<BookmarkedDtcEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bookmarkDtc(dtc: BookmarkedDtcEntity)

    @Delete
    suspend fun unbookmarkDtc(dtc: BookmarkedDtcEntity)
}

@Dao
interface ProductPriceDao {
    @Query("SELECT * FROM product_prices ORDER BY category ASC, id ASC")
    fun getAllProducts(): Flow<List<ProductPriceEntity>>

    @Query("SELECT * FROM product_prices ORDER BY category ASC, id ASC")
    suspend fun getAllProductsList(): List<ProductPriceEntity>

    @Query("SELECT * FROM product_prices WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductPriceEntity>

    @Query("SELECT * FROM product_prices WHERE id = :id")
    suspend fun getProductById(id: Long): ProductPriceEntity?

    @Query("SELECT * FROM product_prices WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductPriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductPriceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductPriceEntity>)

    @Update
    suspend fun updateProduct(product: ProductPriceEntity)

    @Delete
    suspend fun deleteProduct(product: ProductPriceEntity)

    @Query("DELETE FROM product_prices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM product_prices WHERE name = :name")
    suspend fun deleteByName(name: String): Int

    @Query("SELECT COUNT(*) FROM product_prices")
    suspend fun getCount(): Int
}

@Database(
    entities = [
        MaintenanceRecordEntity::class,
        BookmarkedDtcEntity::class,
        DtcCodeEntity::class,
        ProductPriceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MotorBoyDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun dtcDao(): DtcDao
    abstract fun productPriceDao(): ProductPriceDao

    companion object {
        @Volatile
        private var INSTANCE: MotorBoyDatabase? = null

        fun getDatabase(context: android.content.Context): MotorBoyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    MotorBoyDatabase::class.java,
                    "motorboy_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
