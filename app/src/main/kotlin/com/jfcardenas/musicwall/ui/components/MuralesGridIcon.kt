package com.jfcardenas.musicwall.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 2×2 grid icon (Material GridView equivalent) without material-icons-extended. */
val MuralesGridIcon: ImageVector
    get() = _MuralesGridIcon

private var _MuralesGridIcon: ImageVector = buildMuralesGridIcon()

private fun buildMuralesGridIcon(): ImageVector = ImageVector.Builder(
    name = "MuralesGrid",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(3f, 3f)
        verticalLineToRelative(8f)
        horizontalLineToRelative(8f)
        verticalLineTo(3f)
        close()
        moveTo(11f, 19f)
        horizontalLineTo(3f)
        verticalLineToRelative(-8f)
        horizontalLineToRelative(8f)
        verticalLineToRelative(8f)
        close()
        moveTo(21f, 19f)
        horizontalLineToRelative(-8f)
        verticalLineToRelative(-8f)
        horizontalLineToRelative(8f)
        verticalLineToRelative(8f)
        close()
        moveTo(21f, 11f)
        verticalLineTo(3f)
        horizontalLineToRelative(-8f)
        verticalLineToRelative(8f)
        horizontalLineToRelative(8f)
        close()
    }
}.build()
