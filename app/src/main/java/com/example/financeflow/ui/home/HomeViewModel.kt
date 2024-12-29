package com.example.financeflow.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.financeflow.ui.home.models.Expense

class HomeViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<Expense>>().apply {
        value = mutableListOf()  // Inizializza con una lista vuota
    }
    val expenseList: LiveData<List<Expense>> = _expenseList

    // Aggiungi la funzione per eliminare una transazione
    fun removeExpense(expense: Expense) {
        val currentList = _expenseList.value?.toMutableList() ?: mutableListOf()
        currentList.remove(expense) // Rimuovi l'elemento
        _expenseList.value = currentList  // Aggiorna LiveData con la nuova lista
    }

    fun addExpense(expense: Expense) {
        val currentList = _expenseList.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, expense) // Aggiungi in cima
        _expenseList.value = currentList // Aggiorna LiveData con la nuova lista
    }

    fun setExpenses(expenses: List<Expense>) {
        _expenseList.value = expenses // Imposta una nuova lista direttamente
    }
}
