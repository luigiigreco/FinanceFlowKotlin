package com.example.financeflow.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financeflow.ui.dashboard.models.Budget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // LiveData che contiene la lista dei budget
    private val _budgetList = MutableLiveData<List<Budget>>().apply {
        value = mutableListOf()  // Inizialmente la lista è vuota
    }
    val budgetList: LiveData<List<Budget>> = _budgetList

    // LiveData per gestire gli avvisi di budget superato o scaduto
    private val _budgetAlerts = MutableLiveData<List<String>>()
    val budgetAlerts: LiveData<List<String>> = _budgetAlerts

    // Funzione per aggiungere un nuovo budget
    fun addBudget(budget: Budget) {
        val currentList = _budgetList.value?.toMutableList() ?: mutableListOf()
        currentList.add(budget)
        _budgetList.value = currentList
        checkBudgetStatus(budget)  // Controlla subito lo stato del budget
    }

    // Funzione per rimuovere un budget
    fun removeBudget(budget: Budget) {
        val currentList = _budgetList.value?.toMutableList() ?: mutableListOf()
        currentList.remove(budget)
        _budgetList.value = currentList
    }

    // Funzione per aggiornare l'importo del budget (per il decremento delle spese)
    fun updateBudget(budget: Budget) {
        val currentList = _budgetList.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.name == budget.name }
        if (index != -1) {
            currentList[index] = budget
            _budgetList.value = currentList
            checkBudgetStatus(budget)  // Verifica se il budget è stato superato dopo l'aggiornamento
        }
    }

    // Funzione per impostare l'intera lista di budget (utile per caricare da database)
    fun setBudgets(budgets: List<Budget>) {
        _budgetList.value = budgets
        budgets.forEach { checkBudgetStatus(it) }  // Controlla lo stato per tutti i budget
    }

    // Funzione per formattare le date dei budget per visualizzazione coerente
    private fun formatBudgetDates(budget: Budget): Budget {
        return budget.copy(
            creationDate = dateFormatter.parse(dateFormatter.format(budget.creationDate)),
            expirationDate = dateFormatter.parse(dateFormatter.format(budget.expirationDate))
        )
    }

    // Funzione per controllare se il budget è scaduto o superato e generare avvisi
    private fun checkBudgetStatus(budget: Budget) {
        val alerts = _budgetAlerts.value?.toMutableList() ?: mutableListOf()

        // Controllo per la scadenza del budget
        val currentDate = Date()
        if (budget.expirationDate.before(currentDate)) {
            alerts.add("Il budget '${budget.name}' è scaduto il ${dateFormatter.format(budget.expirationDate)}.")
        }

        // Controllo se il budget è stato superato
        if (budget.usedAmount > budget.amount) {
            alerts.add("Il budget '${budget.name}' ha superato il limite di ${budget.amount} ${budget.currency}.")
        }

        _budgetAlerts.value = alerts
    }
}
