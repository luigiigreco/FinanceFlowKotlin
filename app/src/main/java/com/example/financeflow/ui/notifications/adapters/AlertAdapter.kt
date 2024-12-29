package com.example.financeflow.ui.notifications.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeflow.R
import com.example.financeflow.ui.notifications.models.AlertItem

class AlertAdapter(
    private var alertList: MutableList<AlertItem>,
    private val onAlertLongClick: (AlertItem) -> Unit  // Callback per il long click sull'avviso
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.alertTitle)
        val messageTextView: TextView = view.findViewById(R.id.alertMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alertList[position]
        holder.titleTextView.text = alert.title
        holder.messageTextView.text = alert.message

        // Imposta il listener per il long click per avviare l'eliminazione
        holder.itemView.setOnLongClickListener {
            onAlertLongClick(alert)
            true
        }
    }

    override fun getItemCount(): Int {
        return alertList.size
    }

    // Funzione per aggiornare dinamicamente la lista degli avvisi
    fun updateAlerts(newAlerts: List<AlertItem>) {
        alertList.clear()
        alertList.addAll(newAlerts)
        notifyDataSetChanged()  // Notifica che i dati sono cambiati
    }

    // Funzione per rimuovere un avviso specifico
    fun removeAlert(alert: AlertItem) {
        val position = alertList.indexOf(alert)
        if (position != -1) {
            alertList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
