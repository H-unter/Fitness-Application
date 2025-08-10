package com.example.fitnessapp
import androidx.health.connect.client.testing.FakeHealthConnectClient
import androidx.health.connect.client.testing.FakePermissionController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnessapp.data.HealthConnectManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/** Test for HealthConnectManager
 * this implementation of testing is inspired off of the health connect documentation:
 * https://developer.android.com/health-and-fitness/guides/health-connect/test/unit-tests
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class HealthConnectManagerTest {

    private lateinit var fakeHealthConnectClient: FakeHealthConnectClient
    private lateinit var healthConnectManager: HealthConnectManager

    @Before
    fun setup() {
        fakeHealthConnectClient = FakeHealthConnectClient()
        healthConnectManager = HealthConnectManager(
            context = ApplicationProvider.getApplicationContext(),
            healthConnectClient = fakeHealthConnectClient
        )
    }

    @Test
    fun hasAllPermissions_whenAllPermissionsGranted_returnsTrue() = runTest {
        // Create a permission controller with all permissions granted
        val permissionController = FakePermissionController(grantAll = true)
        val fakeClientWithPermissions = FakeHealthConnectClient(
            permissionController = permissionController
        )
        val managerWithPermissions = HealthConnectManager(
            context = ApplicationProvider.getApplicationContext(),
            healthConnectClient = fakeClientWithPermissions
        )
        val result = managerWithPermissions.hasAllPermissions()
        assertTrue(result)
    }

    @Test
    fun checkAvailability_returnsInstalledOrNotInstalledOrNotSupported() = runTest {
        val availability = healthConnectManager.checkAvailability()
        assertTrue(
            availability == com.example.fitnessapp.data.HealthConnectAvailability.INSTALLED ||
            availability == com.example.fitnessapp.data.HealthConnectAvailability.NOT_INSTALLED ||
            availability == com.example.fitnessapp.data.HealthConnectAvailability.NOT_SUPPORTED
        )
    }

    @Test
    fun getGrantedPermissions_returnsSetAndRevocationWorks() = runTest {
        // Initially, permissions should be granted (FakeHealthConnectClient default)
        val initialPermissions = healthConnectManager.getGrantedPermissions()
        assertNotNull(initialPermissions)
        assertTrue(initialPermissions is Set<*>)
        assertTrue(initialPermissions.isNotEmpty())

        // Revoke all permissions and check again
        val revokeResult = healthConnectManager.revokeAllPermissions()
        assertTrue(revokeResult)
        val afterRevokePermissions = healthConnectManager.getGrantedPermissions()
        assertTrue(afterRevokePermissions.isEmpty())
    }


}