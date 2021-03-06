package dev.federicocapece.drawzone.drawables

import dev.federicocapece.drawzone.Drawable
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint

class Rect(
        override var x: Double,
        override var y: Double,
        override var width: Double,
        override var height: Double,
        var color: Paint = Color.BLACK
): Drawable {

    override fun draw(g: GraphicsContext) {
        g.fill = color
        g.fillRect(x, y, width, height)
    }

}