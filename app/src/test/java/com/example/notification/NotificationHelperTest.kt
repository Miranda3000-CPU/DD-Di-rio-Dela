package com.example.notification

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.UserPreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        UserPreferencesManager.setNotificationsEnabled(context, true)
        UserPreferencesManager.setPrivateNotifications(context, false)
        UserPreferencesManager.setUserName(context, "Giovanna")
    }

    @Test
    fun testNotificationChannelsCreated() {
        NotificationHelper.createNotificationChannels(context)

        val predictionsChannel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_PREDICTIONS)
        assertNotNull(predictionsChannel)
        assertEquals("Previsões do Ciclo", predictionsChannel.name)

        val remindersChannel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_REMINDERS)
        assertNotNull(remindersChannel)
        assertEquals("Lembretes do Diário", remindersChannel.name)
    }

    @Test
    fun testNotificationsDisabledRespectsSetting() {
        UserPreferencesManager.setNotificationsEnabled(context, false)
        notificationManager.cancelAll()

        NotificationHelper.showNotification(
            context = context,
            title = "Teste",
            message = "Mensagem que não deve aparecer",
            channelId = NotificationHelper.CHANNEL_PREDICTIONS,
            notificationId = 9999
        )

        val active = notificationManager.activeNotifications
        assertEquals(0, active.size)
    }

    @Test
    fun testPrivacyModeMasksSensitiveContent() {
        UserPreferencesManager.setNotificationsEnabled(context, true)
        UserPreferencesManager.setPrivateNotifications(context, true)
        notificationManager.cancelAll()

        NotificationHelper.showNotification(
            context = context,
            title = "Previsão Íntima",
            message = "Sua menstruação deve iniciar em breve.",
            channelId = NotificationHelper.CHANNEL_PREDICTIONS,
            notificationId = 1234
        )

        val active = notificationManager.activeNotifications
        assertEquals(1, active.size)
        val notif = active[0].notification
        val title = notif.extras.getString("android.title")
        val text = notif.extras.getString("android.text")

        assertEquals("DD • Diário Dela 🌷", title)
        assertEquals("Há uma atualização sobre seu ciclo.", text)
    }

    @Test
    fun testTestNotificationSent() {
        UserPreferencesManager.setNotificationsEnabled(context, true)
        UserPreferencesManager.setPrivateNotifications(context, false)
        notificationManager.cancelAll()

        NotificationHelper.sendTestNotification(context)

        val active = notificationManager.activeNotifications
        assertTrue(active.isNotEmpty())
        val notif = active.find { it.id == NotificationHelper.NOTIFICATION_ID_TEST }?.notification
        assertNotNull(notif)
        val text = notif?.extras?.getString("android.text")
        assertTrue(text?.contains("Giovanna") == true)
    }

    @Test
    fun testScheduleNotificationFor3DaysBefore() {
        notificationManager.cancelAll()
        val today = LocalDate.now()
        // Next period in 3 days
        val nextPeriod = today.plusDays(3)

        NotificationHelper.scheduleNextPeriodReminder(context, nextPeriod)

        val active = notificationManager.activeNotifications
        val notif = active.find { it.id == NotificationHelper.NOTIFICATION_ID_PREDICTION }?.notification
        assertNotNull("Deve exibir lembrete para 3 dias antes", notif)
        val text = notif?.extras?.getString("android.text")
        assertTrue(text?.contains("3 dias") == true)
    }
}
