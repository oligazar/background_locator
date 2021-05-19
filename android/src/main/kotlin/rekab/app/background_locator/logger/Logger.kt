package rekab.app.background_locator.logger

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Logger {
    private var uri: Uri? = null

    private fun appendLog(context: Context, text: String?) {
        val resolver = context.contentResolver

        val now = Date().time
        val dayStr = SimpleDateFormat("yyMMdd", Locale.US).format(now)
        val dateStr = SimpleDateFormat("MM/dd'T'kk:mm:ss.SSSZ", Locale.US).format(now)
        val fileName = "logs/log_$dayStr.txt"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                if (uri == null) {
                    uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                            ?: throw IOException("Failed to create new MediaStore record.")
                }

                resolver.openOutputStream(uri!!, "wa")?.use { outputStream ->
                    outputStream.write("$dateStr $text \n".toByteArray(Charset.defaultCharset()))
                } ?: throw IOException("Failed to open output stream.")
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString()

                val logFile = File(directory, fileName)
                if (!logFile.exists()) {
                    logFile.createNewFile()
                }
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter(FileWriter(logFile, true)).apply {
                    append("$dateStr ")
                    append(text)
                    newLine()
                    close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
//            uri?.let { orphanUri ->
//                 Don't leave an orphan entry in the MediaStore
//                resolver.delete(orphanUri, null, null)
//            }
        }
    }

    fun d(context: Context, text: String? = null) {
        val tag = getTag()
        appendLog(context,"$tag $text")

//        val check: Int = ActivityCompat.checkSelfPermission(context,
//                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        val tag = getTag()
//        if (check == PackageManager.PERMISSION_GRANTED) {
//            appendLog("$tag $text")
//        }
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
}

fun Context.d(text: String? = null) {
    Logger.d(this, text)
}