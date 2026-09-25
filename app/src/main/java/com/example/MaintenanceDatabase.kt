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

@Database(entities = [MaintenanceRecordEntity::class, BookmarkedDtcEntity::class, DtcCodeEntity::class], version = 3, exportSchema = false)
abstract class MotorBoyDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun dtcDao(): DtcDao

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
