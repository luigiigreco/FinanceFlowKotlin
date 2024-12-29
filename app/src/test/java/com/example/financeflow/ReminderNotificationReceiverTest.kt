package com.example.financeflow

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.financeflow.ui.notifications.ReminderNotificationReceiver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ReminderNotificationReceiverTest {

    @Test
    fun `onReceive should show notification with correct title and message`() {
        // Arrange
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra("title", "Test Title")
            putExtra("message", "Test Message")
        }

        val receiver = ReminderNotificationReceiver()

        // Act
        receiver.onReceive(context, intent)

        // Assert
        val shadowNotificationManager = Shadows.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        )
        val postedNotifications = shadowNotificationManager.allNotifications

        assertThat(postedNotifications).isNotEmpty() // Verifica che una notifica sia stata inviata
        assertThat(postedNotifications[0].extras.getString("android.title")).isEqualTo("Test Title")
        assertThat(postedNotifications[0].extras.getString("android.text")).isEqualTo("Test Message")
    }

    @Test
    fun `onReceive should use default title and message when extras are missing`() {
        // Arrange
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, ReminderNotificationReceiver::class.java)

        val receiver = ReminderNotificationReceiver()

        // Act
        receiver.onReceive(context, intent)

        // Assert
        val shadowNotificationManager = Shadows.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        )
        val postedNotifications = shadowNotificationManager.allNotifications

        assertThat(postedNotifications).isNotEmpty() // Verifica che una notifica sia stata inviata
        assertThat(postedNotifications[0].extras.getString("android.title")).isEqualTo("Promemoria in arrivo")
        assertThat(postedNotifications[0].extras.getString("android.text")).isEqualTo("Hai un promemoria in scadenza entro 2 giorni.")
    }
}
