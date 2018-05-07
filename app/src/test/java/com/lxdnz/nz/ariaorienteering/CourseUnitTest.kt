package com.lxdnz.nz.ariaorienteering

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lxdnz.nz.ariaorienteering.model.Course
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate

import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito.doAnswer


@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(FirebaseDatabase::class)
class CourseUnitTest{

    lateinit var mockedDatabaseReference: DatabaseReference

    @Before
    fun before() {
        mockedDatabaseReference = PowerMockito.mock(DatabaseReference::class.java)
        val mockedFirebaseDatabase = PowerMockito.mock(FirebaseDatabase::class.java)
        PowerMockito.`when`(mockedFirebaseDatabase.reference).thenReturn(mockedDatabaseReference)
        PowerMockito.mockStatic(FirebaseDatabase::class.java)
        PowerMockito.`when`(FirebaseDatabase.getInstance()).thenReturn(mockedFirebaseDatabase)
    }

    @Test
    fun createCourseTest() {
        PowerMockito.`when`(mockedDatabaseReference.child(anyString())).thenReturn(mockedDatabaseReference)
        val testCourse = Course("A", 5, mutableListOf(10,11,12))
        // then do Task<Course>
        val tcs: TaskCompletionSource<Course> = TaskCompletionSource()
        mockedDatabaseReference.child(testCourse.id).setValue(testCourse)
        tcs.setResult(testCourse)
        // get tcs result
        val result = tcs.task.result

        assertNotNull("Test Course was created",result)
        assertTrue("Course Id matches", result.id === "A")
        assertTrue("Course year matches", result.year == 5)
    }

}