package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.TransactionType

data class CategoryItem(
  val name: String,
  val icon: ImageVector,
  val color: Color,
  val defaultType: TransactionType
)

object CategoryRegistry {
  val defaultCategories = listOf(
    CategoryItem("Belanja", Icons.Default.ShoppingCart, Color(0xFF8B5CF6), TransactionType.EXPENSE),
    CategoryItem("Pendidikan", Icons.Default.School, Color(0xFF3B82F6), TransactionType.EXPENSE),
    CategoryItem("Makanan & Minuman", Icons.Default.Restaurant, Color(0xFFF97316), TransactionType.EXPENSE),
    CategoryItem("Transportasi", Icons.Default.DirectionsCar, Color(0xFF06B6D4), TransactionType.EXPENSE),
    CategoryItem("Tagihan & Utilitas", Icons.Default.Receipt, Color(0xFFEAB308), TransactionType.EXPENSE),
    CategoryItem("Hiburan", Icons.Default.SportsEsports, Color(0xFFEC4899), TransactionType.EXPENSE),
    CategoryItem("Kesehatan", Icons.Default.LocalHospital, Color(0xFFEF4444), TransactionType.EXPENSE),
    CategoryItem("Tempat Tinggal", Icons.Default.Home, Color(0xFF14B8A6), TransactionType.EXPENSE),
    CategoryItem("Gaji", Icons.Default.Payments, Color(0xFF10B981), TransactionType.INCOME),
    CategoryItem("Investasi", Icons.Default.TrendingUp, Color(0xFF059669), TransactionType.INCOME),
    CategoryItem("Usaha / Bisnis", Icons.Default.Storefront, Color(0xFF0284C7), TransactionType.INCOME),
    CategoryItem("Lainnya", Icons.Default.Category, Color(0xFF64748B), TransactionType.EXPENSE)
  )

  fun getCategory(name: String): CategoryItem {
    return defaultCategories.firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: CategoryItem(name, Icons.Default.Category, Color(0xFF64748B), TransactionType.EXPENSE)
  }
}
