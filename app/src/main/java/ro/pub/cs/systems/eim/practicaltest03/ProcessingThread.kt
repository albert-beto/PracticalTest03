package ro.pub.cs.systems.eim.practicaltest03

import android.app.Service
import android.content.Intent

class ProcessingThread (
    private val service: Service,
    @Volatile var total: Int
) : Thread() {
    @Volatile private var isRunning = true

    override fun run() {
        for (index in 1..5) {
            if (!isRunning) return

            sendMessage(index)

            if (index < 5) {
                try {
                    sleep(3000)
                } catch (e: InterruptedException) {
                    return
                }
            }
        }
        service.stopSelf()

    }
    private fun sendMessage(index: Int) {
        val intent = Intent()
        if (index % 2 == 0)
            intent.action = Constants.ACTION_TOCK
        else
            intent.action = Constants.ACTION_TICK
        intent.putExtra(Constants.EXTRA_INDEX, index)
        intent.putExtra(Constants.TOTAL, total)
        intent.setPackage(service.packageName)
        service.sendBroadcast(intent)
    }

    fun stopThread() {
        isRunning = false
        interrupt()
    }
}