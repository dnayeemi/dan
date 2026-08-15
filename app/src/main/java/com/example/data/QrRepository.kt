package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {
    val allHistory: Flow<List<QrItem>> = qrDao.getAllHistory()

    suspend fun saveQr(item: QrItem): Long {
        return qrDao.insertQr(item)
    }

    suspend fun deleteQr(id: Long) {
        qrDao.deleteById(id)
    }

    suspend fun clearHistory() {
        qrDao.clearAll()
    }

    suspend fun getQrById(id: Long): QrItem? {
        return qrDao.getById(id)
    }
}
