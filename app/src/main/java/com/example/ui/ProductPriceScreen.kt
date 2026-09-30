package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ProductPriceEntity
import com.example.data.repository.ProductPriceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPriceScreen(
    onBack: () -> Unit,
    onOpenAiChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProductPriceRepository.getInstance(context) }
    val products by repository.allProducts.collectAsState(initial = emptyList())

    val categories = listOf(
        "ทั้งหมด",
        "น้ำมันเครื่อง",
        "น้ำมัน 2T",
        "น้ำมันเสริมอื่นๆ",
        "อะไหล่เปลี่ยนประจำ",
        "ลูกปืน (Ball Bearing)",
        "งานบริการ/ค่าแรง"
    )

    var selectedCategory by rememberSaveable { mutableStateOf("ทั้งหมด") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var editingProduct by remember { mutableStateOf<ProductPriceEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductPriceEntity?>(null) }

    val filteredProducts = remember(products, selectedCategory, searchQuery) {
        products.filter { p ->
            val matchCategory = if (selectedCategory == "ทั้งหมด") true else p.category == selectedCategory
            val matchQuery = if (searchQuery.isBlank()) true else {
                p.name.contains(searchQuery, ignoreCase = true) ||
                        p.category.contains(searchQuery, ignoreCase = true)
            }
            matchCategory && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "รายการสินค้า & ราคาช่าง",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            "ระบบราคาอะไหล่ & ค่าบริการอู่ (${products.size} รายการ)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_product_prices")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "กลับ")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onOpenAiChat,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ถาม AI / สั่งแก้", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("fab_add_product")
            ) {
                Icon(Icons.Default.Add, contentDescription = "เพิ่มสินค้าใหม่")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // AI Help Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiChat() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "สั่ง AI เช็คราคา หรือสั่งแก้ราคาได้ทันที!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "พิมพ์ถาม เช่น 'ปะยางกี่บาท', 'เปลี่ยนลูกปืนแผงคอเท่าไหร่' หรือ 'แก้ราคาปะยาง เป็น 60 บาท'",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_product"),
                placeholder = { Text("ค้นหาสินค้า เช่น ปะยาง, ฮอนด้า, 6201, โซ่สเตอร์...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "ล้าง")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "ไม่พบรายการที่ตรงกับการค้นหา",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductPriceCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Edit Product Dialog
    if (editingProduct != null) {
        val p = editingProduct!!
        var editName by rememberSaveable { mutableStateOf(p.name) }
        var editCategory by rememberSaveable { mutableStateOf(p.category) }
        var editCost by rememberSaveable { mutableStateOf(p.costPrice.toInt().toString()) }
        var editRetail by rememberSaveable { mutableStateOf(p.retailPrice.toInt().toString()) }
        var editInstalled by rememberSaveable { mutableStateOf(p.installedPrice.toInt().toString()) }
        var editNote by rememberSaveable { mutableStateOf(p.unitNote) }

        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("แก้ไขราคาสินค้า / บริการ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ชื่อสินค้า/บริการ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("หมวดหมู่") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editCost,
                            onValueChange = { editCost = it },
                            label = { Text("ราคาทุน (บ.)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editRetail,
                            onValueChange = { editRetail = it },
                            label = { Text("ขายปลีก (บ.)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = editInstalled,
                        onValueChange = { editInstalled = it },
                        label = { Text("ราคาพร้อมติดตั้ง/ค่าแรง (บ.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("หมายเหตุ/หน่วย เช่น บาท, ต่อคู่") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = p.copy(
                        name = editName.trim(),
                        category = editCategory.trim(),
                        costPrice = editCost.toDoubleOrNull() ?: p.costPrice,
                        retailPrice = editRetail.toDoubleOrNull() ?: p.retailPrice,
                        installedPrice = editInstalled.toDoubleOrNull() ?: p.installedPrice,
                        unitNote = editNote.trim()
                    )
                    scope.launch {
                        repository.updateProduct(updated)
                        editingProduct = null
                    }
                }) {
                    Text("บันทึกการแก้ไข")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Add New Product Dialog
    if (isAddingNew) {
        var newName by rememberSaveable { mutableStateOf("") }
        var newCategory by rememberSaveable { mutableStateOf(if (selectedCategory == "ทั้งหมด") "น้ำมันเครื่อง" else selectedCategory) }
        var newCost by rememberSaveable { mutableStateOf("0") }
        var newRetail by rememberSaveable { mutableStateOf("0") }
        var newInstalled by rememberSaveable { mutableStateOf("0") }
        var newNote by rememberSaveable { mutableStateOf("บาท") }

        AlertDialog(
            onDismissRequest = { isAddingNew = false },
            title = { Text("เพิ่มรายการสินค้า / บริการใหม่", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("ชื่อสินค้า/บริการ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("หมวดหมู่ (เช่น น้ำมันเครื่อง, อะไหล่, ค่าแรง)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCost,
                            onValueChange = { newCost = it },
                            label = { Text("ราคาทุน (บ.)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newRetail,
                            onValueChange = { newRetail = it },
                            label = { Text("ขายปลีก (บ.)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = newInstalled,
                        onValueChange = { newInstalled = it },
                        label = { Text("ราคาพร้อมติดตั้ง/ค่าแรง (บ.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        label = { Text("หน่วย/หมายเหตุ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val entity = ProductPriceEntity(
                                name = newName.trim(),
                                category = newCategory.trim(),
                                costPrice = newCost.toDoubleOrNull() ?: 0.0,
                                retailPrice = newRetail.toDoubleOrNull() ?: 0.0,
                                installedPrice = newInstalled.toDoubleOrNull() ?: 0.0,
                                unitNote = newNote.trim()
                            )
                            scope.launch {
                                repository.insertProduct(entity)
                                isAddingNew = false
                            }
                        }
                    },
                    enabled = newName.isNotBlank()
                ) {
                    Text("เพิ่มรายการ")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingNew = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val p = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ยืนยันการลบรายการ") },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบ '${p.name}' ออกจากฐานข้อมูลสินค้า?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteById(p.id)
                            productToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบรายการ")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun ProductPriceCard(
    product: ProductPriceEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "แก้ไข",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "ลบ",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cost
                Column {
                    Text(
                        "ต้นทุน",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        "${product.costPrice.toInt()} ฿",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Retail Price
                Column {
                    Text(
                        "ราคาขายปลีก",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${product.retailPrice.toInt()} ฿",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Installed Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "ราคาพร้อมติดตั้ง",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.installedPrice > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        if (product.installedPrice > 0) "${product.installedPrice.toInt()} ฿" else "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.installedPrice > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
