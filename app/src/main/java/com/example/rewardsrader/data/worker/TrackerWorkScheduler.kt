package com.example.rewardsrader.data.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TrackerWorkScheduler {
    private const val TRACKER_WORK_NAME = "tracker_refresh_worker"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<TrackerRefreshWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRACKER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
