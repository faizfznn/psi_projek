package com.kelompok2.scarla.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.kelompok2.scarla.ui.screens.cari.CariFriendRoute

@Composable
fun CariScreen(navController: NavController? = null) {
    CariFriendRoute(navController = navController)
}