package pe.greenminds.ecomind.community.presentation.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import pe.greenminds.ecomind.R
import android.view.ViewGroup

@Composable
fun EventLocationMap(
    latitude: String,
    longitude: String,
    enabled: Boolean,
    onPointSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier.background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Leaflet")
        }
        return
    }

    val error = stringResource(R.string.event_map_failed)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            EventMapView(context, error)
        },
        update = { view ->
            view.interactionEnabled = enabled
            view.onPointSelected = onPointSelected
            view.updatePoint(latitude, longitude)
        },
        onRelease = { it.release() }
    )
}

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
private class EventMapView(
    context: Context,
    error: String
) : WebView(context) {

    var interactionEnabled = true
    var onPointSelected: (Double, Double) -> Unit = { _, _ -> }

    private var ready = false
    private var released = false
    private var currentPoint = "null,null"
    private var lastSentPoint: String? = null

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.javaScriptEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString =
            settings.userAgentString + " EcoMindAndroid/1.0"

        addJavascriptInterface(LocationBridge(), "EventLocation")

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                ready = true
                lastSentPoint = null
                syncPoint()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url

                val allowedHost = uri.host in setOf(
                    "www.openstreetmap.org",
                    "leafletjs.com"
                )

                if (
                    request.isForMainFrame &&
                    uri.scheme == "https" &&
                    allowedHost
                ) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, uri)
                        )
                    }
                }

                return true
            }
        }

        loadDataWithBaseURL(
            "https://appassets.androidplatform.net/community/event-map/",
            EventMapHtml(error),
            "text/html",
            "UTF-8",
            null
        )
    }

    fun updatePoint(latitude: String, longitude: String) {
        val lat = latitude.replace(',', '.').toDoubleOrNull()
        val lng = longitude.replace(',', '.').toDoubleOrNull()

        currentPoint = if (
            lat != null && lng != null &&
            lat.isFinite() && lng.isFinite() &&
            lat in -90.0..90.0 && lng in -180.0..180.0
        ) {
            "$lat,$lng"
        } else {
            "null,null"
        }

        syncPoint()
    }

    private fun syncPoint() {
        if (!ready || released || lastSentPoint == currentPoint) return

        lastSentPoint = currentPoint

        evaluateJavascript(
            "window.setEventPoint && window.setEventPoint($currentPoint);",
            null
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!interactionEnabled) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                parent?.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                parent?.requestDisallowInterceptTouchEvent(false)
        }

        return super.onTouchEvent(event)
    }

    fun release() {
        released = true
        onPointSelected = { _, _ -> }
        removeJavascriptInterface("EventLocation")
        stopLoading()
        destroy()
    }

    private inner class LocationBridge {
        @JavascriptInterface
        fun selected(latitude: Double, longitude: Double) {
            if (!latitude.isFinite() || !longitude.isFinite()) return
            if (latitude !in -90.0..90.0) return
            if (longitude !in -180.0..180.0) return

            post {
                if (!released && interactionEnabled) {
                    onPointSelected(latitude, longitude)
                }
            }
        }
    }
}