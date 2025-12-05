package com.agronick.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agronick.launcher.AppListProvider
import com.agronick.launcher.PInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for managing the launcher's app list state
 * Follows modern Android architecture best practices
 */
class AppListViewModel(
    private val appListProvider: AppListProvider
) : ViewModel() {

    // UI State
    private val _apps = MutableStateFlow<List<PInfo>>(emptyList())
    val apps: StateFlow<List<PInfo>> = _apps.asStateFlow()

    private val _iconSize = MutableStateFlow(24)
    val iconSize: StateFlow<Int> = _iconSize.asStateFlow()

    private val _margin = MutableStateFlow(1)
    val margin: StateFlow<Int> = _margin.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _offsetX = MutableStateFlow(0f)
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(0f)
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    init {
        loadApps()
    }

    /**
     * Load installed applications
     */
    private fun loadApps() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                appListProvider.load()
                _apps.value = appListProvider.getPkgList()
                Timber.d("Loaded ${_apps.value.size} apps")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load apps")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update icon size based on rotary input
     * @param delta Change in size (positive or negative)
     */
    fun updateIconSize(delta: Int) {
        val newSize = (_iconSize.value + delta).coerceIn(12, 24)
        if (newSize != _iconSize.value) {
            _iconSize.value = newSize
            Timber.d("Icon size updated to $newSize")
            // TODO: Save to DataStore
        }
    }

    /**
     * Update margin between icons
     * @param delta Change in margin (positive or negative)
     */
    fun updateMargin(delta: Int) {
        val newMargin = (_margin.value + delta).coerceIn(0, 5)
        if (newMargin != _margin.value) {
            _margin.value = newMargin
            Timber.d("Margin updated to $newMargin")
            // TODO: Save to DataStore
        }
    }

    /**
     * Update viewport offset (panning)
     */
    fun updateOffset(x: Float, y: Float) {
        _offsetX.value = x
        _offsetY.value = y
    }

    /**
     * Reorder apps by swapping positions
     * @param from Source index
     * @param to Destination index
     */
    fun reorderApp(from: Int, to: Int) {
        viewModelScope.launch {
            val currentList = _apps.value.toMutableList()
            if (from in currentList.indices && to in currentList.indices) {
                val item = currentList.removeAt(from)
                currentList.add(to, item)
                _apps.value = currentList

                // Save new order
                appListProvider.savePkgOrder(currentList)
                Timber.d("Reordered app from $from to $to")
            }
        }
    }

    /**
     * Reload apps (useful for when new apps are installed)
     */
    fun refreshApps() {
        loadApps()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel cleared")
    }
}

