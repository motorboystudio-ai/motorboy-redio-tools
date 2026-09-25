package com.example

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * RoomToFirestoreSyncHelper
 * ซิงค์ข้อมูลสแกน DTC และประวัติการซ่อมจากฐานข้อมูลในเครื่อง (หรือ UI State) ขึ้นสู่ Firebase Firestore Cloud
 */
object RoomToFirestoreSyncHelper {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun syncScanResultToCloud(
        customerName: String,
        bikeModel: String,
        licensePlate: String,
        dtcCodes: List<String>,
        repairNote: String
    ): Result<String> {
        return try {
            val recordMap = mapOf(
                "customerName" to customerName,
                "bikeModel" to bikeModel,
                "licensePlate" to licensePlate,
                "dtcCodes" to dtcCodes,
                "repairNote" to repairNote,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "workshop" to "บอย อะไหล่ยนต์ - หนองตาไก้"
            )

            val documentRef = firestore.collection("motorboy_repair_records")
                .add(recordMap)
                .await()

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCloudRecords(): List<Map<String, Any>> {
        return try {
            val querySnapshot = firestore.collection("motorboy_repair_records")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            querySnapshot.documents.map { doc ->
                val data = doc.data ?: emptyMap<String, Any>()
                data + mapOf("id" to doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
