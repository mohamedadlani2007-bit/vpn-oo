package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.vpn.VpnServer
import com.example.vpn.VpnStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VPN", appName)
  }

  @Test
  fun `default vpn servers list is valid`() {
    val servers = VpnServer.DEFAULT_SERVERS
    assertTrue(servers.isNotEmpty())
    val first = servers.first()
    assertTrue(first.id.isNotBlank())
    assertNotNull(first.flagEmoji)
  }

  @Test
  fun `vpn statistics formatting works`() {
    val stats = VpnStatistics(
      durationSeconds = 3665,
      downloadSpeedBps = 1048576,
      uploadSpeedBps = 2048,
      totalBytesDownloaded = 52428800
    )
    assertEquals("01:01:05", stats.formattedDuration())
    assertEquals("1.0 MB/s", stats.formattedDownloadSpeed())
    assertEquals("2.0 KB/s", stats.formattedUploadSpeed())
    assertEquals("50.0 MB", stats.formattedDownloadedTotal())
  }
}
