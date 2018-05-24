package com.lxdnz.nz.ariaorienteering

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.types.ImageType
import com.lxdnz.nz.ariaorienteering.model.types.MarkerStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate

import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito.doAnswer

import org.junit.Assert.*

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(FirebaseDatabase::class)
class MarkerUnitTest {

    lateinit var mockedDatabaseReference: DatabaseReference
    lateinit var testMarker: Marker

    @Before
    fun before() {
        //Set up marker to test
        testMarker = Marker(1, ImageType.DEFAULT, 0.0, 0.0)

        // Mock the Firebase References
        mockedDatabaseReference = PowerMockito.mock(DatabaseReference::class.java)
        val mockedFirebaseDatabase = PowerMockito.mock(FirebaseDatabase::class.java)
        PowerMockito.`when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)
        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        PowerMockito.`when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)
    }

    @Test
    fun createMarkerTaskTest() {
        PowerMockito.`when`(mockedDatabaseReference.child(anyString())).thenReturn(mockedDatabaseReference)
        // then do Task<Course>
        val tcs: TaskCompletionSource<Marker> = TaskCompletionSource()
        mockedDatabaseReference.child(testMarker.id.toString()).setValue(testMarker)
        tcs.setResult(testMarker)
        // get tcs result
        val result = tcs.task.result

        val id = result.id
        val type = result.imageType
        val lon = result.lon
        val lat = result.lat
        val status = result.status

        assertEquals("Marker Id are equal", 1, id)
        assertEquals("Marker type equal", ImageType.DEFAULT, type)
        assertTrue("Marker longitude equal", lon == 0.0)
        assertTrue("Marker latitude equal", lat == 0.0)
        assertEquals("Marker Status", MarkerStatus.NOT_FOUND, status)
    }

    @Test
    fun testMarkerClass() {
        val m = testMarker

        val id = m.id
        val type = m.imageType
        val lon = m.lon
        val lat = m.lat
        val status = m.status

        assertEquals("Marker Id are equal", 1, id)
        assertEquals("Marker type equal", ImageType.DEFAULT, type)
        assertTrue("Marker longitude equal", lon == 0.0)
        assertTrue("Marker latitude equal", lat == 0.0)
        assertEquals("Marker Status", MarkerStatus.NOT_FOUND, status)
    }

}