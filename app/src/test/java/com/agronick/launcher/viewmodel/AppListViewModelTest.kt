package com.agronick.launcher.viewmodel

import app.cash.turbine.test
import com.agronick.launcher.AppListProvider
import com.agronick.launcher.PInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AppListViewModel
 * Tests state management and business logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AppListViewModel
    private lateinit var mockAppListProvider: AppListProvider

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockAppListProvider = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns emptyList()

        // When
        viewModel = AppListViewModel(mockAppListProvider)

        // Then
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `loads apps successfully`() = runTest {
        // Given
        val testApps = listOf(
            PInfo("App1", "com.test.app1", null, "MainActivity"),
            PInfo("App2", "com.test.app2", null, "MainActivity")
        )
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns testApps

        // When
        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // Then
        viewModel.apps.test {
            val apps = awaitItem()
            assertEquals(2, apps.size)
            assertEquals("App1", apps[0].appname)
            assertEquals("App2", apps[1].appname)
        }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `icon size updates within valid range`() = runTest {
        // Given
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns emptyList()
        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // When - Try to increase beyond max
        viewModel.updateIconSize(delta = 10)

        // Then - Should clamp to 24
        assertEquals(24, viewModel.iconSize.value)

        // When - Try to decrease below min
        viewModel.updateIconSize(delta = -20)

        // Then - Should clamp to 12
        assertEquals(12, viewModel.iconSize.value)
    }

    @Test
    fun `icon size does not update if already at boundary`() = runTest {
        // Given
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns emptyList()
        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        val initialSize = viewModel.iconSize.value

        // When - Try to increase when already at max (24)
        viewModel.updateIconSize(delta = 5)

        // Then - Should remain at max
        assertEquals(24, viewModel.iconSize.value)
    }

    @Test
    fun `margin updates within valid range`() = runTest {
        // Given
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns emptyList()
        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // When
        viewModel.updateMargin(delta = 2)

        // Then
        assertEquals(3, viewModel.margin.value)

        // When - Try to go beyond max
        viewModel.updateMargin(delta = 10)

        // Then - Should clamp to 5
        assertEquals(5, viewModel.margin.value)

        // When - Try to go below min
        viewModel.updateMargin(delta = -10)

        // Then - Should clamp to 0
        assertEquals(0, viewModel.margin.value)
    }

    @Test
    fun `reorder swaps apps correctly`() = runTest {
        // Given
        val app1 = PInfo("App1", "com.test.app1", null, "MainActivity")
        val app2 = PInfo("App2", "com.test.app2", null, "MainActivity")
        val app3 = PInfo("App3", "com.test.app3", null, "MainActivity")
        val testApps = listOf(app1, app2, app3)

        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns testApps
        coEvery { mockAppListProvider.savePkgOrder(any()) } coAnswers { }

        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // When - Move first app to last position
        viewModel.reorderApp(from = 0, to = 2)
        advanceUntilIdle()

        // Then
        viewModel.apps.test {
            val apps = awaitItem()
            assertEquals("App2", apps[0].appname)
            assertEquals("App3", apps[1].appname)
            assertEquals("App1", apps[2].appname)
        }
        
        coVerify { mockAppListProvider.savePkgOrder(any()) }
    }

    @Test
    fun `reorder handles invalid indices gracefully`() = runTest {
        // Given
        val testApps = listOf(
            PInfo("App1", "com.test.app1", null, "MainActivity")
        )
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns testApps

        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        val initialApps = viewModel.apps.value

        // When - Try to reorder with invalid index
        viewModel.reorderApp(from = 0, to = 10)
        advanceUntilIdle()

        // Then - Apps should remain unchanged
        assertEquals(initialApps, viewModel.apps.value)
    }

    @Test
    fun `offset updates correctly`() = runTest {
        // Given
        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns emptyList()
        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // When
        viewModel.updateOffset(x = 100f, y = 200f)

        // Then
        assertEquals(100f, viewModel.offsetX.value)
        assertEquals(200f, viewModel.offsetY.value)
    }

    @Test
    fun `refresh apps reloads data`() = runTest {
        // Given
        val initialApps = listOf(PInfo("App1", "com.test.app1", null, "MainActivity"))
        val updatedApps = listOf(
            PInfo("App1", "com.test.app1", null, "MainActivity"),
            PInfo("App2", "com.test.app2", null, "MainActivity")
        )

        coEvery { mockAppListProvider.load() } coAnswers { }
        every { mockAppListProvider.getPkgList() } returns initialApps andThen updatedApps

        viewModel = AppListViewModel(mockAppListProvider)
        advanceUntilIdle()

        // When
        viewModel.refreshApps()
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.apps.value.size)
        verify(exactly = 2) { mockAppListProvider.load() }
    }
}

