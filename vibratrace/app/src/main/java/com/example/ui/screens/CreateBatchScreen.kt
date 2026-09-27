package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.viewmodel.VibraTraceViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchScreen(
    viewModel: VibraTraceViewModel,
    onBack: () -> Unit,
    onCreated: (String) -> Unit
) {
    BackHandler { onBack() }

    val randomSuffix = remember { Random.nextInt(100, 999) }
    var batchId by remember { mutableStateOf("FD2026-$randomSuffix") }
    var productName by remember { mutableStateOf("Organic Hass Avocados") }
    var category by remember { mutableStateOf("Fresh Subtropical Fruits") }
    var farmerSupplier by remember { mutableStateOf("GreenValley Agro Cooperative") }
    var quantity by remember { mutableStateOf("1200") }
    var unit by remember { mutableStateOf("kg") }
    var destination by remember { mutableStateOf("Antwerp Cold Storage Facility") }
    var transporter by remember { mutableStateOf("TransReefer Logistics Ltd") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Batch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = batchId,
                onValueChange = { batchId = it },
                label = { Text("Batch ID (e.g. FD2026-001)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("create_batch_id_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("create_batch_product_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Product Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = farmerSupplier,
                onValueChange = { farmerSupplier = it },
                label = { Text("Farmer / Supplier") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Final Destination") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = transporter,
                onValueChange = { transporter = it },
                label = { Text("Transporter / Fleet") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val qtyNum = quantity.toDoubleOrNull() ?: 100.0
                    viewModel.createBatch(
                        id = batchId,
                        name = productName,
                        category = category,
                        farmer = farmerSupplier,
                        qty = qtyNum,
                        unit = unit,
                        destination = destination,
                        transporter = transporter
                    )
                    onCreated(batchId)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_batch_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Batch & Anchor to Blockchain", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
