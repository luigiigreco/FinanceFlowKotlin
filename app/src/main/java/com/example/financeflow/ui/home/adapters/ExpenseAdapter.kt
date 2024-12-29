package com.example.financeflow.ui.home.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeflow.databinding.ItemExpenseBinding
import com.example.financeflow.ui.home.models.Expense

class ExpenseAdapter(
    private var expenseList: MutableList<Expense>,
    private val onTransactionDeleted: (Expense) -> Unit // Callback per eliminare la transazione
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense)

        // Imposta il colore dell'importo in base al tipo di transazione (entrata/uscita)
        holder.binding.textViewAmount.setTextColor(
            if (expense.isIncome) Color.GREEN else Color.RED
        )

        // Gestione della cancellazione della transazione tramite long click
        holder.binding.root.setOnLongClickListener {
            onTransactionDeleted(expense)  // Chiama il callback per eliminare la transazione
            true
        }
    }

    override fun getItemCount(): Int = expenseList.size

    fun updateExpenses(expenses: List<Expense>) {
        this.expenseList = expenses.toMutableList()
        notifyDataSetChanged()
    }

    fun addExpenseAtTop(expense: Expense) {
        this.expenseList.add(0, expense) // Aggiungi in cima
        notifyItemInserted(0)
    }

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.textViewCategory.text = expense.category
            binding.textViewAmount.text = if (expense.isIncome) "+€${expense.amount}" else "-€${expense.amount}"

            // Formatta la data
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val dateFormatted = dateFormat.format(expense.date)
            binding.textViewDate.text = dateFormatted
        }
    }
}
