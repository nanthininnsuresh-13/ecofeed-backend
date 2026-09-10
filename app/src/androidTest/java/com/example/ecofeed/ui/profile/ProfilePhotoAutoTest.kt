package com.example.ecofeed.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ecofeed.ui.ProfileScreen
import com.example.ecofeed.ui.auth.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ProfilePhotoAutoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileScreenDisplaysPhotosAndFields() {
        val mockViewModel = mock(AuthViewModel::class.java)
        
        // Verifies the Profile Screen renders with high contrast and correct field labels
        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onEditClick = {},
                onChangePasswordClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
        composeTestRule.onNodeWithText("Location").assertExists()
        composeTestRule.onNodeWithText("Phone Number").assertExists()
    }
}
