package com.example.financeflow

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test

class HomeFragmentTest {

    @Before
    fun setup() {
        // Lancia MainActivity che contiene HomeFragment
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun loadTransactions_displaysTransactionsInRecyclerView() {
        // Verifica che il RecyclerView sia visibile
        onView(withId(R.id.recyclerViewExpenses)).check(matches(isDisplayed()))
    }

    @Test
    fun addNewTransaction_updatesRecyclerViewAndTotalBalance() {
        // Simula il clic sul pulsante FAB
        onView(withId(R.id.fabAddExpense)).perform(click())

        // Aspetta che la dialog sia visibile
        onView(withId(R.id.editTextAmount)).check(matches(isDisplayed()))

        // Inserisci i dati nella dialog
        onView(withId(R.id.editTextAmount)).perform(typeText("100"), closeSoftKeyboard())
        onView(withId(R.id.radioButtonExpense)).perform(click())
        onView(withId(R.id.spinnerCategory)).perform(click())

        // Interagisci con lo Spinner per selezionare la categoria
        onView(withText("Stipendio"))
            .inRoot(isPlatformPopup())
            .perform(click())

        onView(withId(R.id.buttonOk)).perform(click())

        // Verifica che il RecyclerView mostri la nuova transazione
        onView(withId(R.id.recyclerViewExpenses))
            .check(matches(hasDescendant(withText("Stipendio"))))

        // Verifica che il saldo totale sia aggiornato
        onView(withId(R.id.textViewTotalBalance))
            .check(matches(withText("Saldo Totale: €-100.0")))
    }

    @Test
    fun deleteTransaction_removesTransactionFromRecyclerView() {
        // Aggiungi una transazione per garantire la presenza di "Stipendio"
        onView(withId(R.id.fabAddExpense)).perform(click())
        onView(withId(R.id.editTextAmount)).perform(typeText("100"), closeSoftKeyboard())
        onView(withId(R.id.radioButtonExpense)).perform(click())
        onView(withId(R.id.spinnerCategory)).perform(click())
        onView(withText("Stipendio"))
            .inRoot(isPlatformPopup())
            .perform(click())
        onView(withId(R.id.buttonOk)).perform(click())

        // Simula un long click sulla transazione per aprire il dialogo
        onView(withText("Stipendio")).perform(longClick())

        // Simula il clic su "Elimina"
        onView(withText("Elimina")).perform(click())

        // Aspetta che il RecyclerView venga aggiornato usando IdlingResource
        val recyclerView = ActivityScenario.launch(MainActivity::class.java)
            .onActivity {
                it.findViewById<RecyclerView>(R.id.recyclerViewExpenses)
            }

        // Verifica che la transazione non sia più visibile
        onView(withId(R.id.recyclerViewExpenses))
            .check(matches(not(hasDescendant(withText("Stipendio")))))
    }
}
