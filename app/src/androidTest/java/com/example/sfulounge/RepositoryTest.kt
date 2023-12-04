package com.example.sfulounge

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
//    private lateinit var remoteDataSource: RemoteDataSource
//    private lateinit var userRepository: UserRepository
//
//    @Before
//    fun setup() {
//        remoteDataSource = mockk()
//        userRepository = UserRepository(remoteDataSource)
//    }
//
//    @Test
//    fun testGetUserById() = runBlocking {
//        // Given
//        val userId = "123"
//        val expectedUser = User(userId, UserProfile(userId, "John Doe"))
//        coEvery { remoteDataSource.getUserById(userId) } returns expectedUser
//
//        // When
//        val result = userRepository.getUserById(userId)
//
//        // Then
//        assertEquals(expectedUser, result)
//    }

//    @Test
//    fun testLoadUserProfile() = runBlockingTest {
//        // Given
//        val userId = "123"
//        val userProfile = UserProfile(userId, "John Doe")
//        coEvery { userRepository.getUserById(userId) } returns User(userId, userProfile)
//
//        // When
//        viewModel.loadUserProfile(userId)
//
//        // Then
//        val result = viewModel.userProfile.getOrAwaitValue()
//        assertEquals(userProfile, result)
//    }
}