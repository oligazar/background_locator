package rekab.app.background_locator.logger

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Logger {

    private fun appendLog(text: String?) {
        val directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).toString()
        val logFile = File(directory, "log.txt")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val dateStr = SimpleDateFormat("MM/dd'T'kk:mm:ss.SSSZ", Locale.US).format(Date().time)
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter(FileWriter(logFile, true)).apply {
                append("$dateStr ")
                append(text)
                newLine()
                close()
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun d(text: String? = null) {
        val tag = getTag()
        appendLog("$tag $text")
    }

    private fun getTag(): String? {
        val stack = Thread.currentThread().stackTrace
        val indexOfD = stack.indexOfFirst { it.methodName == "d" }
        val caller = stack.getOrNull(indexOfD + 1)
        if (caller != null && !caller.isNativeMethod) {
            val className = caller.className.split(".").lastOrNull()
            val methodName = caller.methodName
            val lineNumber = caller.lineNumber
            return "$className.$methodName:$lineNumber"
        }
        return null
    }

    private fun getActivity(context: Context?): Activity? {
        if (context == null) {
            return null
        } else if (context is ContextWrapper) {
            return if (context is Activity) {
                context
            } else {
                getActivity((context).baseContext)
            }
        }
        return null
    }

}