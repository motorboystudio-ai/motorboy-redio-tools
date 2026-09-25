package com.example.domain

interface DtcRepository {
    suspend fun getDtcDescription(code: String): DtcCode?
    suspend fun scanDtcFromImage(imagePath: String): DtcCode?
}
