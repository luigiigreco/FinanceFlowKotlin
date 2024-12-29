package com.example.financeflow.ui.notifications

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financeflow.R
import com.example.financeflow.databinding.FragmentNotificationsBinding
import com.example.financeflow.ui.notifications.adapters.AlertAdapter
import com.example.financeflow.ui.notifications.adapters.NotificationAdapter
import com.example.financeflow.ui.notifications.models.AlertItem
import com.example.financeflow.ui.notifications.models.ReminderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var remindersAdapter: NotificationAdapter
    private lateinit var alertsAdapter: AlertAdapter
    private val reminderList = mutableListOf<ReminderItem>()
    private val alertList = mutableListOf<AlertItem>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val CHANNEL_ID = "reminder_channel_id"
    private val NOTIFICATION_ID = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Crea il canale di notifica
        createNotificationChannel()

        // Inizializzazione di Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Imposta l'adapter per la RecyclerView dei promemoria
        remindersAdapter = NotificationAdapter(reminderList) { reminder ->
            showDeleteReminderDialog(reminder)
        }
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewNotifications.adapter = remindersAdapter

        // Imposta l'adapter per la RecyclerView degli avvisi con supporto al long click
        alertsAdapter = AlertAdapter(alertList) { alert ->
            showDeleteAlertDialog(alert)  // Mostra dialogo di conferma eliminazione
        }
        binding.recyclerViewAlerts.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAlerts.adapter = alertsAdapter

        // Osserva i dati nel ViewModel
        observeReminders()

        // Carica i promemoria da Firestore
        loadRemindersFromFirestore()

        // Aggiungi promemoria tramite FAB
        binding.fabAddReminder.setOnClickListener {
            showAddReminderDialog()
        }

        // Schedula il controllo giornaliero dei promemoria
        scheduleReminderWorker()

        // Monitora i budget in tempo reale
        monitorBudgetsFromFirestore()

        return root
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Promemoria Notifiche"
            val descriptionText = "Canale per le notifiche di promemoria"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminderWorker() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(reminderWorkRequest)
    }

    private fun observeReminders() {
        notificationsViewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            Log.d("NotificationsFragment", "Reminders aggiornati: $reminders")
            reminderList.clear()
            reminderList.addAll(reminders)
            remindersAdapter.notifyDataSetChanged()

            // Controlla se ci sono avvisi e aggiorna la RecyclerView degli avvisi
            val alerts = checkForUpcomingReminders(reminders)

            // Rende visibile sempre la sezione degli avvisi
            binding.titleAlerts.visibility = View.VISIBLE
            binding.recyclerViewAlerts.visibility = View.VISIBLE

            if (alerts.isEmpty()) {
                // Se non ci sono avvisi, aggiungi un elemento fittizio per informare l'utente
                alertList.clear()
                alertList.add(AlertItem("Nessun avviso", "Non ci sono avvisi al momento."))
            } else {
                alertList.clear()
                alertList.addAll(alerts)
            }

            alertsAdapter.notifyDataSetChanged()
            Log.d("observeReminders", "Avvisi aggiornati nella RecyclerView: ${alertList.size}")
        }
    }

    private fun loadRemindersFromFirestore() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("reminders")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("NotificationsFragment", "Errore nel caricamento dei promemoria: ${e.message}")
                        Toast.makeText(context, "Errore nel caricamento dei promemoria", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val reminders = mutableListOf<ReminderItem>()
                        for (document in snapshots.documents) {
                            val title = document.getString("title") ?: "Nessun titolo"
                            val date = document.getString("date") ?: "Nessuna data"

                            val reminder = ReminderItem(document.id, title, date)
                            reminders.add(reminder)
                        }
                        notificationsViewModel.updateReminders(reminders)
                    } else {
                        Toast.makeText(context, "Nessun promemoria trovato", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun checkForUpcomingReminders(reminders: List<ReminderItem>): List<AlertItem> {
        val alerts = mutableListOf<AlertItem>()
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate = calendar.time

        for (reminder in reminders) {
            val reminderDate = formatter.parse(reminder.date)
            if (reminderDate != null) {
                val diff = (reminderDate.time - currentDate.time) / (1000 * 60 * 60 * 24)
                if (diff == 2L) {
                    val alert = AlertItem(
                        title = "Promemoria in arrivo",
                        message = "Mancano 2 giorni per il promemoria '${reminder.title}'."
                    )
                    alerts.add(alert)
                    sendNotification("Promemoria in arrivo", "Mancano 2 giorni per il promemoria '${reminder.title}'.")
                } else if (diff == 0L) {
                    val alert = AlertItem(
                        title = "Promemoria scaduto",
                        message = "Il promemoria '${reminder.title}' è scaduto oggi."
                    )
                    alerts.add(alert)
                    sendNotification("Promemoria scaduto", "Il promemoria '${reminder.title}' è scaduto oggi.")
                }
            }
        }
        return alerts
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (notificationManager == null) {
            Log.e("NotificationsFragment", "Errore: NotificationManager è null. Notifica non inviata.")
            return
        }

        val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_reminder, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextReminderTitle)
        val dateEditText = dialogView.findViewById<EditText>(R.id.editTextReminderDate)
        val addButton = dialogView.findViewById<Button>(R.id.buttonAddReminder)

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    dateEditText.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        addButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val date = dateEditText.text.toString()

            if (title.isNotEmpty() && date.isNotEmpty()) {
                val newReminder = ReminderItem("", title, date)
                notificationsViewModel.addReminder(newReminder)

                val user = auth.currentUser
                user?.let {
                    val reminderMap = hashMapOf("title" to newReminder.title, "date" to newReminder.date)
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("reminders")
                        .add(reminderMap)
                        .addOnSuccessListener {
                            loadRemindersFromFirestore()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Errore nell'aggiunta del promemoria", Toast.LENGTH_SHORT).show()
                        }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showDeleteReminderDialog(reminder: ReminderItem) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Elimina Promemoria")
        dialogBuilder.setMessage("Sei sicuro di voler eliminare questo promemoria?")
        dialogBuilder.setPositiveButton("Elimina") { _, _ ->
            deleteReminder(reminder)
        }
        dialogBuilder.setNegativeButton("Annulla", null)
        dialogBuilder.show()
    }

    private fun deleteReminder(reminder: ReminderItem) {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("reminders")
                .document(reminder.id)
                .delete()
                .addOnSuccessListener {
                    notificationsViewModel.removeReminder(reminder)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Errore nell'eliminazione del promemoria", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteAlertDialog(alert: AlertItem) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Elimina Avviso")
        dialogBuilder.setMessage("Sei sicuro di voler eliminare questo avviso?")
        dialogBuilder.setPositiveButton("Elimina") { _, _ ->
            deleteAlert(alert)
        }
        dialogBuilder.setNegativeButton("Annulla", null)
        dialogBuilder.show()
    }

    private fun deleteAlert(alert: AlertItem) {
        alertList.remove(alert)
        alertsAdapter.notifyDataSetChanged()
        Toast.makeText(context, "Avviso eliminato", Toast.LENGTH_SHORT).show()
    }

    private fun monitorBudgetsFromFirestore() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("NotificationsFragment", "Errore nel monitoraggio dei budget: ${e.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        for (document in snapshots.documents) {
                            val name = document.getString("name") ?: "Nessun nome"
                            val amount = document.getDouble("amount") ?: 0.0
                            val usedAmount = document.getDouble("usedAmount") ?: 0.0
                            val expirationDate = document.getDate("expirationDate") ?: Date()

                            if (usedAmount > amount) {
                                val alert = AlertItem(
                                    title = "Budget superato",
                                    message = "Hai superato il budget '$name'."
                                )
                                alertList.add(alert)
                                sendNotification("Budget superato", "Hai superato il budget '$name'.")
                            }

                            if (expirationDate.before(Date())) {
                                val alert = AlertItem(
                                    title = "Budget scaduto",
                                    message = "Il budget '$name' è scaduto."
                                )
                                alertList.add(alert)
                                sendNotification("Budget scaduto", "Il budget '$name' è scaduto.")
                            }

                            alertsAdapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
