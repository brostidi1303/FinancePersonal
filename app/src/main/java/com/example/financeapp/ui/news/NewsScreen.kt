package com.example.financeapp.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.data.CommonSectors
import com.example.financeapp.data.NewsArticle
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.FinanceViewModel

private val AccentBlue = Color(0xFF2979FF)
private val PositiveGreen = Color(0xFF00A137)
private val NegativeRed = Color(0xFFEF5350)
private val WarningOrange = Color(0xFFF57C00)

@Composable
fun NewsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val allNews by viewModel.allNews.collectAsStateWithLifecycle()
    val newsBySector by viewModel.newsBySector.collectAsStateWithLifecycle()
    val newsBySymbol by viewModel.newsBySymbol.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAllNewsLoading.collectAsStateWithLifecycle()
    val isSectorLoading by viewModel.isSectorNewsLoading.collectAsStateWithLifecycle()
    val isSymbolLoading by viewModel.isSymbolNewsLoading.collectAsStateWithLifecycle()
    val currentSearchSector by viewModel.currentSearchSector.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    var showSectorSearch by remember { mutableStateOf(false) }
    var showSymbolSearch by remember { mutableStateOf(false) }
    var sectorSearchQuery by remember { mutableStateOf("") }
    var symbolSearchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // ✅ Debounced sector search
    LaunchedEffect(sectorSearchQuery) {
        if (sectorSearchQuery.isBlank()) {
            viewModel.clearSectorSearch()
        } else {
            // Debounce 500ms trước khi call API
            kotlinx.coroutines.delay(500)
            viewModel.fetchNewsBySector(sectorSearchQuery)
        }
    }

    // ✅ Debounced symbol search
    LaunchedEffect(symbolSearchQuery) {
        if (symbolSearchQuery.isBlank()) {
            viewModel.clearSymbolSearch()
        } else {
            // Debounce 500ms trước khi call API
            kotlinx.coroutines.delay(500)
            viewModel.fetchNewsBySymbol(symbolSearchQuery)
        }
    }

    // Load all news khi màn hình được tạo
    LaunchedEffect(Unit) {
        viewModel.fetchAllNews()
    }

    // ✅ Determine which news to show
    val displayNews = remember(newsBySector, newsBySymbol, allNews, showSectorSearch, showSymbolSearch) {
        if (showSymbolSearch && newsBySymbol != null) {
            newsBySymbol!!.news
        } else if (showSectorSearch && newsBySector != null) {
            newsBySector!!.news
        } else {
            allNews
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }

            Text(
                "Tin tức thị trường",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // ─── Filter Chips ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Nhóm ngành filter
            FilterChip(
                selected = showSectorSearch,
                label = "Nhóm ngành",
                onClick = {
                    showSectorSearch = !showSectorSearch
                    if (!showSectorSearch) {
                        sectorSearchQuery = ""
                        viewModel.clearSectorSearch()
                    }
                }
            )

            // ✅ Mã cổ phiếu filter
            FilterChip(
                selected = showSymbolSearch,
                label = "Mã cổ phiếu",
                onClick = {
                    showSymbolSearch = !showSymbolSearch
                    if (!showSymbolSearch) {
                        symbolSearchQuery = ""
                    }
                }
            )
        }

        // ─── Search Fields với Animation ──────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Sector Search - chỉ hiện khi showSectorSearch = true
            AnimatedVisibility(
                visible = showSectorSearch,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SearchField(
                        value = sectorSearchQuery,
                        onValueChange = { sectorSearchQuery = it },
                        placeholder = "Nhập nhóm ngành...",
                        label = "NHÓM NGÀNH",
                        onClear = {
                            sectorSearchQuery = ""
                            viewModel.clearSectorSearch()
                        }
                    )

                    // ✅ Common sectors suggestions
                    if (sectorSearchQuery.isBlank()) {
                        SectorSuggestions(
                            onSectorClick = { sector ->
                                sectorSearchQuery = sector
                            }
                        )
                    }
                }
            }

            // ✅ Symbol Search - chỉ hiện khi showSymbolSearch = true
            AnimatedVisibility(
                visible = showSymbolSearch,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
            ) {
                SearchField(
                    value = symbolSearchQuery,
                    onValueChange = { symbolSearchQuery = it },
                    placeholder = "Tìm mã cổ phiếu...",
                    label = "MÃ CỔ PHIẾU",
                    onClear = { symbolSearchQuery = "" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Loading indicator for sector search
        if (isSectorLoading || isSymbolLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Đang tìm kiếm...",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        if (isLoading && allNews.isEmpty()) {
            // Initial Loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // ─── News List ─────────────────────────────────────
                if (displayNews.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy tin tức",
                                color = colors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(
                        items = displayNews,
                        key = { it.getUniqueId() }
                    ) { article ->
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            NewsArticleItem(article = article)
                        }
                    }

                    // Loading indicator at bottom
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AccentBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AccentBlue else colors.cardBackground,
        border = if (!selected) BorderStroke(1.dp, colors.divider) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else colors.textPrimary
            )
            Text(
                text = "▼",
                fontSize = 10.sp,
                color = if (selected) Color.White else colors.textSecondary
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    onClear: () -> Unit
) {
    val colors = AppTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            letterSpacing = 0.5.sp
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.divider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                        }
                        innerTextField()
                    }
                )
                // ✅ Clear button
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ✅ FIXED: Sector suggestions chips
 */
@Composable
fun SectorSuggestions(
    onSectorClick: (String) -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "GỢI Ý PHỔ BIẾN",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            letterSpacing = 0.5.sp
        )

        // ✅ FIXED: Remove background color and use proper FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommonSectors.list.forEach { sector ->
                SuggestionChip(
                    label = sector,
                    onClick = { onSectorClick(sector) }
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.divider)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * ✅ FIXED: FlowRow implementation - corrected layout calculation
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacing = 8.dp.roundToPx()
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        var currentX = 0
        var currentY = 0
        var lineHeight = 0

        val rowPositions = mutableListOf<Pair<Int, Int>>()

        // ✅ Calculate positions
        placeables.forEach { placeable ->
            // Check if we need a new line
            if (currentX + placeable.width > constraints.maxWidth && currentX > 0) {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }

            // Store position for this placeable
            rowPositions.add(Pair(currentX, currentY))

            // Update tracking variables
            currentX += placeable.width + spacing
            lineHeight = maxOf(lineHeight, placeable.height)
        }

        // ✅ Calculate total height
        val totalHeight = currentY + lineHeight

        layout(constraints.maxWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = rowPositions[index]
                placeable.placeRelative(x, y)
            }
        }
    }
}

@Composable
fun NewsArticleItem(article: NewsArticle) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val sentimentColor = when (article.sentiment.uppercase()) {
        "POSITIVE" -> PositiveGreen
        "NEGATIVE" -> NegativeRed
        else -> WarningOrange
    }

    val sentimentLabel = when (article.sentiment.uppercase()) {
        "POSITIVE" -> "TÍCH CỰC"
        "NEGATIVE" -> "TIÊU CỰC"
        else -> "TRUNG LẬP"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.divider)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sentiment & Sector Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sentiment Tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = sentimentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, sentimentColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = sentimentLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = sentimentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Sector Tag (if available)
                article.sector?.let { sector ->
                    if (sector.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = sector.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Title
            Text(
                text = article.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                lineHeight = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Source & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Text(
                    text = "• ${article.getRelativeTime()}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}