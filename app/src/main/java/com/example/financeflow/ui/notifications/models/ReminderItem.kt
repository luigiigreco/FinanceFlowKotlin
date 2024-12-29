package com.example.financeflow.ui.notifications.models

data class ReminderItem(
    val id: String = "",             // ID unico per tracciare il promemoria nel database
    val title: String,               // Titolo del promemoria
    val date: String,                // Data del promemoria (in formato stringa)
    val timestamp: Long = System.currentTimeMillis(), // Timestamp per l'ordinamento cronologico
    val isRecurring: Boolean = false // Flag per indicare se il promemoria è ricorrente
)
