package com.example.ecofeed.ui.ngo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ecofeed.data.model.ReviewRequest

private val EcoGreen = Color(0xFF008000)

@Composable
fun NgoFeedbackDialog(
    donationId: String,
    donorId: String,
    ngoId: String,
    onDismiss: () -> Unit,
    onSubmit: (ReviewRequest) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var feedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Feedback") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("How was this donation experience?", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (star in 1..5) {
                        TextButton(onClick = { rating = star }) {
                            Text(
                                text = if (star <= rating) "★" else "☆",
                                color = if (star <= rating) EcoGreen else Color.Gray,
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
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        ReviewRequest(
                            donationId = donationId,
                            donorId = donorId,
                            ngoId = ngoId,
                            rating = rating,
                            feedbackText = feedback
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
