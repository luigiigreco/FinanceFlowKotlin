package com.example.financeflow.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeflow.LoginActivity
import com.example.financeflow.R
import com.example.financeflow.databinding.FragmentHomeBinding
import com.example.financeflow.ui.dashboard.models.Budget
import com.example.financeflow.ui.home.adapters.ExpenseAdapter
import com.example.financeflow.ui.home.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var totalBalance: Double = 0.0
    private lateinit var adapter: ExpenseAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inizializzazione Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Inizializza l'adapter per gestire l'eliminazione tramite il ViewModel
        adapter = ExpenseAdapter(mutableListOf()) { expenseToDelete ->
            showDeleteConfirmationDialog(expenseToDelete)  // Mostra il dialogo di conferma eliminazione
        }

        val recyclerView = binding.recyclerViewExpenses
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Osserva i cambiamenti nella lista delle transazioni
        homeViewModel.expenseList.observe(viewLifecycleOwner) { expenses ->
            adapter.updateExpenses(expenses)
            updateTotalBalance()  // Aggiorna il saldo ogni volta che la lista cambia
        }

        // Carica le transazioni salvate da Firestore
        loadTransactionsFromFirestore()

        // Setup FloatingActionButton per aggiungere una nuova transazione
        binding.fabAddExpense.setOnClickListener {
            showAddTransactionDialog()
        }

        return root
    }

    // Funzione per aggiornare il budget con la transazione
    private fun updateBudgetWithTransaction(transaction: Expense) {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val budget = document.toObject(Budget::class.java)

                        // Verifica se la transazione è successiva alla creazione del budget
                        // e verifica che la transazione sia una spesa (non un'entrata)
                        if ((budget.category == "Tutte le Spese" || budget.category == transaction.category)
                            && transaction.date.after(budget.creationDate)
                            && !transaction.isIncome) {  // Aggiunta condizione per controllare se è una spesa

                            // Aggiorna il `usedAmount` solo se la transazione è una spesa
                            val newUsedAmount = budget.usedAmount + transaction.amount
                            firestore.collection("users")
                                .document(auth.currentUser!!.uid)
                                .collection("budgets")
                                .document(document.id)
                                .update("usedAmount", newUsedAmount)
                        }
                    }
                }
        }
    }



    // Funzione per mostrare il dialogo di eliminazione
    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Elimina Transazione")
            .setMessage("Sei sicuro di voler eliminare questa transazione?")
            .setPositiveButton("Elimina") { _, _ ->
                deleteTransaction(expense)  // Elimina la transazione da Firestore e dal ViewModel
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    // Funzione per eliminare una transazione da Firestore e dal ViewModel
    private fun deleteTransaction(expense: Expense) {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("transactions")
                .whereEqualTo("category", expense.category)
                .whereEqualTo("amount", expense.amount)
                .whereEqualTo("isIncome", expense.isIncome)
                .whereEqualTo("date", expense.date)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("users")
                            .document(user.uid)
                            .collection("transactions")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                // Rimuovi la transazione dal ViewModel (che aggiorna anche l'adapter)
                                homeViewModel.removeExpense(expense)
                                updateBudgetAfterTransactionDeletion(expense)
                                // Aggiorna il saldo subito dopo l'eliminazione
                                updateTotalBalance()
                                Toast.makeText(context, "Transazione eliminata", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Errore nell'eliminazione: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    // Funzione per aggiornare il budget dopo l'eliminazione della transazione
    private fun updateBudgetAfterTransactionDeletion(transaction: Expense) {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("budgets")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val budget = document.toObject(Budget::class.java)

                        // Controlla se la transazione è una spesa (non un'entrata)
                        if (!transaction.isIncome && (budget.category == "Tutte le Spese" || budget.category == transaction.category)) {

                            // Decrementa il `usedAmount`, ma non andare sotto 0
                            val newUsedAmount = (budget.usedAmount - transaction.amount).coerceAtLeast(0.0)
                            firestore.collection("users")
                                .document(user.uid)
                                .collection("budgets")
                                .document(document.id)
                                .update("usedAmount", newUsedAmount)
                        }
                    }
                }
        }
    }



    // Funzione per caricare tutte le transazioni salvate da Firestore
    private fun loadTransactionsFromFirestore() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users")
                .document(user.uid)
                .collection("transactions")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val expenses = mutableListOf<Expense>()
                    for (document in documents) {
                        val category = document.getString("category") ?: ""
                        val amount = document.getDouble("amount") ?: 0.0
                        val isIncome = document.getBoolean("isIncome") ?: false
                        val date = document.getDate("date") ?: Date()

                        val expense = Expense(category, amount, isIncome, date)

                        // Aggiungi tutte le transazioni, sia entrate che uscite
                        expenses.add(expense)
                    }

                    // Aggiorna il ViewModel con tutte le transazioni caricate (sia entrate che spese)
                    homeViewModel.setExpenses(expenses)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Errore nel caricamento dei dati: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    // Funzione per aggiornare il saldo totale
    private fun updateTotalBalance() {
        totalBalance = homeViewModel.expenseList.value?.sumByDouble { if (it.isIncome) it.amount else -it.amount } ?: 0.0
        binding.textViewTotalBalance.text = "Saldo Totale: €$totalBalance"
    }

    // Mostra la dialog per aggiungere una nuova transazione
    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        // Dialog Views
        val amountEditText = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val transactionTypeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupTransactionType)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val okButton = dialogView.findViewById<Button>(R.id.buttonOk)

        // Setup Spinner con le categorie (tags)
        val categories = resources.getStringArray(R.array.budget_category_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categorySpinner.adapter = adapter

        okButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            if (amount != null) {
                val isIncome = transactionTypeRadioGroup.checkedRadioButtonId == R.id.radioButtonIncome
                val category = categorySpinner.selectedItem.toString()
                addNewTransaction(amount, category, isIncome)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Inserisci un importo valido", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // Funzione per aggiungere una nuova transazione
    private fun addNewTransaction(amount: Double, category: String, isIncome: Boolean) {
        val currentDate = Date()
        val newExpense = Expense(category, amount, isIncome, currentDate)
        homeViewModel.addExpense(newExpense)
        saveTransactionToFirestore(newExpense)
        updateBudgetWithTransaction(newExpense)
        updateTotalBalance()
    }

    // Salva la transazione su Firestore
    private fun saveTransactionToFirestore(expense: Expense) {
        val user = auth.currentUser
        user?.let {
            val transactionData = hashMapOf(
                "category" to expense.category,
                "amount" to expense.amount,
                "isIncome" to expense.isIncome,
                "date" to expense.date
            )

            firestore.collection("users")
                .document(user.uid)
                .collection("transactions")
                .add(transactionData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Transazione salvata", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Errore nel salvataggio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
