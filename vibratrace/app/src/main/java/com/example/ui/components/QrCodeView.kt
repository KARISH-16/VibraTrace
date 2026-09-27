package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    // Generate deterministic 25x25 QR-like matrix from string content
    val matrix = remember(content) {
        generateQrMatrix(content, 25)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val moduleCount = matrix.size
                val cellSize = this.size.width / moduleCount

                for (r in 0 until moduleCount) {
                    for (c in 0 until moduleCount) {
                        if (matrix[r][c]) {
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(data: String, dimension: Int): Array<BooleanArray> {
    val grid = Array(dimension) { BooleanArray(dimension) }

    // Finder pattern in corners (7x7)
    fun drawFinder(topRow: Int, leftCol: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                grid[topRow + r][leftCol + c] = isOuter || isInner
            }
        }
    }

    drawFinder(0, 0) // Top-Left
    drawFinder(0, dimension - 7) // Top-Right
    drawFinder(dimension - 7, 0) // Bottom-Left

    // Timing patterns
    for (i in 7 until dimension - 7) {
        grid[6][i] = (i % 2 == 0)
        grid[i][6] = (i % 2 == 0)
    }

    // Hash data fill
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(data.toByteArray())
    var bitIndex = 0

    for (r in 0 until dimension) {
        for (c in 0 until dimension) {
            val inFinder1 = r < 8 && c < 8
            val inFinder2 = r < 8 && c >= dimension - 8
            val inFinder3 = r >= dimension - 8 && c < 8
            val inTiming = r == 6 || c == 6

            if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                val bytePos = (bitIndex / 8) % hash.size
                val bitPos = bitIndex % 8
                val bitVal = ((hash[bytePos].toInt() shr bitPos) and 1) == 1
                grid[r][c] = bitVal xor ((r + c) % 3 == 0)
                bitIndex++
            }
        }
    }

    return grid
}
