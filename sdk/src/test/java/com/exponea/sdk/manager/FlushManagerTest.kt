package com.exponea.sdk.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.exponea.sdk.models.DatabaseStorageObject
import com.exponea.sdk.models.ExponeaConfiguration
import com.exponea.sdk.models.ExponeaProject
import com.exponea.sdk.models.ExportedEventType
import com.exponea.sdk.models.Route
import com.exponea.sdk.network.ExponeaService
import com.exponea.sdk.repository.EventRepository
import com.exponea.sdk.repository.EventRepositoryImpl
import com.exponea.sdk.testutil.ExponeaSDKTest
import com.exponea.sdk.testutil.mocks.ExponeaMockService
import com.exponea.sdk.testutil.waitForIt
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class FlushManagerTest : ExponeaSDKTest() {
    private lateinit var manager: FlushManager
    private lateinit var repo: EventRepository
    private lateinit var connectionManager: ConnectionManager
    private lateinit var service: ExponeaService

    private fun setup(connected: Boolean, serviceSuccess: Boolean) {
        val configuration = ExponeaConfiguration()
        val context = ApplicationProvider.getApplicationContext<Context>()

        connectionManager = mockk()
        every { connectionManager.isConnectedToInternet() } returns connected
        service = spyk(ExponeaMockService(serviceSuccess))
        repo = EventRepositoryImpl(context)
        repo.clear()
        manager = FlushManagerImpl(configuration, repo, service, connectionManager)
    }

    private fun createTestEvent(includeProject: Boolean) {
        repo.add(DatabaseStorageObject(
            projectId = "old-project-id",
            item = ExportedEventType(
                type = "test_event",
                timestamp = System.currentTimeMillis() / 1000.0,
                customerIds = hashMapOf(),
                properties = hashMapOf("property" to "value")
            ),
            route = Route.TRACK_EVENTS,
            exponeaProject = if (includeProject)
                ExponeaProject("mock_base_url.com", "mock_project_token", "mock_auth")
                else null
        ))
    }

    @Test
    fun `should flush event`() {
        setup(connected = true, serviceSuccess = true)
        createTestEvent(true)
        waitForIt {
            manager.flushData { _ ->
                it.assertEquals(0, repo.all().size)
                it()
            }
        }
        verify {
            service.postEvent(ExponeaProject("mock_base_url.com", "mock_project_token", "mock_auth"), any())
        }
    }

    @Test
    fun `should flush old event without exponea project`() {
        setup(connected = true, serviceSuccess = true)
        createTestEvent(false)
        waitForIt {
            manager.flushData { _ ->
                it.assertEquals(0, repo.all().size)
                it()
            }
        }
        verify {
            service.postEvent(ExponeaProject("https://api.exponea.com", "old-project-id", null), any())
        }
    }

    @Test
    fun `should fail to flush without internet connection`() {
        setup(connected = false, serviceSuccess = true)
        createTestEvent(true)
        every { connectionManager.isConnectedToInternet() } returns false
        waitForIt {
            manager.flushData { _ ->
                it.assertEquals(1, repo.all().size)
                it.assertEquals(0, repo.all().first().tries)
                it()
            }
        }
    }

    @Test
    fun `should increase tries on flush failure`() {
        setup(connected = true, serviceSuccess = false)
        createTestEvent(true)

        waitForIt {
            manager.flushData { _ ->
                assertEquals(1, repo.all().size)
                assertEquals(1, repo.all().first().tries)
                it()
            }
        }
    }

    @Test
    fun `should delete event on max tries reached`() {
        setup(connected = true, serviceSuccess = false)
        createTestEvent(true)

        val event = repo.all().first()
        event.tries = 10
        repo.update(event)

        waitForIt {
            manager.flushData { _ ->
                assertEquals(0, repo.all().size)
                it()
            }
        }
    }

    @Test
    fun `should only flush once`() {
        setup(connected = true, serviceSuccess = false)
        createTestEvent(true)

        waitForIt {
            var done = 0
            for (i in 1..10) {
                thread(start = true) {
                    manager.flushData {
                        if (++done == 10) it()
                    }
                }
            }
        }

        verify(exactly = 1) {
            service.postEvent(any(), any())
        }
    }
}
