package reposanalyzer.zipper

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

interface Zipper {
    private companion object {
        const val DOT_GZ = ".gz"
    }

    fun compressFolder(dir: File, removeAfterZip: Boolean = false) {
        try {
            if (!dir.exists() || !dir.isDirectory) {
                return
            }
            dir.walkTopDown().maxDepth(1).forEach { file ->
                if (!file.absolutePath.endsWith(DOT_GZ)) {
                    compressFile(file, removeAfterZip)
                }
            }
        } catch (e: IOException) {
            // ignore
        }
    }

    fun compressFile(file: File, removeAfterZip: Boolean = false) {
        if (!file.exists() || file.isDirectory || file.absolutePath.endsWith(DOT_GZ)) {
            return
        }
        val gzipFile = File(file.absolutePath + DOT_GZ)
        gzipFile.createNewFile()
        val fileOutputStream = FileOutputStream(gzipFile)
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
        val gzipOutputStream = GzipCompressorOutputStream(bufferedOutputStream)
        gzipOutputStream.bufferedWriter().use { out ->
            FileInputStream(file).bufferedReader().use { input ->
                input.lines().forEach { line ->
                    out.appendLine(line)
                }
            }
        }
        fileOutputStream.close()
        bufferedOutputStream.close()
        gzipOutputStream.close()
        if (removeAfterZip) {
            file.delete()
        }
    }
}
