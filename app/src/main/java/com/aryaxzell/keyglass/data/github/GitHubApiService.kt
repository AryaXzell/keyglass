package com.aryaxzell.keyglass.data.github

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
    val isDirectApk: Boolean
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
     * Checks GitHub Actions runs and artifacts matching current device ABI.
     * Fallback to GitHub Releases if artifacts require authorization.
     */
    suspend fun checkNightlyUpdates(): Result<NightlyUpdateInfo> = withContext(Dispatchers.IO) {
        val currentAbi = getDeviceArchitecture()
        val actionsUrl = "https://github.com/aryaxzell/keyglass/actions"

        try {
            // 1. Try fetching latest GitHub Release first (publicly downloadable APKs)
            val releaseReq = Request.Builder()
                .url("https://api.github.com/repos/aryaxzell/keyglass/releases/latest")
                .header("User-Agent", "KeyGlass-Android-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val releaseRes = client.newCall(releaseReq).execute()
            if (releaseRes.isSuccessful) {
                val body = releaseRes.body?.string() ?: ""
                val json = JSONObject(body)
                val assets = json.optJSONArray("assets") ?: JSONArray()
                val tagName = json.optString("tag_name", "nightly")
                val htmlUrl = json.optString("html_url", actionsUrl)

                var matchedDownloadUrl: String? = null
                var matchedName: String? = null
                var isDirectApk = false

                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "").lowercase()
                    val downloadUrl = asset.optString("browser_download_url", "")

                    // Check exact ABI match
                    if (assetName.contains(currentAbi.lowercase()) ||
                        (currentAbi == "arm64-v8a" && (assetName.contains("arm64") || assetName.contains("aarch64"))) ||
                        (currentAbi == "armeabi-v7a" && (assetName.contains("armv7") || assetName.contains("arm-v7a")))
                    ) {
                        matchedDownloadUrl = downloadUrl
                        matchedName = asset.optString("name", "keyglass-$currentAbi.apk")
                        isDirectApk = assetName.endsWith(".apk")
                        break
                    }
                }

                // If no exact ABI match found in release assets, look for universal / debug APK/ZIP
                if (matchedDownloadUrl == null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "").lowercase()
                        if (assetName.endsWith(".apk") || assetName.endsWith(".zip")) {
                            matchedDownloadUrl = asset.optString("browser_download_url", "")
                            matchedName = asset.optString("name", "keyglass-universal.apk")
                            isDirectApk = assetName.endsWith(".apk")
                            break
                        }
                    }
                }

                if (matchedDownloadUrl != null) {
                    return@withContext Result.success(
                        NightlyUpdateInfo(
                            artifactName = matchedName ?: "keyglass-$currentAbi",
                            downloadUrl = matchedDownloadUrl,
                            htmlUrl = htmlUrl,
                            targetAbi = currentAbi,
                            versionLabel = tagName,
                            runNumber = "Latest",
                            isDirectApk = isDirectApk
                        )
                    )
                }
            }

            // 2. Try GitHub Actions Workflow Runs
            val runsReq = Request.Builder()
                .url("https://api.github.com/repos/aryaxzell/keyglass/actions/runs?status=success&per_page=3")
                .header("User-Agent", "KeyGlass-Android-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val runsRes = client.newCall(runsReq).execute()
            if (runsRes.isSuccessful) {
                val runsBody = runsRes.body?.string() ?: ""
                val runsJson = JSONObject(runsBody)
                val workflowRuns = runsJson.optJSONArray("workflow_runs") ?: JSONArray()

                if (workflowRuns.length() > 0) {
                    val latestRun = workflowRuns.getJSONObject(0)
                    val runId = latestRun.optLong("id", 0L)
                    val runNum = latestRun.optString("run_number", "Nightly")
                    val runHtml = latestRun.optString("html_url", actionsUrl)
                    val artifactsUrl = latestRun.optString("artifacts_url", "")

                    if (artifactsUrl.isNotEmpty()) {
                        val artReq = Request.Builder()
                            .url(artifactsUrl)
                            .header("User-Agent", "KeyGlass-Android-App")
                            .header("Accept", "application/vnd.github.v3+json")
                            .build()

                        val artRes = client.newCall(artReq).execute()
                        if (artRes.isSuccessful) {
                            val artBody = artRes.body?.string() ?: ""
                            val artJson = JSONObject(artBody)
                            val artifacts = artJson.optJSONArray("artifacts") ?: JSONArray()

                            for (i in 0 until artifacts.length()) {
                                val art = artifacts.getJSONObject(i)
                                val name = art.optString("name", "")
                                val downloadUrl = art.optString("archive_download_url", "")

                                if (name.contains(currentAbi, ignoreCase = true) ||
                                    (currentAbi == "arm64-v8a" && name.contains("arm64", ignoreCase = true)) ||
                                    name.contains("nightly", ignoreCase = true) ||
                                    name.contains("app", ignoreCase = true)
                                ) {
                                    return@withContext Result.success(
                                        NightlyUpdateInfo(
                                            artifactName = name,
                                            downloadUrl = downloadUrl,
                                            htmlUrl = runHtml,
                                            targetAbi = currentAbi,
                                            versionLabel = "Run #$runNum",
                                            runNumber = runNum,
                                            isDirectApk = false
                                        )
                                    )
                                }
                            }
                        }
                    }

                    return@withContext Result.success(
                        NightlyUpdateInfo(
                            artifactName = "KeyGlass Nightly #$runNum ($currentAbi)",
                            downloadUrl = runHtml,
                            htmlUrl = runHtml,
                            targetAbi = currentAbi,
                            versionLabel = "Run #$runNum",
                            runNumber = runNum,
                            isDirectApk = false
                        )
                    )
                }
            }

            // Fallback information linking to GitHub Actions
            Result.success(
                NightlyUpdateInfo(
                    artifactName = "KeyGlass Nightly ($currentAbi)",
                    downloadUrl = actionsUrl,
                    htmlUrl = actionsUrl,
                    targetAbi = currentAbi,
                    versionLabel = "Latest Nightly",
                    runNumber = "Actions",
                    isDirectApk = false
                )
            )
        } catch (e: Exception) {
            Result.success(
                NightlyUpdateInfo(
                    artifactName = "KeyGlass Nightly ($currentAbi)",
                    downloadUrl = actionsUrl,
                    htmlUrl = actionsUrl,
                    targetAbi = currentAbi,
                    versionLabel = "GitHub Actions",
                    runNumber = "Actions",
                    isDirectApk = false
                )
            )
        }
    }

    /**
     * Downloads and extracts the APK artifact from zip, then returns the ready File.
     */
    suspend fun downloadAndExtractUpdate(
        context: Context,
        updateInfo: NightlyUpdateInfo,
        onProgress: (Float, Long, Long) -> Unit,
        onExtracting: (String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val downloadDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val tempFile = File(downloadDir, "download_temp_${System.currentTimeMillis()}")

            val request = Request.Builder()
                .url(updateInfo.downloadUrl)
                .header("User-Agent", "KeyGlass-Android-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
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

            onExtracting("Processing package...")

            // Check if downloaded file is directly an APK or a ZIP archive
            if (updateInfo.isDirectApk || isApkFile(tempFile)) {
                val finalApk = File(downloadDir, "keyglass-${updateInfo.targetAbi}.apk")
                if (finalApk.exists()) finalApk.delete()
                tempFile.renameTo(finalApk)
                return@withContext Result.success(finalApk)
            } else {
                // Extract zip archive to find the inner APK
                onExtracting("Extracting zip archive...")
                val extractedApk = extractApkFromZip(tempFile, downloadDir, updateInfo.targetAbi)
                tempFile.delete()

                if (extractedApk != null && extractedApk.exists()) {
                    Result.success(extractedApk)
                } else {
                    Result.failure(Exception("No valid APK found inside downloaded package."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
