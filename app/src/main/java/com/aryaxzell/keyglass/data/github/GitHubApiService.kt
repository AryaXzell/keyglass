package com.aryaxzell.keyglass.data.github

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class GitHubProfile(
    val login: String = "aryaxzell",
    val name: String = "Arya Vallencia",
    val avatarUrl: String = "https://avatars.githubusercontent.com/u/aryaxzell",
    val bio: String = "Android & Software Developer",
    val htmlUrl: String = "https://github.com/aryaxzell"
)

data class NightlyArtifact(
    val id: Long,
    val name: String,
    val sizeInBytes: Long,
    val downloadUrl: String,
    val workflowRunId: Long,
    val createdAt: String,
    val matchedArchitecture: String
)

data class NightlyUpdateInfo(
    val artifactName: String,
    val downloadUrl: String,
    val htmlUrl: String,
    val targetAbi: String,
    val versionLabel: String,
    val runNumber: String,
    val isDirectApk: Boolean,
    val hasUpdate: Boolean = true
)

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState()
    data class Extracting(val status: String) : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File, val apkUri: Uri) : UpdateDownloadState()
    data class Error(val message: String, val fallbackUrl: String) : UpdateDownloadState()
}

class GitHubRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun getDeviceArchitecture(): String {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        return when {
            abi.contains("arm64", ignoreCase = true) || abi.contains("aarch64", ignoreCase = true) -> "arm64-v8a"
            abi.contains("v7a", ignoreCase = true) || abi.contains("armeabi", ignoreCase = true) -> "armeabi-v7a"
            abi.contains("x86_64", ignoreCase = true) -> "x86_64"
            abi.contains("x86", ignoreCase = true) -> "x86"
            else -> abi
        }
    }

    suspend fun getDeveloperProfile(): Result<GitHubProfile> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/users/aryaxzell")
                .header("User-Agent", "KeyGlass-Android-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val profile = GitHubProfile(
                    login = json.optString("login", "aryaxzell"),
                    name = json.optString("name", "Arya Xzell"),
                    avatarUrl = json.optString("avatar_url", "https://avatars.githubusercontent.com/u/aryaxzell"),
                    bio = json.optString("bio", "Creator of KeyGlass"),
                    htmlUrl = json.optString("html_url", "https://github.com/aryaxzell")
                )
                Result.success(profile)
            } else {
                Result.success(GitHubProfile())
            }
        } catch (e: Exception) {
            Result.success(GitHubProfile())
        }
    }

    /**
     * Checks GitHub Releases for Nightly build artifacts.
     * Uses public release URLs that do not require authentication tokens.
     */
    suspend fun checkNightlyUpdates(currentVersionName: String = "1.0.0"): Result<NightlyUpdateInfo> = withContext(Dispatchers.IO) {
        val currentAbi = getDeviceArchitecture()
        val repoReleasePage = "https://github.com/aryaxzell/keyglass/releases"

        fun isSameVersion(tagName: String, currentVer: String): Boolean {
            val cleanTag = tagName.lowercase().removePrefix("v").replace("-nightly", "").replace("nightly", "").trim()
            val cleanCurrent = currentVer.lowercase().removePrefix("v").replace("-nightly", "").replace("nightly", "").trim()
            return cleanTag.isNotEmpty() && cleanCurrent.isNotEmpty() && (cleanTag == cleanCurrent || tagName.contains(currentVer, ignoreCase = true))
        }

        // Direct public release download URLs for tag 'nightly'
        val directAbiApkUrl = when (currentAbi) {
            "arm64-v8a" -> "https://github.com/aryaxzell/keyglass/releases/download/nightly/KeyGlass-arm64-v8a-nightly.apk"
            "armeabi-v7a" -> "https://github.com/aryaxzell/keyglass/releases/download/nightly/KeyGlass-armeabi-v7a-nightly.apk"
            else -> "https://github.com/aryaxzell/keyglass/releases/download/nightly/KeyGlass-all-arch-nightly.apk"
        }

        try {
            val releaseEndpoints = listOf(
                "https://api.github.com/repos/aryaxzell/keyglass/releases/tags/nightly",
                "https://api.github.com/repos/aryaxzell/keyglass/releases/latest"
            )

            for (endpoint in releaseEndpoints) {
                val releaseReq = Request.Builder()
                    .url(endpoint)
                    .header("User-Agent", "KeyGlass-Android-App")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val releaseRes = client.newCall(releaseReq).execute()
                if (releaseRes.isSuccessful) {
                    val body = releaseRes.body?.string() ?: ""
                    val json = JSONObject(body)
                    val assets = json.optJSONArray("assets") ?: JSONArray()
                    val tagName = json.optString("tag_name", "nightly")
                    val htmlUrl = json.optString("html_url", repoReleasePage)

                    val matchesVersion = isSameVersion(tagName, currentVersionName)

                    var matchedDownloadUrl: String? = null
                    var matchedName: String? = null
                    var isDirectApk = true

                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        val downloadUrl = asset.optString("browser_download_url", "")

                        if (assetName.lowercase().contains(currentAbi.lowercase()) ||
                            (currentAbi == "arm64-v8a" && assetName.lowercase().contains("arm64")) ||
                            (currentAbi == "armeabi-v7a" && assetName.lowercase().contains("v7a"))
                        ) {
                            matchedDownloadUrl = downloadUrl
                            matchedName = assetName
                            isDirectApk = assetName.endsWith(".apk", ignoreCase = true)
                            break
                        }
                    }

                    if (matchedDownloadUrl == null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val assetName = asset.optString("name", "")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                matchedDownloadUrl = asset.optString("browser_download_url", "")
                                matchedName = assetName
                                isDirectApk = true
                                break
                            }
                        }
                    }

                    if (matchedDownloadUrl != null) {
                        return@withContext Result.success(
                            NightlyUpdateInfo(
                                artifactName = matchedName ?: "KeyGlass Nightly ($currentAbi)",
                                downloadUrl = matchedDownloadUrl,
                                htmlUrl = htmlUrl,
                                targetAbi = currentAbi,
                                versionLabel = tagName,
                                runNumber = "Nightly",
                                isDirectApk = isDirectApk,
                                hasUpdate = !matchesVersion
                            )
                        )
                    }
                }
            }

            // Default fallback using public direct URL
            Result.success(
                NightlyUpdateInfo(
                    artifactName = "KeyGlass Nightly ($currentAbi)",
                    downloadUrl = directAbiApkUrl,
                    htmlUrl = repoReleasePage,
                    targetAbi = currentAbi,
                    versionLabel = "Nightly",
                    runNumber = "Latest",
                    isDirectApk = true,
                    hasUpdate = true
                )
            )
        } catch (e: Exception) {
            Result.success(
                NightlyUpdateInfo(
                    artifactName = "KeyGlass Nightly ($currentAbi)",
                    downloadUrl = directAbiApkUrl,
                    htmlUrl = repoReleasePage,
                    targetAbi = currentAbi,
                    versionLabel = "Nightly",
                    runNumber = "Latest",
                    isDirectApk = true,
                    hasUpdate = true
                )
            )
        }
    }

    /**
     * Downloads and extracts the APK artifact with multi-URL fallback if authorization fails (401/403).
     */
    suspend fun downloadAndExtractUpdate(
        context: Context,
        updateInfo: NightlyUpdateInfo,
        onProgress: (Float, Long, Long) -> Unit,
        onExtracting: (String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val urlsToTry = mutableListOf(updateInfo.downloadUrl)

        val directAbiUrl = "https://github.com/aryaxzell/keyglass/releases/download/nightly/KeyGlass-${updateInfo.targetAbi}-nightly.apk"
        val directUniversalUrl = "https://github.com/aryaxzell/keyglass/releases/download/nightly/KeyGlass-all-arch-nightly.apk"

        if (!urlsToTry.contains(directAbiUrl)) urlsToTry.add(directAbiUrl)
        if (!urlsToTry.contains(directUniversalUrl)) urlsToTry.add(directUniversalUrl)

        var lastException: Exception? = null

        for (url in urlsToTry) {
            try {
                val downloadDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val tempFile = File(downloadDir, "download_temp_${System.currentTimeMillis()}")

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "KeyGlass-Android-App")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val errCode = response.code
                    response.close()
                    if (errCode == 401 || errCode == 403) {
                        lastException = Exception("Otentikasi diperlukan ($errCode). Mengabaikan dan mencoba tautan publik...")
                        continue
                    } else if (errCode == 404) {
                        lastException = Exception("File tidak ditemukan ($errCode). Mencoba cermin alternatif...")
                        continue
                    } else {
                        lastException = Exception("Gagal mengunduh: HTTP $errCode")
                        continue
                    }
                }

                val body = response.body ?: run {
                    lastException = Exception("Respon kosong dari server")
                    return@run null
                } ?: continue

                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(tempFile)

                val buffer = ByteArray(8192)
                var totalBytesRead = 0L
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = if (contentLength > 0) totalBytesRead.toFloat() / contentLength else 0.5f
                    onProgress(progress.coerceIn(0f, 1f), totalBytesRead, contentLength)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                onExtracting("Memproses paket aplikasi...")

                val isApk = url.endsWith(".apk", ignoreCase = true) || updateInfo.isDirectApk || isApkFile(tempFile)

                if (isApk) {
                    val finalApk = File(downloadDir, "keyglass-${updateInfo.targetAbi}.apk")
                    if (finalApk.exists()) finalApk.delete()
                    tempFile.renameTo(finalApk)
                    return@withContext Result.success(finalApk)
                } else {
                    onExtracting("Mengekstrak file ZIP...")
                    val extractedApk = extractApkFromZip(tempFile, downloadDir, updateInfo.targetAbi)
                    tempFile.delete()

                    if (extractedApk != null && extractedApk.exists()) {
                        return@withContext Result.success(extractedApk)
                    } else {
                        lastException = Exception("Tidak ada file APK valid dalam arsip ZIP.")
                    }
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        val finalErrMsg = when {
            lastException?.message?.contains("401") == true || lastException?.message?.contains("403") == true ->
                "Gagal mengunduh otomatis karena otentikasi GitHub. Silakan buka halaman Release di browser."
            else ->
                lastException?.message ?: "Gagal mengunduh pembaruan dari GitHub."
        }

        Result.failure(Exception(finalErrMsg))
    }

    private fun isApkFile(file: File): Boolean {
        return try {
            val bytes = ByteArray(4)
            file.inputStream().use { it.read(bytes) }
            // APK is a zip archive format (starts with PK\x03\x04)
            bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
        } catch (e: Exception) {
            false
        }
    }

    private fun extractApkFromZip(zipFile: File, outputDir: File, targetAbi: String): File? {
        var foundApk: File? = null
        try {
            ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && name.endsWith(".apk", ignoreCase = true)) {
                        val outFile = File(outputDir, "keyglass-$targetAbi-nightly.apk")
                        if (outFile.exists()) outFile.delete()
                        FileOutputStream(outFile).use { fos ->
                            zis.copyTo(fos)
                        }
                        foundApk = outFile
                        break
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return foundApk
    }

    fun launchPackageInstaller(context: Context, apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    fun openGitHubActions(context: Context, url: String = "https://github.com/aryaxzell/keyglass/actions") {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
