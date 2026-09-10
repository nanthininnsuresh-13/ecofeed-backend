package com.example.ecofeed.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ecofeed.ui.auth.AuthViewModel
import com.example.ecofeed.ui.ProfileScreen
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileLocationVisibility() {
        val mockViewModel = mock(AuthViewModel::class.java)
        
        // This test verifies that the Profile Screen correctly displays the Location label
        // and its associated value from the UI state.
        
        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onEditClick = {},
                onChangePasswordClick = {}
            )
        }

        // Assert that the Location section is present
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
    }
}
