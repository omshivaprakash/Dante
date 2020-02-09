package at.shockbytes.dante.camera.overlay

import android.graphics.RectF
import android.util.Size
import kotlin.math.ceil
import kotlin.math.max

/**
 * Allows the tracked ArObject to be mapped to a other coordinate system, e.g. a view.
 *
 * We use this to map the coordinate system of the preview image we received from the camera API to the ArOverlayView's
 * coordinate system which will have an other size.
 */
class PositionTranslator(
    private val targetWidth: Int,
    private val targetHeight: Int
) {

    fun processObject(barcodeObject: BarcodeObject): BarcodeObject {

            // Rotate Size
            val rotatedSize = when (barcodeObject.sourceRotationDegrees) {
                90, 270 -> Size(barcodeObject.sourceSize.height, barcodeObject.sourceSize.width)
                0, 180 -> barcodeObject.sourceSize
                else -> throw IllegalArgumentException("Unsupported rotation. Must be 0, 90, 180 or 270")
            }

            // Calculate scale
            val scaleX = targetWidth / rotatedSize.width.toDouble()
            val scaleY = targetHeight / rotatedSize.height.toDouble()
            val scale = max(scaleX, scaleY)
            val scaleF = scale.toFloat()
            val scaledSize = Size(ceil(rotatedSize.width * scale).toInt(), ceil(rotatedSize.height * scale).toInt())

            // Calculate offset (we need to center the overlay on the target)
            val offsetX = (targetWidth - scaledSize.width) / 2
            val offsetY = (targetHeight - scaledSize.height) / 2

            // Map bounding box
            val mappedBoundingBox = RectF().apply {
                left = barcodeObject.boundingBox.right * scaleF + offsetX
                top = barcodeObject.boundingBox.top * scaleF + offsetY
                right = barcodeObject.boundingBox.left * scaleF + offsetX
                bottom = barcodeObject.boundingBox.bottom * scaleF + offsetY
            }

            return barcodeObject.copy(
                    boundingBox = mappedBoundingBox,
                    sourceSize = Size(targetWidth, targetHeight)
            )
    }
}