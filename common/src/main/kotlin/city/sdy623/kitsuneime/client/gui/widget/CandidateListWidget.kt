package city.sdy623.kitsuneime.client.gui.widget

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

class CandidateListWidget(font: Font) : Widget(font) {
    enum class Layout {
        HORIZONTAL,
        VERTICAL,
    }

    enum class SelectionStyle {
        BACKGROUND,
        TEXT_COLOR,
    }

    enum class PanelPreset {
        LIGHT,
        DARK,
        CUSTOM,
    }

    enum class CandidatePreset {
        LIGHT,
        DARK,
        CUSTOM,
    }

    companion object {
        const val DEFAULT_PANEL_BG_LIGHT: Int = 0xEB_EB_EB_EB.toInt()
        const val DEFAULT_PANEL_BORDER_LIGHT: Int = 0xFF_B8_B8_B8.toInt()
        const val DEFAULT_TEXT_LIGHT: Int = 0xFF_8F_B8_FF.toInt()

        const val DEFAULT_PANEL_BG_DARK: Int = 0xF0_10_00_10.toInt()
        const val DEFAULT_PANEL_BORDER_DARK: Int = 0xFF_52_45_74.toInt()
        const val DEFAULT_TEXT_DARK: Int = 0xFF_C5_C5_C5.toInt()

        const val DEFAULT_TEXT_COLOR_STYLE_PANEL_BG: Int = 0xFF_0E_18_19.toInt()
        const val DEFAULT_TEXT_COLOR_STYLE_TEXT: Int = 0xFF_C5_C5_C5.toInt()
        const val DEFAULT_TEXT_COLOR_STYLE_SELECTED_TEXT: Int = 0xFF_0F_F7_96.toInt()

        const val DEFAULT_SELECTED_BG_LIGHT: Int = 0xFF_D9_D9_D9.toInt()
        const val DEFAULT_SELECTED_BG_DARK: Int = 0xFF_37_37_42.toInt()
        const val DEFAULT_SELECTED_TEXT_LIGHT: Int = 0xFF_11_11_11.toInt()
        const val DEFAULT_SELECTED_TEXT_DARK: Int = 0xFF_9E_C8_FF.toInt()
    }

    var candidates: Array<String>? = null
    var selectedIndex: Int = -1

    var layout: Layout = Layout.VERTICAL
    var selectionStyle: SelectionStyle = SelectionStyle.BACKGROUND
    var selectionHighlightEnabled: Boolean = true

    var panelPreset: PanelPreset = PanelPreset.LIGHT
        set(value) {
            field = value
            when (value) {
                PanelPreset.LIGHT -> {
                    panelBackgroundColor = DEFAULT_PANEL_BG_LIGHT
                    panelBorderColor = DEFAULT_PANEL_BORDER_LIGHT
                    candidateTextColor = DEFAULT_TEXT_LIGHT
                }

                PanelPreset.DARK -> {
                    panelBackgroundColor = DEFAULT_PANEL_BG_DARK
                    panelBorderColor = DEFAULT_PANEL_BORDER_DARK
                    candidateTextColor = DEFAULT_TEXT_DARK
                }

                PanelPreset.CUSTOM -> Unit
            }
        }

    var candidatePreset: CandidatePreset = CandidatePreset.LIGHT
        set(value) {
            field = value
            when (value) {
                CandidatePreset.LIGHT -> {
                    selectionBackgroundColor = DEFAULT_SELECTED_BG_LIGHT
                    selectedTextColor = DEFAULT_SELECTED_TEXT_LIGHT
                }

                CandidatePreset.DARK -> {
                    selectionBackgroundColor = DEFAULT_SELECTED_BG_DARK
                    selectedTextColor = DEFAULT_SELECTED_TEXT_DARK
                }

                CandidatePreset.CUSTOM -> Unit
            }
        }

    var panelBackgroundColor: Int = DEFAULT_PANEL_BG_LIGHT
    var panelBorderColor: Int = DEFAULT_PANEL_BORDER_LIGHT
    var borderEnabled: Boolean = true
    var roundedEnabled: Boolean = false
    var cornerRadius: Int = 3

    var candidateTextColor: Int = DEFAULT_TEXT_LIGHT
    var selectedTextColor: Int = DEFAULT_SELECTED_TEXT_LIGHT
    var selectionBackgroundColor: Int = DEFAULT_SELECTED_BG_LIGHT

    private val panelPadding = 4 to 4
    private val itemPadding = 3 to 2
    private val itemGap = 1
    private val indexWidth = font.width("00") + 6

    override val active get() = !candidates.isNullOrEmpty()
    override val width
        get() = if (!active) super.width else {
            val list = candidates!!
            when (layout) {
                Layout.VERTICAL -> panelPadding.first * 2 + maxItemWidth(list)
                Layout.HORIZONTAL -> {
                    val totalItemWidth = list.sumOf { itemWidth(it) }
                    val totalGap = itemGap * (list.size - 1)
                    panelPadding.first * 2 + totalItemWidth + totalGap
                }
            }
        }
    override val height
        get() = if (!active) super.height else {
            val count = candidates!!.size
            when (layout) {
                Layout.VERTICAL -> panelPadding.second * 2 + count * itemHeight() + itemGap * (count - 1)
                Layout.HORIZONTAL -> panelPadding.second * 2 + itemHeight()
            }
        }
    override val padding: Pair<Int, Int>
        get() = panelPadding
    
