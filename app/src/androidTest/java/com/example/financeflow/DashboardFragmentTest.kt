package com.example.financeflow

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.financeflow.ui.dashboard.models.Budget
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.util.Date

class DashboardFragmentTest {

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Lancia MainActivity che contiene il DashboardFragment
        ActivityScenario.launch(MainActivity::class.java)

        // Spostati nel DashboardFragment usando la bottom navigation
        onView(withId(R.id.navigation_dashboard)).perform(click())
    }

    @Test
    fun loadBudgets_displaysBudgetsInRecyclerView() {
        // Verifica che il RecyclerView per i budget sia visibile
        onView(withId(R.id.recyclerViewBudgets)).check(matches(isDisplayed()))
    }

    @Test
    fun addNewBudget_updatesRecyclerViewDirectly() {
        // Crea un nuovo budget
        val mockBudget = Budget(
            name = "Budget Casa",
            amount = 500.0,
            category = "Stipendio",
            currency = "EUR",
            creationDate = Date(),
            expirationDate = Date(),
            usedAmount = 0.0
        )

        // Simula l'aggiunta tramite UI
        onView(withId(R.id.buttonAddBudget)).perform(click())
        onView(withId(R.id.editTextBudgetName)).perform(typeText(mockBudget.name), closeSoftKeyboard())
        onView(withId(R.id.editTextBudgetAmount)).perform(typeText(mockBudget.amount.toString()), closeSoftKeyboard())
        onView(withId(R.id.spinnerBudgetCategory)).perform(click())
        onView(withText(mockBudget.category)).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.spinnerBudgetCurrency)).perform(click())
        onView(withText(mockBudget.currency)).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.spinnerBudgetFrequency)).perform(click())
        onView(withText("Mensile")).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.buttonOk)).perform(click())

        // Aspetta l'aggiornamento del RecyclerView
        Thread.sleep(1000)

        // Verifica che il RecyclerView mostri il nuovo budget
        onView(withId(R.id.recyclerViewBudgets))
            .check(matches(hasDescendant(withText("Budget Casa"))))
    }

    @Test
    fun deleteBudget_removesBudgetAfterConfirmation() {
        // Crea un nuovo budget
        val mockBudget = Budget(
            name = "Budget Casa",
            amount = 500.0,
            category = "Stipendio",
            currency = "EUR",
            creationDate = Date(),
            expirationDate = Date(),
            usedAmount = 0.0
        )

        // Simula l'aggiunta tramite UI
        onView(withId(R.id.buttonAddBudget)).perform(click())
        onView(withId(R.id.editTextBudgetName)).perform(typeText(mockBudget.name), closeSoftKeyboard())
        onView(withId(R.id.editTextBudgetAmount)).perform(typeText(mockBudget.amount.toString()), closeSoftKeyboard())
        onView(withId(R.id.spinnerBudgetCategory)).perform(click())
        onView(withText(mockBudget.category)).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.spinnerBudgetCurrency)).perform(click())
        onView(withText(mockBudget.currency)).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.spinnerBudgetFrequency)).perform(click())
        onView(withText("Mensile")).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.buttonOk)).perform(click())

        // Aspetta l'aggiornamento del RecyclerView
        Thread.sleep(1000)

        // Verifica che il RecyclerView mostri il nuovo budget
        onView(withId(R.id.recyclerViewBudgets))
            .check(matches(hasDescendant(withText("Budget Casa"))))

        // Simula un long click sul budget appena aggiunto
        onView(withText("Budget Casa")).perform(longClick())

        // Verifica che la finestra di dialogo di eliminazione sia visibile
        onView(withText("Sei sicuro di voler eliminare il budget?")).check(matches(isDisplayed()))

        // Simula il clic sul pulsante "Elimina" usando `withId` o `withText`
        onView(allOf(withText("Elimina"), isDisplayed())).perform(click())


        // Aspetta l'aggiornamento del RecyclerView
        Thread.sleep(1000)

        // Verifica che il budget sia stato rimosso dal RecyclerView
        onView(withId(R.id.recyclerViewBudgets))
            .check(matches(not(hasDescendant(withText("Budget Casa")))))
    }

}
