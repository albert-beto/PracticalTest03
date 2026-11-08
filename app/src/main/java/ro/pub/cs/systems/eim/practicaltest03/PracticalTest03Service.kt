package ro.pub.cs.systems.eim.practicaltest03

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class PracticalTest03Service : Service() {
    private var processingThread: ProcessingThread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val total = intent?.getIntExtra(Constants.TOTAL, 0) ?: 0
        if (processingThread == null) {
            processingThread = ProcessingThread(this, total)
            processingThread?.start()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        processingThread?.stopThread()
        Log.d(Constants.TAG, "Service stopped")
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}