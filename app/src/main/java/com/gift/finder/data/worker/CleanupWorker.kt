package com.gift.finder.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gift.finder.data.local.dao.RejectedGiftDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

/**
 * WorkManager worker for cleaning up old rejected gifts.
 * Removes rejections older than 6 months to prevent stale suggestions.
 */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rejectedGiftDao: RejectedGiftDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -6)
            val cutoffTime = calendar.timeInMillis

            rejectedGiftDao.deleteOlderThan(cutoffTime)
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e(WORK_NAME, "Error cleaning up old rejected gifts", e)
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "cleanup_work"
    }
}