    @Suppress("NAME_SHADOWING")
    override fun draw(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int, delta: Float) {
        val list = candidates ?: return
        drawPanelBackground(guiGraphics, offsetX, offsetY, width, height)

        val itemHeight = itemHeight()
        when (layout) {
            Layout.VERTICAL -> {
                val maxItemWidth = maxItemWidth(list)
                var itemY = offsetY + panelPadding.second
                list.forEachIndexed { index, text ->
                    val itemX = offsetX + panelPadding.first
                    drawCandidateItem(guiGraphics, itemX, itemY, maxItemWidth, itemHeight, index, text)
                    itemY += itemHeight + itemGap
                }
            }

            Layout.HORIZONTAL -> {
                val itemY = offsetY + panelPadding.second
                var itemX = offsetX + panelPadding.first
                list.forEachIndexed { index, text ->
                    val width = itemWidth(text)
                    drawCandidateItem(guiGraphics, itemX, itemY, width, itemHeight, index, text)
                    itemX += width + itemGap
                }
            }
        }
    }

    private fun itemHeight(): Int = font.lineHeight + itemPadding.second * 2

    private fun itemWidth(text: String): Int = indexWidth + itemPadding.first * 2 + font.width(text)

    private fun maxItemWidth(list: Array<String>): Int = list.maxOfOrNull { itemWidth(it) } ?: 0

    private fun drawCandidateItem(
        guiGraphics: GuiGraphics,
        itemX: Int,
        itemY: Int,
        itemWidth: Int,
        itemHeight: Int,
        index: Int,
        text: String,
    ) {
        val isSelected = index == selectedIndex
        val useTextColorForSelection = !selectionHighlightEnabled || selectionStyle == SelectionStyle.TEXT_COLOR
        if (isSelected && !useTextColorForSelection) {
            fillRoundedRect(
                guiGraphics,
                itemX,
                itemY,
                itemX + itemWidth,
                itemY + itemHeight,
                selectionBackgroundColor,
                roundedEnabled,
                cornerRadius.coerceAtLeast(0)
            )
        }

        val indexText = (index + 1).toString()
        val indexTextX = itemX + itemPadding.first + (indexWidth - font.width(indexText)) / 2
        val textBaseY = itemY + itemPadding.second
        val currentTextColor = if (isSelected && useTextColorForSelection) {
            selectedTextColor
        } else {
            candidateTextColor
        }

        guiGraphics.drawString(font, indexText, indexTextX, textBaseY, currentTextColor, false)

        val textX = itemX + itemPadding.first + indexWidth
        guiGraphics.drawString(font, text, textX, textBaseY, currentTextColor, false)
    }

    private fun drawPanelBackground(guiGraphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int) {
        val right = x + width
        val bottom = y + height
        val radius = cornerRadius.coerceAtLeast(0)

        if (borderEnabled) {
            fillRoundedRect(guiGraphics, x, y, right, bottom, panelBorderColor, roundedEnabled, radius)
            fillRoundedRect(guiGraphics, x + 1, y + 1, right - 1, bottom - 1, panelBackgroundColor, roundedEnabled, (radius - 1).coerceAtLeast(0))
        } else {
            fillRoundedRect(guiGraphics, x, y, right, bottom, panelBackgroundColor, roundedEnabled, radius)
        }
    }

    private fun fillRoundedRect(
        guiGraphics: GuiGraphics,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        color: Int,
        rounded: Boolean,
        radius: Int,
    ) {
        if (right <= left || bottom <= top) return

        if (!rounded || radius <= 0) {
            guiGraphics.fill(left, top, right, bottom, color)
            return
        }

        val width = right - left
        val height = bottom - top
        val safeRadius = radius.coerceAtMost((width / 2).coerceAtLeast(1)).coerceAtMost((height / 2).coerceAtLeast(1))
        if (safeRadius <= 0) {
            guiGraphics.fill(left, top, right, bottom, color)
            return
        }

        guiGraphics.fill(left + safeRadius, top, right - safeRadius, bottom, color)
        guiGraphics.fill(left, top + safeRadius, left + safeRadius, bottom - safeRadius, color)
        guiGraphics.fill(right - safeRadius, top + safeRadius, right, bottom - safeRadius, color)

        val r = safeRadius - 1
        val rr = r * r
        for (dx in 0..r) {
            for (dy in 0..r) {
                if (dx * dx + dy * dy > rr) continue
                val x1 = left + safeRadius - 1 - dx
                val y1 = top + safeRadius - 1 - dy
                val x2 = right - safeRadius + dx
                val y2 = bottom - safeRadius + dy

                guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color)
                guiGraphics.fill(x2, y1, x2 + 1, y1 + 1, color)
                guiGraphics.fill(x1, y2, x1 + 1, y2 + 1, color)
                guiGraphics.fill(x2, y2, x2 + 1, y2 + 1, color)
            }
        }
    }
}