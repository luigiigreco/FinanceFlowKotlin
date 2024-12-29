package com.example.financeflow

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.financeflow.ui.dashboard.DashboardViewModel
import com.example.financeflow.ui.dashboard.models.Budget
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE) // SDK 28 per compatibilità Robolectric
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Per LiveData

    private val viewModel = DashboardViewModel()

    @Test
    fun addBudget_addsBudgetToList() {
        val observer = createObserver<List<Budget>>()
        viewModel.budgetList.observeForever(observer)

        val budget = Budget(
            name = "Shopping",
            amount = 100.0,
            category = "Spese Varie",
            currency = "EUR",
            creationDate = Date(),
            expirationDate = Date(),
            usedAmount = 0.0
        )

        viewModel.addBudget(budget)
        val currentList = viewModel.budgetList.value

        assertEquals(1, currentList?.size)
        assertEquals(budget, currentList?.get(0))
    }

    @Test
    fun removeBudget_removesBudgetFromList() {
        val observer = createObserver<List<Budget>>()
        viewModel.budgetList.observeForever(observer)

        val budget1 = Budget("Shopping", 100.0, "Spese Varie", "EUR", Date(), Date(), 0.0)
        val budget2 = Budget("Affitto", 500.0, "Casa", "EUR", Date(), Date(), 0.0)

        viewModel.setBudgets(listOf(budget1, budget2))
        viewModel.removeBudget(budget1)

        val currentList = viewModel.budgetList.value
        assertEquals(1, currentList?.size)
        assertEquals(budget2, currentList?.get(0))
    }

    @Test
    fun setBudgets_replacesBudgetList() {
        val observer = createObserver<List<Budget>>()
        viewModel.budgetList.observeForever(observer)

        val budgets = listOf(
            Budget("Shopping", 100.0, "Spese Varie", "EUR", Date(), Date(), 0.0),
            Budget("Affitto", 500.0, "Casa", "EUR", Date(), Date(), 0.0)
        )

        viewModel.setBudgets(budgets)

        val currentList = viewModel.budgetList.value
        assertEquals(budgets.size, currentList?.size)
        assertEquals(budgets, currentList)
    }

    @Test
    fun updateBudget_updatesBudgetInList() {
        val observer = createObserver<List<Budget>>()
        viewModel.budgetList.observeForever(observer)

        val budget = Budget("Shopping", 100.0, "Spese Varie", "EUR", Date(), Date(), 0.0)
        viewModel.addBudget(budget)

        val updatedBudget = budget.copy(usedAmount = 50.0)
        viewModel.updateBudget(updatedBudget)

        val currentList = viewModel.budgetList.value
        assertEquals(1, currentList?.size)
        assertEquals(updatedBudget, currentList?.get(0))
    }

    @Test
    fun checkBudgetStatus_generatesAlertsForExceededOrExpiredBudgets() {
        val observer = createObserver<List<String>>()
        viewModel.budgetAlerts.observeForever(observer)

        val exceededBudget = Budget("Shopping", 100.0, "Spese Varie", "EUR", Date(), Date(), 150.0)
        val expiredBudget = Budget(
            "Affitto",
            500.0,
            "Casa",
            "EUR",
            Date(),
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time,
            400.0
        )

        viewModel.addBudget(exceededBudget)
        viewModel.addBudget(expiredBudget)

        val alerts = viewModel.budgetAlerts.value
        assertEquals(2, alerts?.size)
        assert(alerts?.any { it.contains("superato il limite") } == true)
        assert(alerts?.any { it.contains("scaduto il") } == true)
    }

    private fun <T> createObserver(): Observer<T> = Observer { }
}
