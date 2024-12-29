package com.example.financeflow

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.financeflow.ui.home.HomeViewModel
import com.example.financeflow.ui.home.models.Expense
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import java.util.Date

// Usa Robolectric come test runner
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE) // Specifica la versione SDK simulata
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Per LiveData

    private val viewModel = HomeViewModel()

    @Test
    fun addExpense_addsExpenseToList() {
        val observer = createObserver<List<Expense>>()
        viewModel.expenseList.observeForever(observer)

        val expense = Expense("Shopping", 50.0, false, Date())
        viewModel.addExpense(expense)

        val currentList = viewModel.expenseList.value
        assertEquals(1, currentList?.size)
        assertEquals(expense, currentList?.get(0))
    }

    @Test
    fun removeExpense_removesExpenseFromList() {
        val observer = createObserver<List<Expense>>()
        viewModel.expenseList.observeForever(observer)

        val expense1 = Expense("Shopping", 50.0, false, Date())
        val expense2 = Expense("Groceries", 30.0, false, Date())

        viewModel.setExpenses(listOf(expense1, expense2))
        viewModel.removeExpense(expense1)

        val currentList = viewModel.expenseList.value
        assertEquals(1, currentList?.size)
        assertEquals(expense2, currentList?.get(0))
    }

    @Test
    fun setExpenses_replacesExpenseList() {
        val observer = createObserver<List<Expense>>()
        viewModel.expenseList.observeForever(observer)

        val newList = listOf(
            Expense("Shopping", 50.0, false, Date()),
            Expense("Stipendio", 500.0, true, Date())
        )

        viewModel.setExpenses(newList)

        val currentList = viewModel.expenseList.value
        assertEquals(newList.size, currentList?.size)
        assertEquals(newList, currentList)
    }

    // Helper per creare un Observer
    private fun <T> createObserver(): Observer<T> = Observer { }
}
