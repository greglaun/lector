package com.greglaun.lector.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.greglaun.lector.data.net.InternetChecker





class AndroidInternetChecker(val context: Context): InternetChecker {
    override fun internetIsAvailable(): Boolean {
        val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

}