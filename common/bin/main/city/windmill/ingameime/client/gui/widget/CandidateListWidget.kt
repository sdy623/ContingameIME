package city.windmill.ingameime.client.gui.widget

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

class CandidateListWidget(font: Font) : Widget(font) {
    var candidates: Array<String>? = null
    var selectedIndex: Int = -1
    var highlightColor: Int = 0xFF_C9_DD_FF.toInt()
    
    private val drawItem = CandidateEntry(font)
    
    override val active get() = !candidates.isNullOrEmpty()
    override val width
        get() = super.width + candidates!!.sumOf { s -> drawItem.apply { this.text = s }.width }
    override val height
        get() = super.height + font.lineHeight
    override val padding: Pair<Int, Int>
        get() = 1 to 1
    
    @Suppress("NAME_SHADOWING")
    override fun draw(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int, delta: Float) {
        candidates?.let {
            super.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY, delta)
            
            var offsetX = offsetX + padding.first
            val offsetY = offsetY + padding.second
            var index = 1
            for (str in it) {
                drawItem.index = index
                drawItem.text = str
                drawItem.selected = (index - 1) == selectedIndex
                drawItem.highlightColor = highlightColor
                drawItem.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY, delta)
                offsetX += drawItem.width
                index++
            }
        }
    }
    
    class CandidateEntry(font: Font) : Widget(font) {
        var text: String? = null
        var index = 0
        var selected = false
        var highlightColor: Int = 0xFF_C9_DD_FF.toInt()
        
        private val indexWidth = font.width("00") + 5
        
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        override val width
            get() = super.width + font.width(text ?: "") + indexWidth
        override val height
            get() = super.height + font.lineHeight
        override val padding: Pair<Int, Int>
            get() = 2 to 1
        
        @Suppress("NAME_SHADOWING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        override fun draw(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int, delta: Float) {
            if (selected) {
                guiGraphics.fill(
                    offsetX,
                    offsetY,
                    offsetX + width,
                    offsetY + height,
                    highlightColor
                )
            }
            var offsetX = offsetX + padding.first
            drawCenteredString(guiGraphics, font, index.toString(), offsetX + indexWidth / 2, offsetY, textColor)
            offsetX += indexWidth
            guiGraphics.drawString(font, text, offsetX, offsetY, textColor, false)
        }
        
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        private fun drawCenteredString(
            guiGraphics: GuiGraphics,
            font: Font,
            text: String?,
            centerX: Int,
            y: Int,
            color: Int
        ) {
            guiGraphics.drawString(
                font, text,
                (centerX - font.width(text) / 2), y, color, false
            )
        }
        
    }
}