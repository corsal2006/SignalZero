package com.signalzero.app.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.Incident
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.MeshCyan
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    currentLat: Double?,
    currentLng: Double?,
    incidents: List<Incident>,
    language: AppLanguage,
    onIncidentSelected: (Incident) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize Osmdroid configuration
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "SignalZero/1.0"
    }

    val validIncidents = incidents.filter { it.latitude != null && it.longitude != null }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    val center = if (currentLat != null && currentLng != null) {
                        GeoPoint(currentLat, currentLng)
                    } else if (validIncidents.isNotEmpty()) {
                        GeoPoint(validIncidents.first().latitude!!, validIncidents.first().longitude!!)
                    } else {
                        GeoPoint(28.6139, 77.2090) // Default New Delhi coordinates
                    }
                    controller.setCenter(center)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // Current Device Location Marker
                if (currentLat != null && currentLng != null) {
                    val myMarker = Marker(mapView).apply {
                        position = GeoPoint(currentLat, currentLng)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "My Location"
                    }
                    mapView.overlays.add(myMarker)
                }

                // Incident Markers
                for (incident in validIncidents) {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(incident.latitude!!, incident.longitude!!)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "${incident.category.getDisplayName(language)}: ${incident.id}"
                        snippet = incident.message
                        setOnMarkerClickListener { _, _ ->
                            onIncidentSelected(incident)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Fallback banner if GPS is unavailable
        if (currentLat == null || currentLng == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPS Acquiring — Displaying Saved Offline Incident Pins",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
