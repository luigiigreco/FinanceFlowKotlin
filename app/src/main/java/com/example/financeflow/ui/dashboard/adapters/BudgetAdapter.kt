package com.example.financeflow.ui.dashboard.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeflow.databinding.ItemBudgetBinding
import com.example.financeflow.ui.dashboard.models.Budget
import java.text.SimpleDateFormat
import java.util.Locale

class BudgetAdapter(
    private var budgetList: MutableList<Budget>,
    private val onBudgetDelete: (Budget) -> Unit // Callback per la cancellazione
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgetList[position]
        holder.bind(budget)

        // Imposta un listener per l'eliminazione del budget tramite long click
        holder.itemView.setOnLongClickListener {
            onBudgetDelete(budget) // Chiama il callback per eliminare il budget
            true
        }
    }

    override fun getItemCount(): Int = budgetList.size

    fun updateBudgets(budgets: List<Budget>) {
        budgetList.clear()
        budgetList.addAll(budgets)
        notifyDataSetChanged()
    }

    fun addBudget(budget: Budget) {
        budgetList.add(0, budget)
        notifyItemInserted(0)
    }

    fun removeBudget(budget: Budget) {
        val position = budgetList.indexOf(budget)
        if (position != -1) {
            budgetList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: Budget) {
            binding.textViewBudgetName.text = budget.name
            binding.textViewBudgetAmount.text = "${budget.amount} ${budget.currency}"
            binding.textViewBudgetTag.text = "Tag: ${budget.category}"

            // Formattazione coerente delle date
            binding.textViewCreationDate.text = "Creato: ${dateFormatter.format(budget.creationDate)}"
            binding.textViewExpirationDate.text = "Scade: ${dateFormatter.format(budget.expirationDate)}"

            // Mostra l'importo usato e modifica il colore se vicino al limite
            binding.textViewBudgetUsed.text = "Usato: €${budget.usedAmount}"
            if (budget.usedAmount > budget.amount) {
                binding.textViewBudgetUsed.setTextColor(Color.RED) // Rosso se ha superato il budget
            } else if (budget.usedAmount > budget.amount * 0.8) {
                binding.textViewBudgetUsed.setTextColor(Color.parseColor("#FFA500")) // Arancione se vicino al limite (80%)
            } else {
                binding.textViewBudgetUsed.setTextColor(Color.BLACK) // Nero se entro i limiti
            }
        }
    }
}
