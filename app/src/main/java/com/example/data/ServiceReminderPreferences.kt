package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.OilChangeReminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServiceReminderPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("oil_change_service_reminder_prefs", Context.MODE_PRIVATE)

    private val _reminderFlow = MutableStateFlow(loadReminder())
    val reminderFlow: StateFlow<OilChangeReminder> = _reminderFlow.asStateFlow()

    fun loadReminder(): OilChangeReminder {
        return OilChangeReminder(
            bikeModel = prefs.getString(KEY_BIKE_MODEL, "Honda Wave 110i") ?: "Honda Wave 110i",
            lastOilChangeMileage = prefs.getInt(KEY_LAST_OIL_MILEAGE, 12000),
            currentMileage = prefs.getInt(KEY_CURRENT_MILEAGE, 13850),
            intervalKm = prefs.getInt(KEY_INTERVAL_KM, 2000),
            lastChangeDate = prefs.getString(KEY_LAST_DATE, "2026-08-15") ?: "2026-08-15",
            oilBrandGrade = prefs.getString(KEY_OIL_GRADE, "10W-30 JASO MA") ?: "10W-30 JASO MA",
            notes = prefs.getString(KEY_NOTES, "เปลี่ยนแหวนรองน็อตถ่ายทุกครั้ง") ?: ""
        )
    }

    fun saveReminder(reminder: OilChangeReminder) {
        prefs.edit().apply {
            putString(KEY_BIKE_MODEL, reminder.bikeModel)
            putInt(KEY_LAST_OIL_MILEAGE, reminder.lastOilChangeMileage)
            putInt(KEY_CURRENT_MILEAGE, reminder.currentMileage)
            putInt(KEY_INTERVAL_KM, reminder.intervalKm)
            putString(KEY_LAST_DATE, reminder.lastChangeDate)
            putString(KEY_OIL_GRADE, reminder.oilBrandGrade)
            putString(KEY_NOTES, reminder.notes)
            apply()
        }
        _reminderFlow.value = reminder
    }

    fun updateCurrentMileage(newMileage: Int) {
        val current = _reminderFlow.value
        val updated = current.copy(currentMileage = newMileage)
        saveReminder(updated)
    }

    fun recordNewOilChange(
        bikeModel: String = _reminderFlow.value.bikeModel,
        mileage: Int,
        intervalKm: Int = _reminderFlow.value.intervalKm,
        oilGrade: String = _reminderFlow.value.oilBrandGrade,
        date: String,
        notes: String = ""
    ) {
        val newReminder = OilChangeReminder(
            bikeModel = bikeModel,
            lastOilChangeMileage = mileage,
            currentMileage = mileage,
            intervalKm = intervalKm,
            lastChangeDate = date,
            oilBrandGrade = oilGrade,
            notes = notes
        )
        saveReminder(newReminder)
    }

    companion object {
        private const val KEY_BIKE_MODEL = "bike_model"
        private const val KEY_LAST_OIL_MILEAGE = "last_oil_mileage"
        private const val KEY_CURRENT_MILEAGE = "current_mileage"
        private const val KEY_INTERVAL_KM = "interval_km"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_OIL_GRADE = "oil_grade"
        private const val KEY_NOTES = "notes"

        @Volatile
        private var INSTANCE: ServiceReminderPreferences? = null

        fun getInstance(context: Context): ServiceReminderPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServiceReminderPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
