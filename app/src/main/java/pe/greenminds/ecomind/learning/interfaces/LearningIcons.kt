package pe.greenminds.ecomind.learning.interfaces

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val DownloadIcon: ImageVector by lazy {
    ImageVector.Builder("download", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 16f); lineTo(7f, 11f); lineTo(8.4f, 9.55f)
            lineTo(11f, 12.15f); verticalLineTo(4f); horizontalLineTo(13f)
            verticalLineTo(12.15f); lineTo(15.6f, 9.55f); lineTo(17f, 11f)
            lineTo(12f, 16f); close()
            moveTo(6f, 20f); quadTo(5.18f, 20f, 4.59f, 19.41f)
            quadTo(4f, 18.82f, 4f, 18f); verticalLineTo(15f); horizontalLineTo(6f)
            verticalLineTo(18f); horizontalLineTo(18f); verticalLineTo(15f)
            horizontalLineTo(20f); verticalLineTo(18f); quadTo(20f, 18.82f, 19.41f, 19.41f)
            quadTo(18.82f, 20f, 18f, 20f); horizontalLineTo(6f); close()
        }
    }.build()
}

internal val FavoriteIcon: ImageVector by lazy {
    ImageVector.Builder("favorite", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 21f); lineTo(10.55f, 19.7f)
            quadTo(8.03f, 17.43f, 6.38f, 15.78f); quadTo(4.73f, 14.13f, 3.75f, 12.81f)
            quadTo(2.78f, 11.5f, 2.39f, 10.4f); quadTo(2f, 9.3f, 2f, 8.15f)
            quadTo(2f, 5.8f, 3.58f, 4.22f); quadTo(5.15f, 2.65f, 7.5f, 2.65f)
            quadTo(8.8f, 2.65f, 9.98f, 3.2f); quadTo(11.15f, 3.75f, 12f, 4.75f)
            quadTo(12.85f, 3.75f, 14.03f, 3.2f); quadTo(15.2f, 2.65f, 16.5f, 2.65f)
            quadTo(18.85f, 2.65f, 20.43f, 4.22f); quadTo(22f, 5.8f, 22f, 8.15f)
            quadTo(22f, 9.3f, 21.61f, 10.4f); quadTo(21.22f, 11.5f, 20.25f, 12.81f)
            quadTo(19.28f, 14.12f, 17.62f, 15.77f); quadTo(15.97f, 17.42f, 13.45f, 19.69f)
            lineTo(12f, 21f); close()
            moveTo(12f, 18.3f); quadTo(14.4f, 16.15f, 15.95f, 14.61f)
            quadTo(17.5f, 13.07f, 18.4f, 11.94f); quadTo(19.3f, 10.8f, 19.65f, 9.91f)
            quadTo(20f, 9.02f, 20f, 8.15f); quadTo(20f, 6.65f, 19f, 5.65f)
            quadTo(18f, 4.65f, 16.5f, 4.65f); quadTo(15.33f, 4.65f, 14.33f, 5.31f)
            quadTo(13.33f, 5.97f, 12.95f, 7f); horizontalLineTo(11.05f)
            quadTo(10.68f, 5.97f, 9.68f, 5.31f); quadTo(8.68f, 4.65f, 7.5f, 4.65f)
            quadTo(6f, 4.65f, 5f, 5.65f); quadTo(4f, 6.65f, 4f, 8.15f)
            quadTo(4f, 9.02f, 4.35f, 9.91f); quadTo(4.7f, 10.8f, 5.6f, 11.94f)
            quadTo(6.5f, 13.07f, 8.05f, 14.61f); quadTo(9.6f, 16.15f, 12f, 18.3f)
            close()
        }
    }.build()
}
