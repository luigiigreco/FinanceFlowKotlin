package com.example.financeflow.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financeflow.ui.notifications.models.ReminderItem
import com.example.financeflow.ui.notifications.models.AlertItem

class NotificationsViewModel : ViewModel() {

    // Lista dei promemoria
    private val _reminders = MutableLiveData<List<ReminderItem>>()
    val reminders: LiveData<List<ReminderItem>> get() = _reminders

    // Lista degli avvisi
    private val _alerts = MutableLiveData<List<AlertItem>>()
    val alerts: LiveData<List<AlertItem>> get() = _alerts

    // Metodo per aggiornare la lista dei promemoria
    fun updateReminders(newReminders: List<ReminderItem>) {
        _reminders.value = newReminders
        Log.d("NotificationsViewModel", "Lista dei promemoria aggiornata: $newReminders")
    }

    // Metodo per aggiungere un nuovo promemoria
    fun addReminder(reminder: ReminderItem) {
        val currentList = _reminders.value?.toMutableList() ?: mutableListOf()
        currentList.add(reminder)
        _reminders.value = currentList
        Log.d("NotificationsViewModel", "Promemoria aggiunto: $reminder")
    }

    // Metodo per rimuovere un promemoria
    fun removeReminder(reminder: ReminderItem) {
        val currentList = _reminders.value?.toMutableList()
        currentList?.remove(reminder)
        _reminders.value = currentList
        Log.d("NotificationsViewModel", "Promemoria rimosso: $reminder")
    }

    // Metodo per aggiornare l'intera lista degli avvisi
    fun updateAlerts(newAlerts: List<AlertItem>) {
        _alerts.value = newAlerts
        Log.d("NotificationsViewModel", "Lista degli avvisi aggiornata: $newAlerts")
    }

    // Metodo per aggiungere un singolo avviso
    fun addAlert(alert: AlertItem) {
        val currentList = _alerts.value?.toMutableList() ?: mutableListOf()
        currentList.add(alert)
        _alerts.value = currentList
        Log.d("NotificationsViewModel", "Avviso aggiunto: $alert")
    }

    // Metodo per rimuovere un avviso specifico
    fun removeAlert(alert: AlertItem) {
        val currentList = _alerts.value?.toMutableList()
        currentList?.remove(alert)
        _alerts.value = currentList
        Log.d("NotificationsViewModel", "Avviso rimosso: $alert")
    }
}
