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

    /**
     * Verifies that the Location field is visible and correctly labeled.
     * Also asserts that the Phone Number and Location are distinct fields.
     */
    @Test
    fun testProfileLocationAndPhoneDistinction() {
        val mockViewModel = mock(AuthViewModel::class.java)
        
        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onEditClick = {},
                onChangePasswordClick = {}
            )
        }

        // Verify Location and Phone Number labels exist
        composeTestRule.onNodeWithText("Location").assertExists()
        composeTestRule.onNodeWithText("Phone Number").assertExists()
    }
}
