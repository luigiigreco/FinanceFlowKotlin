package com.example.financeflow.ui.dashboard

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeflow.R
import com.example.financeflow.databinding.FragmentDashboardBinding
import com.example.financeflow.ui.dashboard.adapters.BudgetAdapter
import com.example.financeflow.ui.dashboard.models.Budget
import com.example.financeflow.ui.home.models.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BudgetAdapter
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Imposta l'adapter per il RecyclerView e gestisci l'eliminazione del budget tramite long click
        adapter = BudgetAdapter(mutableListOf()) { budgetToDelete ->
            AlertDialog.Builder(requireContext())
                .setTitle("Elimina Budget")
                .setMessage("Sei sicuro di voler eliminare il budget?")
                .setPositiveButton("Elimina") { _, _ ->
                    deleteBudget(budgetToDelete)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        binding.recyclerViewBudgets.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewBudgets.adapter = adapter

        // Setup del BarChart
        barChart = binding.barChart
        setupBarChart()

        // Carica i budget esistenti
        loadBudgetsFromFirestore()

        // Carica le transazioni per il grafico
        loadTransactionsForChart()

        // Setup pulsante per aggiungere un nuovo budget
        binding.buttonAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }

        return root
    }

    private fun addNewBudget(name: String, amount: Double, currency: String, category: String, period: String) {
        val currentDate = Date()
        val expirationDate = calculateExpirationDate(period)

        val newBudget = Budget(name, amount, category, currency, currentDate, expirationDate)
        val user = auth.currentUser

        // Aggiorna il ViewModel localmente
        dashboardViewModel.addBudget(newBudget)
        adapter.addBudget(newBudget) // Aggiorna subito l'Adapter per visualizzarlo

        // Salva su Firestore
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .add(newBudget.toMap())
                .addOnSuccessListener {
                    // Notifica che è stato salvato con successo (se necessario)
                    Log.d("Firestore", "Budget aggiunto correttamente!")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Errore nell'aggiungere il budget: ${e.message}")
                    // Rimuovi il budget dall'Adapter in caso di errore
                    dashboardViewModel.removeBudget(newBudget)
                    adapter.removeBudget(newBudget)
                }
        }
    }


    private fun calculateExpirationDate(period: String): Date {
        val calendar = Calendar.getInstance()
        when (period) {
            "Settimanale" -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            "Mensile" -> calendar.add(Calendar.MONTH, 1)
            "Annuale" -> calendar.add(Calendar.YEAR, 1)
        }
        return calendar.time
    }

    private fun loadBudgetsFromFirestore() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val budgets = mutableListOf<Budget>()
                        for (document in snapshots.documents) {
                            val name = document.getString("name") ?: ""
                            val amount = document.getDouble("amount") ?: 0.0
                            val category = document.getString("category") ?: ""
                            val currency = document.getString("currency") ?: ""
                            val creationDate = document.getDate("creationDate") ?: Date()
                            val expirationDate = document.getDate("expirationDate") ?: Date()
                            val usedAmount = document.getDouble("usedAmount") ?: 0.0

                            // Formatta le date per la visualizzazione
                            val budget = Budget(
                                name = name,
                                amount = amount,
                                category = category,
                                currency = currency,
                                creationDate = dateFormatter.parse(dateFormatter.format(creationDate)),
                                expirationDate = dateFormatter.parse(dateFormatter.format(expirationDate)),
                                usedAmount = usedAmount
                            )
                            budgets.add(budget)
                        }
                        adapter.updateBudgets(budgets)
                    }
                }
        }
    }

    private fun showAddBudgetDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_budget, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextBudgetName)
        val amountEditText = dialogView.findViewById<EditText>(R.id.editTextBudgetAmount)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinnerBudgetCategory)
        val currencySpinner = dialogView.findViewById<Spinner>(R.id.spinnerBudgetCurrency)
        val frequencySpinner = dialogView.findViewById<Spinner>(R.id.spinnerBudgetFrequency)
        val okButton = dialogView.findViewById<Button>(R.id.buttonOk)

        // Recupera gli array di valori da strings.xml
        val categories = resources.getStringArray(R.array.budget_category_array)
        val currencies = resources.getStringArray(R.array.currency_array)
        val frequencies = resources.getStringArray(R.array.budget_frequency_array)

        // Imposta gli adapter per i vari Spinner
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        currencySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        frequencySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)

        okButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val amount = amountEditText.text.toString().toDoubleOrNull()
            val currency = currencySpinner.selectedItem?.toString() ?: ""
            val category = categorySpinner.selectedItem?.toString() ?: ""
            val frequency = frequencySpinner.selectedItem?.toString() ?: ""

            if (name.isNotEmpty() && amount != null && currency.isNotEmpty() && category.isNotEmpty()) {
                // Chiama la funzione per aggiungere il nuovo budget
                addNewBudget(name, amount, currency, category, frequency)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
    }

    // Funzione per eliminare un budget
    private fun deleteBudget(budget: Budget) {
        // Rimuovi immediatamente il budget dall'UI
        adapter.removeBudget(budget)

        // Mostra un messaggio di feedback all'utente
        Toast.makeText(context, "Eliminazione in corso...", Toast.LENGTH_SHORT).show()

        // Procedi con l'eliminazione da Firestore
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .whereEqualTo("name", budget.name)
                .whereEqualTo("amount", budget.amount)
                .whereEqualTo("currency", budget.currency)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("users")
                            .document(user.uid)
                            .collection("budgets")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Budget eliminato con successo!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // In caso di errore, aggiungi nuovamente il budget nell'adapter
                                adapter.addBudget(budget)
                                Toast.makeText(context, "Errore nell'eliminazione: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Gestisci il caso in cui il recupero dei documenti fallisce
                    adapter.addBudget(budget) // Reintegra il budget nella UI
                    Toast.makeText(context, "Errore nel recupero: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Funzione per caricare le transazioni e aggiornare il grafico
    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadTransactionsForChart() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("DashboardFragment", "loadTransactionsForChart: Utente non autenticato")
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .collection("transactions")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DashboardFragment", "Errore nel caricamento delle transazioni: ${e.message}")
                    Toast.makeText(context, "Errore nel caricamento delle transazioni: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val transactions = mutableListOf<Expense>()
                    for (document in snapshot.documents) {
                        val category = document.getString("category") ?: ""
                        val amount = document.getDouble("amount") ?: 0.0
                        val isIncome = document.getBoolean("isIncome") ?: false
                        val date = document.getDate("date") ?: Date()

                        val expense = Expense(category, amount, isIncome, date)
                        transactions.add(expense)
                    }
                    updateChart(transactions)
                } else {
                    updateChart(emptyList())
                }
            }
    }

    // Funzione per aggiornare il grafico
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateChart(transactions: List<Expense>) {
        val entriesIncome = ArrayList<BarEntry>()
        val entriesExpense = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val categoryMap = mutableMapOf<String, Pair<Double, Double>>()

        for (transaction in transactions) {
            val currentCategory = transaction.category
            val currentAmount = transaction.amount
            val isIncome = transaction.isIncome

            val currentSums = categoryMap.getOrDefault(currentCategory, Pair(0.0, 0.0))

            if (isIncome) {
                categoryMap[currentCategory] = Pair(currentSums.first + currentAmount, currentSums.second)
            } else {
                categoryMap[currentCategory] = Pair(currentSums.first, currentSums.second + currentAmount)
            }
        }

        var i = 0f
        for ((category, sums) in categoryMap) {
            val incomeSum = sums.first.toFloat()
            val expenseSum = sums.second.toFloat()

            entriesIncome.add(BarEntry(i, incomeSum))
            entriesExpense.add(BarEntry(i + 0.3f, expenseSum))
            labels.add(category)

            i += 1f
        }

        val incomeDataSet = BarDataSet(entriesIncome, "Entrate").apply {
            color = Color.GREEN
        }

        val expenseDataSet = BarDataSet(entriesExpense, "Uscite").apply {
            color = Color.RED
        }

        val data = BarData(incomeDataSet, expenseDataSet)
        data.barWidth = 0.25f

        barChart.data = data

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setLabelCount(labels.size)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        barChart.groupBars(0f, 0.2f, 0.05f)
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
