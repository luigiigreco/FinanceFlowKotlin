package com.example.financeflow.ui.notifications.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeflow.databinding.ItemNotificationBinding
import com.example.financeflow.ui.notifications.models.ReminderItem

class NotificationAdapter(
    private var reminderList: MutableList<ReminderItem>,  // Cambiato a MutableList per supportare aggiornamenti
    private val onItemLongClickListener: (ReminderItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        holder.bind(reminder)

        // Imposta il listener per la pressione prolungata
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener(reminder)
            true
        }
    }

    override fun getItemCount(): Int = reminderList.size

    // Funzione per aggiornare dinamicamente la lista dei promemoria
    fun updateReminders(newReminders: List<ReminderItem>) {
        reminderList.clear()
        reminderList.addAll(newReminders)
        notifyDataSetChanged()  // Notifica l'adapter di aggiornare la RecyclerView
    }

    inner class ReminderViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderItem) {
            binding.notificationTitle.text = reminder.title
            binding.notificationMessage.text = reminder.date
        }
    }
}
