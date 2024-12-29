package com.example.financeflow.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.financeflow.R

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Recupera il titolo e il messaggio dalla notifica (se forniti)
        val title = intent?.getStringExtra("title") ?: "Promemoria in arrivo"
        val message = intent?.getStringExtra("message") ?: "Hai un promemoria in scadenza entro 2 giorni."

        // Mostra la notifica all'utente
        showNotification(context, title, message)
    }

    private fun showNotification(context: Context?, title: String, message: String) {
        context?.let {
            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Crea un NotificationChannel per Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "ReminderChannel",
                    "Promemoria",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Canale per le notifiche di promemoria"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Crea la notifica
            val notification = NotificationCompat.Builder(it, "ReminderChannel")
                .setSmallIcon(R.drawable.ic_launcher) // Imposta l'icona della notifica
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            // Mostra la notifica
            notificationManager.notify(5554, notification)  //
        }
    }
}
