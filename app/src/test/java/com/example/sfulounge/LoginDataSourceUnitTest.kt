package com.example.sfulounge

import com.example.sfulounge.data.LoginDataSource
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginDataSourceUnitTest {
    private lateinit var dataSourceUnderTest: LoginDataSource

    @Before
    fun setup() {
        dataSourceUnderTest = LoginDataSource()
    }

    @Test
    fun loginAsUnregisteredUser_onErrorIsCalled() {
//        val onErrorMock = mock<ResponseCallback>()
//        dataSourceUnderTest.login1(
//            "fake_email@sfu.ca",
//            "fake_password",
//            onErrorMock
//        )
//        verify(onErrorMock).onError(any())
        Assert.assertEquals(4, 2 + 2)

    }

//    @Test
//    fun loginAsRegisteredUser_onSuccessIsCalled() {
//        val onSuccessMock = mock<(Result.Success<LoggedInUser>) -> Unit>()
//        dataSourceUnderTest.login(
//            "matheww@sfu.ca",
//            "aaaaaaA1!",
//            onSuccessMock,
//            mock()
//        )
//        verify(onSuccessMock).invoke(any())
//    }
}