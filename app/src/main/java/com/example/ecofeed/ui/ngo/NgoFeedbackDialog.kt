package com.example.ecofeed.ui.ngo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecofeed.data.model.FeedbackRequest

private val EcoGreen = Color(0xFF008000)

@Composable
fun NgoFeedbackDialog(
    donationId: String?,
    donorId: String?,
    ngoId: String?,
    foodTitle: String? = "Donation",
    onDismiss: () -> Unit,
    onSubmit: (FeedbackRequest) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var feedback by remember { mutableStateOf("") }

    if (donationId.isNullOrBlank() || donorId.isNullOrBlank() || ngoId.isNullOrBlank()) {
        android.util.Log.e("NGO_FEEDBACK", "Missing IDs: donationId=$donationId, donorId=$donorId, ngoId=$ngoId")
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate ${foodTitle?.ifBlank { "Donation" } ?: "Donation"}", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How was the quality of food for '${foodTitle?.ifBlank { "this order" } ?: "this order"}'?", 
                    fontWeight = FontWeight.SemiBold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (star in 1..5) {
                        TextButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (star <= rating) "★" else "☆",
                                color = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Review comment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                enabled = rating in 1..5,
                onClick = {
                    try {
                        onSubmit(
                            FeedbackRequest(
                                donationId = donationId,
                                donorId = donorId,
                                reviewerId = ngoId,
                                reviewerRole = "NGO",
                                rating = rating,
                                comments = feedback
                            )
                        )
                    } catch (_: Exception) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
