package com.knottlabs.jinnyspatchtracker.ui

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.knottlabs.jinnyspatchtracker.DataStoreManager
import com.knottlabs.jinnyspatchtracker.JournalEntry
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun JournalHistoryContent(
    dataStore: DataStoreManager,
    surfaceColor: Color,
    textColor: Color,
    primaryColor: Color
) {
    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var shouldLoadAds by remember { mutableStateOf(false) }

    // State for month navigation
    val months = remember { mutableStateListOf<String>() }
    var selectedMonthIndex by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(initialPage = selectedMonthIndex)
    val coroutineScope = rememberCoroutineScope()

    // Load entries
    LaunchedEffect(Unit) {
        journalEntries = withContext(Dispatchers.IO) {
            dataStore.getAllJournalEntries().sortedByDescending { it.timestamp }
        }

        // Group entries by month and set initial state
        val entriesByMonth = journalEntries.groupBy { getMonthYearFromTimestamp(it.timestamp) }
        months.clear()
        months.addAll(entriesByMonth.keys)
        selectedMonthIndex = 0.coerceAtMost(months.size - 1)

        isLoading = false
        delay(1000)
        shouldLoadAds = true
    }

    // Sync pager state with selected month
    LaunchedEffect(selectedMonthIndex) {
        pagerState.animateScrollToPage(selectedMonthIndex)
    }

    // Handle swipe gestures
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != selectedMonthIndex && page in months.indices) {
                selectedMonthIndex = page
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            journalEntries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No journal entries yet",
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Month tabs with visual indicators
                    val entriesByMonth = journalEntries.groupBy { getMonthYearFromTimestamp(it.timestamp) }
                    ScrollableTabRow(
                        selectedTabIndex = selectedMonthIndex,
                        edgePadding = 16.dp,
                        containerColor = surfaceColor,
                        contentColor = primaryColor,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMonthIndex]),
                                color = primaryColor
                            )
                        }
                    ) {
                        months.forEachIndexed { index, month ->
                            val monthEntries = entriesByMonth[month] ?: emptyList()
                            val moodStats = calculateMoodStats(monthEntries)

                            Tab(
                                selected = index == selectedMonthIndex,
                                onClick = {
                                    coroutineScope.launch {
                                        selectedMonthIndex = index
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = month.split(" ")[0], // Just show month name
                                            style = MaterialTheme.typography.labelMedium
                                        )

                                        // Mood indicator dots
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            moodStats.averageMood?.let { avg ->
                                                val moodColor = getMoodColor(avg.toInt())
                                                repeat(3) { // Show 3 dots for visual balance
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(moodColor.copy(
                                                                alpha = 0.7f - (it * 0.2f)
                                                            ))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Horizontal pager for swipe gestures between months
                    HorizontalPager(
                        state = pagerState,
                        count = months.size,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        val month = months[page]
                        val monthEntries = entriesByMonth[month] ?: emptyList()
                        val entriesByDay = monthEntries.groupBy { getDayFromTimestamp(it.timestamp) }

                        // Day separators with mood indicators
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            entriesByDay.forEach { (day, dayEntries) ->
                                val dayMoodStats = calculateMoodStats(dayEntries)

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mood indicator bar
                                        dayMoodStats.averageMood?.let { avg ->
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(32.dp)
                                                    .background(
                                                        getMoodColor(avg.toInt()),
                                                        RoundedCornerShape(2.dp)
                                                    )
                                                    .padding(end = 8.dp)
                                            )
                                        }

                                        // Date header
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = primaryColor
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Mood summary
                                        dayMoodStats.averageMood?.let { avg ->
                                            Text(
                                                text = "Avg: ${"%.1f".format(avg)}/5",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = getMoodColor(avg.toInt())
                                                ),
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }

                                // Entries for this day
                                itemsIndexed(dayEntries) { index, entry ->
                                    JournalEntryCard(
                                        entry = entry,
                                        surfaceColor = surfaceColor,
                                        textColor = textColor,
                                        primaryColor = primaryColor,
                                        onOpenModal = { selectedEntry = it }
                                    )

                                    // Ads logic (if you're using them)
                                    if (shouldLoadAds && shouldShowAd(index, dayEntries.size)) {
                                        val nativeAds = rememberNativeAdsManager(dayEntries.size)
                                        val adIndex = calculateAdIndex(index, dayEntries.size, nativeAds.size)
                                        nativeAds.getOrNull(adIndex)?.let { ad ->
                                            if (isValidNativeAd(ad)) {
                                                NativeAdViewComposable(
                                                    nativeAd = ad,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedEntry?.let { entry ->
            JournalEntryModal(
                entry = entry,
                onDismiss = { selectedEntry = null },
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
fun JournalEntryModal(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = primaryColor)
            }
        },
        title = {
            Text("Journal Entry", color = primaryColor)
        },
        text = {
            Column {
                Text(
                    text = formatDate(entry.timestamp),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                entry.moodRating?.let { rating ->
                    MoodIndicator(rating = rating)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    surfaceColor: Color,
    textColor: Color,
    primaryColor: Color,
    onOpenModal: (JournalEntry) -> Unit
) {
    val maxPreviewLines = 3
    var isExpanded by remember { mutableStateOf(false) }
    val lineCount = remember { entry.content.lineCount() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
            contentColor = textColor
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mood rating if available
            entry.moodRating?.let { rating ->
                MoodIndicator(rating = rating)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content with limited lines
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = if (isExpanded) Int.MAX_VALUE else maxPreviewLines,
                overflow = TextOverflow.Ellipsis
            )

            // Read More/Less button if content is long enough
            if (lineCount > maxPreviewLines) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text(if (isExpanded) "Read Less" else "Read More")
                }
            }

            // Button to open full modal view
            OutlinedButton(
                onClick = { onOpenModal(entry) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryColor
                )
            ) {
                Text("View Full Entry")
            }
        }
    }
}

@Composable
private fun MoodIndicator(rating: Int) {
    val moodColor = getMoodColor(rating)

    Box(
        modifier = Modifier
            .background(
                color = moodColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Mood: $rating/5",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Helper functions for date formatting
private fun getMonthYearFromTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getDayFromTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDate(ms: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}

// Helper class for mood statistics
private data class MoodStats(
    val averageMood: Float?,
    val lowestMood: Int?,
    val highestMood: Int?
)

// Calculate mood statistics for a group of entries
private fun calculateMoodStats(entries: List<JournalEntry>): MoodStats {
    val moodEntries = entries.filter { it.moodRating != null }
    if (moodEntries.isEmpty()) return MoodStats(null, null, null)

    val average = moodEntries.map { it.moodRating!! }.average().toFloat()
    val min = moodEntries.minOf { it.moodRating!! }
    val max = moodEntries.maxOf { it.moodRating!! }

    return MoodStats(average, min, max)
}

// Get color based on mood rating (1-5)
private fun getMoodColor(rating: Int): Color {
    return when (rating) {
        1 -> Color(0xFFEF5350) // Red for worst mood
        2 -> Color(0xFFFFA726) // Orange
        3 -> Color(0xFFFFEE58) // Yellow
        4 -> Color(0xFF66BB6A) // Green
        5 -> Color(0xFF42A5F5) // Blue for best mood
        else -> Color.LightGray
    }
}

private fun String.lineCount(): Int {
    return this.split("\n").size
}


@Composable
private fun rememberNativeAdsManager(entryCount: Int): List<NativeAd?> {
    val context = LocalContext.current
    val nativeAds = remember { mutableStateListOf<NativeAd?>() }

    LaunchedEffect(entryCount) {
        withContext(Dispatchers.IO) {
            val TEST_AD_UNIT_ID = "ca-app-pub-7848605130142110/1142941683"
            val adCount = if (entryCount in 1..4) 1
            else (entryCount / 5).coerceAtLeast(1)

            nativeAds.clear()
            nativeAds.addAll(List(adCount) { null })

            nativeAds.forEachIndexed { index, _ ->
                val adLoader = AdLoader.Builder(context, TEST_AD_UNIT_ID)
                    .forNativeAd { ad ->
                        // Verify ad meets requirements before displaying
                        if (isValidNativeAd(ad)) {
                            nativeAds[index] = ad
                        }
                    }
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                        .setRequestCustomMuteThisAd(true)
                        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                        .build())
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("AdMob", "Ad failed: ${error.message}")
                        }
                    })
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())
                if (index < adCount - 1) delay(1000)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAds.filterNotNull().forEach { it.destroy() }
        }
    }

    return nativeAds
}

private fun isValidNativeAd(ad: NativeAd): Boolean {
    return ad.headline != null &&
            ad.body != null &&
            ad.callToAction != null &&
            ad.icon != null &&
            ad.mediaContent != null
}

@Composable
fun NativeAdViewComposable(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color.White,
    textColor: Color = Color.Black,
    primaryColor: Color = Color(0xFF6200EE)
) {
    val surfaceColorInt = surfaceColor.toArgb()
    val textColorInt = textColor.toArgb()
    val primaryColorInt = primaryColor.toArgb()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp)
    ) {
        AndroidView(
            factory = { context ->
                NativeAdView(context).apply {
                    val layout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setBackgroundColor(surfaceColorInt)
                        setPadding(16, 16, 16, 16)
                    }

                    val mediaView = MediaView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            context.resources.displayMetrics.widthPixels * 7 / 16 // 16:9 ratio
                        )

                    }

                    val headlineAndIconLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 8)
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val iconView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(64.dpToPx(context), 64.dpToPx(context)).apply {
                            marginEnd = 12.dpToPx(context)
                        }
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    val headlineView = TextView(context).apply {
                        textSize = 18f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(textColorInt)
                    }

                    headlineAndIconLayout.addView(iconView)
                    headlineAndIconLayout.addView(headlineView)

                    val bodyView = TextView(context).apply {
                        textSize = 14f
                        setPadding(0, 8, 0, 8)
                        setTextColor(textColorInt.copyAlpha(0.7f))
                    }

                    val callToActionView = Button(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 12.dpToPx(context)
                        }
                        setBackgroundColor(primaryColorInt)
                        setTextColor(surfaceColorInt)
                        text = "Learn More"
                        setPadding(24, 12, 24, 12)
                    }

                    layout.addView(mediaView)
                    layout.addView(headlineAndIconLayout)
                    layout.addView(bodyView)
                    layout.addView(callToActionView)

                    addView(layout)

                    // Bind ad views
                    this.mediaView = mediaView
                    this.headlineView = headlineView
                    this.bodyView = bodyView
                    this.callToActionView = callToActionView
                    this.iconView = iconView
                    this.advertiserView = null // optional

                    populateNativeAdView(nativeAd, this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

// Helper to convert dp to px
private fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

// Helper to copy alpha
private fun Int.copyAlpha(alpha: Float): Int {
    val color = Color(this)
    return color.copy(alpha = alpha).toArgb()
}



private fun setViewLayout(parent: NativeAdView, mediaView: MediaView) {
    parent.removeAllViews()

    val container = LinearLayout(parent.context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )
    }

    // Add views in proper order
    container.addView(parent.headlineView)
    container.addView(parent.advertiserView)
    container.addView(mediaView)
    container.addView(parent.bodyView)
    container.addView(parent.callToActionView)

    parent.addView(container)
    parent.mediaView = mediaView
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Set all required attributes
    (adView.headlineView as? TextView)?.text = nativeAd.headline
    (adView.bodyView as? TextView)?.text = nativeAd.body
    (adView.callToActionView as? Button)?.text = nativeAd.callToAction
    (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
    (adView.advertiserView as? TextView)?.text = nativeAd.advertiser ?: "Advertisement"

    // Required: Set native ad and media content
    adView.setNativeAd(nativeAd)
    if (nativeAd.mediaContent != null) {
        adView.mediaView?.setMediaContent(nativeAd.mediaContent)
    }
}
// Helper functions
private fun shouldShowAd(currentIndex: Int, totalEntries: Int): Boolean {
    return if (totalEntries < 5) {
        currentIndex == totalEntries - 1
    } else {
        (currentIndex + 1) % 5 == 0 || currentIndex == totalEntries - 1
    }
}

private fun calculateAdIndex(currentIndex: Int, totalEntries: Int, totalAds: Int): Int {
    return if (totalEntries < 5) {
        0
    } else {
        ((currentIndex + 1) / 5).coerceAtMost(totalAds - 1)
    }
}


@Composable
fun CustomModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    entry: JournalEntry,
    primaryColor: Color
) {
    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismissRequest),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 340.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Journal Entry",
                        style = MaterialTheme.typography.titleLarge,
                        color = primaryColor
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date section
                Text(
                    "Date:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    formatDate(entry.timestamp),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Mood rating (if available)
                entry.moodRating?.let { rating ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mood:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    MoodIndicator(rating = rating)
                }

                // Content section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Content:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    entry.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}


