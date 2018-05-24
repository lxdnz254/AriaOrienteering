package com.lxdnz.nz.ariaorienteering

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.Course
import com.lxdnz.nz.ariaorienteering.model.Marker
import com.lxdnz.nz.ariaorienteering.model.User
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
class UserUnitTest {

    lateinit var mockedDatabaseReference: DatabaseReference
    lateinit var testUser: User
    lateinit var testCourse: Course
    lateinit var mockMarkerList: MutableList<Marker>

    @Before
    fun before() {
        // Set up test User
        testUser = User("1A", "a@b.com", "Jim", 0.0, 0.0, true)
        //Set up test Course
        val marker1 = Marker()
        val marker2 = Marker()
        mockMarkerList = mutableListOf(marker1, marker2)
        testCourse = Course("A", 5, mockMarkerList)

        // Mock the Firebase References
        mockedDatabaseReference = PowerMockito.mock(DatabaseReference::class.java)
        val mockedFirebaseDatabase = PowerMockito.mock(FirebaseDatabase::class.java)
        PowerMockito.`when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)
        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        PowerMockito.`when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)
    }

    @Test
    fun createUserTaskTest() {
        PowerMockito.`when`(mockedDatabaseReference.child(anyString())).thenReturn(mockedDatabaseReference)
        // then do Task<Course>
        val tcs: TaskCompletionSource<User> = TaskCompletionSource()
        mockedDatabaseReference.child(testUser.uid).setValue(testUser)
        tcs.setResult(testUser)
        // get tcs result
        val result = tcs.task.result

        assertNotNull("result Exists", result)
        val uid = result.uid
        val email = result.email
        val firstName = result.firstName
        val lat = result.lat
        val lon = result.lon
        assertEquals("User ids match", "1A", uid)
        assertEquals("User email match",  "a@b.com", email)
        assertEquals("User firstName match", "Jim", firstName)
        assertTrue("User latitude match" , lat == 0.0)
        assertTrue("User longitude matches", lon == 0.0)
    }

    @Test
    fun testUserClass() {
        val u = testUser

        val uid = u.uid
        val email = u.email
        val firstName = u.firstName
        val lat = u.lat
        val lon = u.lon

        assertEquals("User ids match", "1A", uid)
        assertEquals("User email match",  "a@b.com", email)
        assertEquals("User firstName match", "Jim", firstName)
        assertTrue("User latitude match" , lat == 0.0)
        assertTrue("User longitude matches", lon == 0.0)
    }

    @Test
    fun testUpdateMarkerInUserCourse() {
        val u = testUser
        u.courseObject = testCourse
        val markerListSize = testCourse.markers.size
        val newMarker = Marker(100, ImageType.DEFAULT, 0.0, 0.0)
        val updateMarker_1 = Marker(100, ImageType.DEFAULT, 0.0, 0.0)
        val updateCourse = u.courseObject

        // write the inner function / implementation code
        fun testMarkerCheck(marker: Marker) {

            val findMarker = updateCourse!!.markers.find { it -> it.id == marker.id }
            if(findMarker != null){
                val index = updateCourse.markers.indexOf(findMarker)
                updateCourse.markers.removeAt(index)
                marker.status = MarkerStatus.FOUND
                updateCourse.markers.add(index, marker)
            }
        }

        //add the first Marker
        updateCourse!!.markers.add(newMarker)
        // test marker added to list
        assertEquals("Marker List size increased", markerListSize+1, updateCourse.markers.size)

        // perform the method to check for match, update status if match
        testMarkerCheck(updateMarker_1)

        // test
        assertEquals("Marker List size did not increase", markerListSize+1, updateCourse.markers.size)
        val updatedMarker = updateCourse.markers.find { it -> it.id == updateMarker_1.id }
        val updateIndex = updateCourse.markers.indexOf(updatedMarker)
        assertEquals("Marker is now Found", MarkerStatus.FOUND, updateCourse.markers[updateIndex].status)
    }

}