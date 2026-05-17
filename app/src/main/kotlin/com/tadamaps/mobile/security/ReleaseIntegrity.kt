package com.tadamaps.mobile.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tadamaps.mobile.BuildConfig
import java.security.MessageDigest

/**
 * Lightweight hardening for **release** builds: blocks obvious tamper/debug patterns.
 *
 * Not a substitute for server-side trust, Play App Integrity, or native protections—
 * it raises the bar for casual reverse engineering / repackaging.
 *
 * Optional: set `RELEASE_CERT_SHA256` (hex, no colons) in `local.properties` or
 * `config/<flavor>.properties` to reject APKs not signed with your release key.
 */
object ReleaseIntegrity {

    fun verifyAndEnforce(context: Context) {
        if (!BuildConfig.INTEGRITY_CHECKS_ENABLED) return

        val appInfo = context.applicationInfo
        if ((appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            fail()
        }

        val expected = BuildConfig.EXPECTED_CERT_SHA256.trim()
        if (expected.isEmpty()) return

        val actual = sha256HexOfFirstSigner(context)
        if (actual.isEmpty() || !actual.equals(expected, ignoreCase = true)) {
            fail()
        }
    }

    private fun fail(): Nothing {
        throw SecurityException()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun sha256HexOfFirstSigner(context: Context): String {
        val pm = context.packageManager
        val pkg = context.packageName
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(
                pkg,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
        }
        val sig = info.signingInfo?.apkContentsSigners?.firstOrNull() ?: return ""
        val cert = sig.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(cert)
        return digest.joinToString("") { "%02X".format(it) }
    }
}
