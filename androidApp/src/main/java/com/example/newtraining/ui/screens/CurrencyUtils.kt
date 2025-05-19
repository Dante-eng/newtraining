package com.example.newtraining.ui.screens

import java.text.NumberFormat
import java.util.Locale
 
fun formatCurrency(value: Double): String {
    val nf = NumberFormat.getIntegerInstance(Locale.US)
    return nf.format(value)
} 