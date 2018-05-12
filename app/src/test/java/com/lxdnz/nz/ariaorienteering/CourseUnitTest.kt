package com.lxdnz.nz.ariaorienteering

import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.database.*
import com.lxdnz.nz.ariaorienteering.model.Course
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import org.junit.Assert.*


@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(FirebaseDatabase::class)
class CourseUnitTest{

    lateinit var mockedDatabaseReference: DatabaseReference
    lateinit var testCourse: Course
    lateinit var mockedLooper: Looper

    @Before
    fun before() {
        mockedLooper = PowerMockito.mock(Looper::class.java)
        mockedDatabaseReference = PowerMockito.mock(DatabaseReference::class.java)
        val mockedFirebaseDatabase = PowerMockito.mock(FirebaseDatabase::class.java)
        PowerMockito.`when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)
        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        PowerMockito.`when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)
        testCourse = Course("A", 5, mutableListOf(10,11,12))
    }

    @Test
    fun createCourseTest() {
        PowerMockito.`when`(mockedDatabaseReference.child(anyString())).thenReturn(mockedDatabaseReference)
        // then do Task<Course>
        val tcs: TaskCompletionSource<Course> = TaskCompletionSource()
        mockedDatabaseReference.child(testCourse.id).setValue(testCourse)
        tcs.setResult(testCourse)
        // get tcs result
        val result = tcs.task.result

        assertNotNull("Test Course was created",result)
        assertTrue("Course Id matches", result.id === testCourse.id)
        assertTrue("Course year matches", result.year == testCourse.year)
        assertTrue("Course is List", result.markers.isNotEmpty())
        assertTrue ("Course List first item is 10", result.markers[0] == testCourse.markers[0])
    }

    @Test
    fun retrieveCourseTest() {
        PowerMockito.`when`(mockedDatabaseReference.child(anyString())).thenReturn(mockedDatabaseReference)

        val testOrMockedUser = Course("A", 5, mutableListOf(10,11,12))
        val throwable = Throwable("An error happened!")
        doAnswer { invocation ->
            val valueEventListener = invocation.arguments[0] as ValueEventListener

            val mockedDataSnapshot = Mockito.mock(DataSnapshot::class.java)
            //when(mockedDataSnapshot.getValue(Course::class.java)).thenReturn(testOrMockedUser)

            valueEventListener.onDataChange(mockedDataSnapshot)

            val mockedDatabaseError = Mockito.mock(DatabaseError::class.java)
            valueEventListener.onCancelled(mockedDatabaseError)

            null
        }.`when`(mockedDatabaseReference).addListenerForSingleValueEvent(any(ValueEventListener::class.java))

    }

}