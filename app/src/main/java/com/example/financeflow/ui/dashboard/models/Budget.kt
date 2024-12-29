package com.example.financeflow.ui.dashboard.models

import java.util.Date

data class Budget(
    val name: String = "",          // Imposta un valore predefinito
    val amount: Double = 0.0,       // Imposta un valore predefinito
    val category: String = "",      // Imposta un valore predefinito
    val currency: String = "",      // Imposta un valore predefinito
    val creationDate: Date = Date(),    // Imposta un valore predefinito (data corrente)
    val expirationDate: Date = Date(),  // Imposta un valore predefinito (data corrente)
    val usedAmount: Double = 0.0     // Imposta un valore predefinito
) {
    // Metodo toMap() per convertire l'oggetto Budget in una mappa
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "amount" to amount,
            "category" to category,
            "currency" to currency,
            "creationDate" to creationDate,
            "expirationDate" to expirationDate,
            "usedAmount" to usedAmount
        )
    }

    // Costruttore vuoto richiesto da Firestore
    constructor() : this("", 0.0, "", "", Date(), Date(), 0.0)
}
