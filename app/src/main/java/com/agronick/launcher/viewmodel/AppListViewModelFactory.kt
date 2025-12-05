package com.agronick.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agronick.launcher.AppListProvider

/**
 * Factory for creating AppListViewModel instances
 * Required because ViewModel needs AppListProvider dependency
 */
class AppListViewModelFactory(
    private val appListProvider: AppListProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
            return AppListViewModel(appListProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Convenience function to create factory
 * Usage:
 * ```
 * val viewModel: AppListViewModel by viewModels {
 *     createAppListViewModelFactory(appListProvider)
 * }
 * ```
 */
fun createAppListViewModelFactory(
    appListProvider: AppListProvider
): ViewModelProvider.Factory {
    return AppListViewModelFactory(appListProvider)
}

