package city.sdy623.kitsuneime.client.handler

import city.sdy623.kitsuneime.KitsuneIMEClient
import city.sdy623.kitsuneime.client.ChatScreenHelper
import city.sdy623.kitsuneime.client.gui.OverlayScreen
import city.sdy623.kitsuneime.client.gui.widget.CandidateListWidget
import city.sdy623.kitsuneime.client.jni.ExternalBaseIME
import city.sdy623.kitsuneime.client.jni.ICommitListener
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.outputStream
import kotlin.io.path.reader

object ConfigHandler {
    var disableIMEInCommandMode = false
        set(value) {
            if (field != value)
                if (value) {
                    //Disable -> Enable
                    ScreenHandler.ScreenState.EditState.apply {
                        iEditstateListener = IEditStateListener { state ->
                            if (state == ScreenHandler.ScreenState.EditState.EDIT_OPEN
                                && ScreenHandler.ScreenState.currentScreen is ChatScreen
                                && ChatScreenHelper.getInitial(ScreenHandler.ScreenState.currentScreen as ChatScreen) == "/"
                            ) {
                                //Disable IME in Command Mode
                                IMEHandler.IMEState.onEditState(ScreenHandler.ScreenState.EditState.NULL_EDIT)
                                return@IEditStateListener
                            }
                            IMEHandler.IMEState.onEditState(state)
                        }
                    }
                } else {
                    //Enable -> Disable
                    ScreenHandler.ScreenState.EditState.apply {
                        iEditstateListener = IMEHandler.IMEState
                    }
                }
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var autoReplaceSlashChar = false
        set(value) {
            if (field != value)
                if (value) {
                    ExternalBaseIME.iCommitListener = ICommitListener { commit ->
                        var result = commit
                        if (ScreenHandler.ScreenState.currentScreen is ChatScreen
                            && ScreenHandler.ScreenState.EditState.currentEdit is EditBox
                            && (ScreenHandler.ScreenState.EditState.currentEdit as EditBox).cursorPosition == 0
                            && commit.isNotEmpty() && slashCharArray.contains(commit[0])
                        ) {
                            //Change to command mode, replace the char /
                            result = "/${commit.substring(1)}"
                            //Disable IME in command mode
                            if (disableIMEInCommandMode)
                                IMEHandler.IMEState.onEditState(ScreenHandler.ScreenState.EditState.NULL_EDIT)
                        }
                        return@ICommitListener IMEHandler.IMEState.onCommit(result)
                    }
                } else {
                    ExternalBaseIME.iCommitListener = IMEHandler.IMEState
                }
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var slashCharArray = charArrayOf('、', '・')

    var candidateHighlightColor = 0xAA_87_99_DD.toInt()
        set(value) {
            field = value
            OverlayScreen.candidateHighlightColor = value
        }

    var candidateSelectedTextColor = 0xFF_0F_F7_96.toInt()
        set(value) {
            field = value
            OverlayScreen.candidateSelectedTextColor = value
        }

    var candidatePanelBackgroundColor = 0xFF_0E_18_19.toInt()
        set(value) {
            field = value
            OverlayScreen.candidatePanelBackgroundColor = value
        }

    var candidateTextColor = 0xFF_C5_C5_C5.toInt()
        set(value) {
            field = value
            OverlayScreen.candidateTextColor = value
        }

    var candidatePanelBorderColor = 0xFF_B8_B8_B8.toInt()
        set(value) {
            field = value
            OverlayScreen.candidatePanelBorderColor = value
        }

    var candidatePanelBorderEnabled = false
        set(value) {
            field = value
            OverlayScreen.candidatePanelBorderEnabled = value
        }

    var candidateRoundedEnabled = false
        set(value) {
            field = value
            OverlayScreen.candidateRoundedEnabled = value
        }

    var candidateCornerRadius = 3
        set(value) {
            field = value.coerceIn(0, 24)
            OverlayScreen.candidateCornerRadius = field
        }

    var candidateSelectionHighlightEnabled = true
        set(value) {
            field = value
            OverlayScreen.candidateSelectionHighlightEnabled = value
        }

    var candidateLayout = CandidateListWidget.Layout.VERTICAL
        set(value) {
            field = value
            OverlayScreen.candidateLayout = value
        }

    var candidateSelectionStyle = CandidateListWidget.SelectionStyle.TEXT_COLOR
        set(value) {
            field = value
            OverlayScreen.candidateSelectionStyle = value
        }

    var candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
        set(value) {
            field = value
            OverlayScreen.candidatePanelPreset = value
            when (value) {
                CandidateListWidget.PanelPreset.LIGHT -> {
                    candidatePanelBackgroundColor = CandidateListWidget.DEFAULT_PANEL_BG_LIGHT
                    candidatePanelBorderColor = CandidateListWidget.DEFAULT_PANEL_BORDER_LIGHT
                    candidateTextColor = CandidateListWidget.DEFAULT_TEXT_LIGHT
                }

                CandidateListWidget.PanelPreset.DARK -> {
                    candidatePanelBackgroundColor = CandidateListWidget.DEFAULT_PANEL_BG_DARK
                    candidatePanelBorderColor = CandidateListWidget.DEFAULT_PANEL_BORDER_DARK
                    candidateTextColor = CandidateListWidget.DEFAULT_TEXT_DARK
                }

                CandidateListWidget.PanelPreset.CUSTOM -> Unit
            }
        }

    var candidatePreset = CandidateListWidget.CandidatePreset.CUSTOM
        set(value) {
            field = value
            OverlayScreen.candidatePreset = value
            when (value) {
                CandidateListWidget.CandidatePreset.LIGHT -> {
                    candidateHighlightColor = CandidateListWidget.DEFAULT_SELECTED_BG_LIGHT
                    candidateSelectedTextColor = CandidateListWidget.DEFAULT_SELECTED_TEXT_LIGHT
                }

                CandidateListWidget.CandidatePreset.DARK -> {
                    candidateHighlightColor = CandidateListWidget.DEFAULT_SELECTED_BG_DARK
                    candidateSelectedTextColor = CandidateListWidget.DEFAULT_SELECTED_TEXT_DARK
                }

                CandidateListWidget.CandidatePreset.CUSTOM -> Unit
            }
        }

    var forceFullscreenCandidateInWindowed = true

    private val config = Paths.get(
        Minecraft.getInstance().gameDirectory.toString(),
        "config", "ingameime.json"
    )
    private val LOGGER = LogManager.getFormatterLogger(KitsuneIMEClient.MODNAME + "|Config")!!

    fun initialConfig() {
        readConfig()
    }

    fun loadDefaultConfig() {
        disableIMEInCommandMode = true
        autoReplaceSlashChar = true
        slashCharArray = charArrayOf('、', '・')
        candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
        candidatePreset = CandidateListWidget.CandidatePreset.CUSTOM
        candidateHighlightColor = 0xAA_87_99_DD.toInt()
        candidateSelectedTextColor = 0xFF_0F_F7_96.toInt()
        candidatePanelBackgroundColor = 0xFF_0E_18_19.toInt()
        candidateTextColor = 0xFF_C5_C5_C5.toInt()
        candidatePanelBorderColor = 0xFF_B8_B8_B8.toInt()
        candidatePanelBorderEnabled = false
        candidateRoundedEnabled = false
        candidateCornerRadius = 3
        candidateSelectionHighlightEnabled = true
        candidateLayout = CandidateListWidget.Layout.HORIZONTAL
        candidateSelectionStyle = CandidateListWidget.SelectionStyle.TEXT_COLOR
        forceFullscreenCandidateInWindowed = true
    }

    fun readConfig() {
        try {
            JsonParser().parse(JsonReader(config.reader())).apply {
                disableIMEInCommandMode = (this as JsonObject).get("disableIMEInCommandMode").asBoolean
                autoReplaceSlashChar = this.get("autoReplaceSlashChar").asBoolean
                slashCharArray = this.get("slashChars").asJsonArray.map { it.asCharacter }.toCharArray()
                candidateHighlightColor = parseColor(this.get("candidateHighlightColor")?.asString)
                candidateSelectedTextColor = parseColor(this.get("candidateSelectedTextColor")?.asString, CandidateListWidget.DEFAULT_SELECTED_TEXT_LIGHT)
                candidatePanelBackgroundColor = parseColor(this.get("candidatePanelBackgroundColor")?.asString, CandidateListWidget.DEFAULT_PANEL_BG_LIGHT)
                candidateTextColor = parseColor(this.get("candidateTextColor")?.asString, CandidateListWidget.DEFAULT_TEXT_LIGHT)
                candidatePanelBorderColor = parseColor(this.get("candidatePanelBorderColor")?.asString, CandidateListWidget.DEFAULT_PANEL_BORDER_LIGHT)
                candidatePanelBorderEnabled = this.get("candidatePanelBorderEnabled")?.asBoolean ?: true
                candidateRoundedEnabled = this.get("candidateRoundedEnabled")?.asBoolean ?: false
                candidateCornerRadius = this.get("candidateCornerRadius")?.asInt ?: 3
                candidateSelectionHighlightEnabled = this.get("candidateSelectionHighlightEnabled")?.asBoolean ?: true
                candidateLayout = parseCandidateLayout(this.get("candidateLayout")?.asString)
                candidateSelectionStyle = parseSelectionStyle(this.get("candidateSelectionStyle")?.asString)
                candidatePanelPreset = parsePanelPreset(this.get("candidatePanelPreset")?.asString)
                candidatePreset = parseCandidatePreset(this.get("candidatePreset")?.asString)
                forceFullscreenCandidateInWindowed = this.get("forceFullscreenCandidateInWindowed")?.asBoolean ?: false
            }
        } catch (e: Exception) {
            LOGGER.warn("Failed to read config:", e)
            LOGGER.warn("Loading Default config")
            loadDefaultConfig()
        }
        saveConfig()
    }

    fun saveConfig() {
        config.outputStream(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ).bufferedWriter().apply {
            write(
                GsonBuilder().setPrettyPrinting().create().toJson(
                    JsonObject().apply {
                        addProperty("disableIMEInCommandMode", disableIMEInCommandMode)
                        addProperty("autoReplaceSlashChar", autoReplaceSlashChar)
                        add("slashChars", JsonArray().apply { slashCharArray.onEach(::add) })
                        addProperty("candidateHighlightColor", formatColor(candidateHighlightColor))
                        addProperty("candidateSelectedTextColor", formatColor(candidateSelectedTextColor))
                        addProperty("candidatePanelBackgroundColor", formatColor(candidatePanelBackgroundColor))
                        addProperty("candidateTextColor", formatColor(candidateTextColor))
                        addProperty("candidatePanelBorderColor", formatColor(candidatePanelBorderColor))
                        addProperty("candidatePanelBorderEnabled", candidatePanelBorderEnabled)
                        addProperty("candidateRoundedEnabled", candidateRoundedEnabled)
                        addProperty("candidateCornerRadius", candidateCornerRadius)
                        addProperty("candidateSelectionHighlightEnabled", candidateSelectionHighlightEnabled)
                        addProperty("candidateLayout", candidateLayout.name)
                        addProperty("candidateSelectionStyle", candidateSelectionStyle.name)
                        addProperty("candidatePanelPreset", candidatePanelPreset.name)
                        addProperty("candidatePreset", candidatePreset.name)
                        addProperty("forceFullscreenCandidateInWindowed", forceFullscreenCandidateInWindowed)
                    }
                )
            )
            flush()
            close()
        }
    }

    fun createConfigScreen(): ConfigBuilder {
        return ConfigBuilder.create()
            .setTitle(Component.literal(I18n.get("config.kitsuneime.title")))
            .setSavingRunnable { saveConfig() }.apply {
                getOrCreateCategory(Component.literal(I18n.get("config.kitsuneime.category.chat"))).apply {
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.disableIMEInCommandMode")),
                                disableIMEInCommandMode
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.disableIMEInCommandMode")))
                            .setSaveConsumer { result -> disableIMEInCommandMode = result }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.autoReplaceSlashChar")),
                                autoReplaceSlashChar
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.autoReplaceSlashChar")))
                            .setSaveConsumer { result -> autoReplaceSlashChar = result }
                            .build()
                    )
                    addEntry(
                        entryBuilder().startStrList(
                            Component.literal(I18n.get("desc.kitsuneime.slashChars")),
                            slashCharArray.map { it.toString() }
                        )
                            .setDefaultValue(mutableListOf("、", "・"))
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.slashChars")))
                            .setCellErrorSupplier { str ->
                                if (str.length > 1)
                                    return@setCellErrorSupplier Optional.of(Component.literal(I18n.get("desc.kitsuneime.error.slashChars")))
                                return@setCellErrorSupplier Optional.empty()
                            }
                            .setSaveConsumer { result ->
                                slashCharArray = result
                                    .filterNot { it.isBlank() }
                                    .map { it[0] }
                                    .toSet()
                                    .toCharArray()
                            }
                            .build()
                    )
                }
                getOrCreateCategory(Component.literal(I18n.get("config.kitsuneime.category.overlay"))).apply {
                    addEntry(
                        entryBuilder()
                            .startEnumSelector(
                                Component.literal(I18n.get("desc.kitsuneime.candidatePanelPreset")),
                                CandidateListWidget.PanelPreset::class.java,
                                candidatePanelPreset
                            )
                            .setDefaultValue(CandidateListWidget.PanelPreset.CUSTOM)
                            .setEnumNameProvider { value ->
                                Component.literal(I18n.get("desc.kitsuneime.candidatePanelPreset.${value.name.lowercase(Locale.ROOT)}"))
                            }
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidatePanelPreset")))
                            .setSaveConsumer { result ->
                                candidatePanelPreset = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startColorField(
                                Component.literal(I18n.get("desc.kitsuneime.candidatePanelBackgroundColor")),
                                candidatePanelBackgroundColor
                            )
                            .setDefaultValue(0xFF_0E_18_19.toInt())
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidatePanelBackgroundColor")))
                            .setAlphaMode(true)
                            .setSaveConsumer { result ->
                                candidatePanelBackgroundColor = result
                                candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startColorField(
                                Component.literal(I18n.get("desc.kitsuneime.candidateTextColor")),
                                candidateTextColor
                            )
                            .setDefaultValue(0xFF_C5_C5_C5.toInt())
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateTextColor")))
                            .setAlphaMode(true)
                            .setSaveConsumer { result ->
                                candidateTextColor = result
                                candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.candidatePanelBorderEnabled")),
                                candidatePanelBorderEnabled
                            )
                            .setDefaultValue(false)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidatePanelBorderEnabled")))
                            .setSaveConsumer { result ->
                                candidatePanelBorderEnabled = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startColorField(
                                Component.literal(I18n.get("desc.kitsuneime.candidatePanelBorderColor")),
                                candidatePanelBorderColor
                            )
                            .setDefaultValue(0xFF_B8_B8_B8.toInt())
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidatePanelBorderColor")))
                            .setAlphaMode(true)
                            .setSaveConsumer { result ->
                                candidatePanelBorderColor = result
                                candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.candidateRoundedEnabled")),
                                candidateRoundedEnabled
                            )
                            .setDefaultValue(false)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateRoundedEnabled")))
                            .setSaveConsumer { result ->
                                candidateRoundedEnabled = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startIntSlider(
                                Component.literal(I18n.get("desc.kitsuneime.candidateCornerRadius")),
                                candidateCornerRadius,
                                0,
                                24
                            )
                            .setDefaultValue(3)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateCornerRadius")))
                            .setSaveConsumer { result ->
                                candidateCornerRadius = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startEnumSelector(
                                Component.literal(I18n.get("desc.kitsuneime.candidateLayout")),
                                CandidateListWidget.Layout::class.java,
                                candidateLayout
                            )
                            .setDefaultValue(CandidateListWidget.Layout.VERTICAL)
                            .setEnumNameProvider { value ->
                                Component.literal(I18n.get("desc.kitsuneime.candidateLayout.${value.name.lowercase(Locale.ROOT)}"))
                            }
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateLayout")))
                            .setSaveConsumer { result ->
                                candidateLayout = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startEnumSelector(
                                Component.literal(I18n.get("desc.kitsuneime.candidatePreset")),
                                CandidateListWidget.CandidatePreset::class.java,
                                candidatePreset
                            )
                            .setDefaultValue(CandidateListWidget.CandidatePreset.CUSTOM)
                            .setEnumNameProvider { value ->
                                Component.literal(I18n.get("desc.kitsuneime.candidatePreset.${value.name.lowercase(Locale.ROOT)}"))
                            }
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidatePreset")))
                            .setSaveConsumer { result ->
                                candidatePreset = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.candidateSelectionHighlightEnabled")),
                                candidateSelectionHighlightEnabled
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateSelectionHighlightEnabled")))
                            .setSaveConsumer { result ->
                                candidateSelectionHighlightEnabled = result
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startEnumSelector(
                                Component.literal(I18n.get("desc.kitsuneime.candidateSelectionStyle")),
                                CandidateListWidget.SelectionStyle::class.java,
                                candidateSelectionStyle
                            )
                            .setDefaultValue(CandidateListWidget.SelectionStyle.TEXT_COLOR)
                            .setEnumNameProvider { value ->
                                Component.literal(I18n.get("desc.kitsuneime.candidateSelectionStyle.${value.name.lowercase(Locale.ROOT)}"))
                            }
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateSelectionStyle")))
                            .setSaveConsumer { result ->
                                candidateSelectionStyle = result
                                if (result == CandidateListWidget.SelectionStyle.TEXT_COLOR) {
                                    candidatePanelPreset = CandidateListWidget.PanelPreset.CUSTOM
                                    candidatePreset = CandidateListWidget.CandidatePreset.CUSTOM
                                    candidatePanelBackgroundColor = 0xFF_0E_18_19.toInt()
                                    candidateTextColor = 0xFF_C5_C5_C5.toInt()
                                    candidateSelectedTextColor = 0xFF_0F_F7_96.toInt()
                                }
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startColorField(
                                Component.literal(I18n.get("desc.kitsuneime.candidateHighlightColor")),
                                candidateHighlightColor
                            )
                            .setRequirement { candidateSelectionHighlightEnabled }
                            .setDefaultValue(0xAA_87_99_DD.toInt())
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateHighlightColor")))
                            .setAlphaMode(true)
                            .setSaveConsumer { result ->
                                candidateHighlightColor = result
                                candidatePreset = CandidateListWidget.CandidatePreset.CUSTOM
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startColorField(
                                Component.literal(I18n.get("desc.kitsuneime.candidateSelectedTextColor")),
                                candidateSelectedTextColor
                            )
                            .setRequirement { !candidateSelectionHighlightEnabled }
                            .setDefaultValue(0xFF_0F_F7_96.toInt())
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.candidateSelectedTextColor")))
                            .setAlphaMode(true)
                            .setSaveConsumer { result ->
                                candidateSelectedTextColor = result
                                candidatePreset = CandidateListWidget.CandidatePreset.CUSTOM
                            }
                            .build()
                    )
                    addEntry(
                        entryBuilder()
                            .startBooleanToggle(
                                Component.literal(I18n.get("desc.kitsuneime.forceFullscreenCandidateInWindowed")),
                                forceFullscreenCandidateInWindowed
                            )
                            .setDefaultValue(true)
                            .setTooltip(Component.literal(I18n.get("tooltip.kitsuneime.forceFullscreenCandidateInWindowed")))
                            .setSaveConsumer { result ->
                                forceFullscreenCandidateInWindowed = result
                            }
                            .build()
                    )
                }
            }
    }

    fun shouldUseFullscreenCandidate(windowIsFullscreen: Boolean): Boolean {
        return windowIsFullscreen || forceFullscreenCandidateInWindowed
    }

    private fun parseColor(text: String?, defaultColor: Int = 0xAA_87_99_DD.toInt()): Int {
        val raw = text?.trim()?.removePrefix("#")?.removePrefix("0x")?.removePrefix("0X")
        if (raw.isNullOrEmpty()) return defaultColor
        val hex = if (raw.length == 6) "FF$raw" else raw
        return hex.toLongOrNull(16)?.toInt() ?: defaultColor
    }

    private fun formatColor(color: Int): String {
        return String.format("%08X", color)
    }

    private fun parseCandidateLayout(raw: String?): CandidateListWidget.Layout {
        return runCatching { CandidateListWidget.Layout.valueOf(raw?.uppercase(Locale.ROOT) ?: "") }
            .getOrDefault(CandidateListWidget.Layout.VERTICAL)
    }

    private fun parseSelectionStyle(raw: String?): CandidateListWidget.SelectionStyle {
        return runCatching { CandidateListWidget.SelectionStyle.valueOf(raw?.uppercase(Locale.ROOT) ?: "") }
            .getOrDefault(CandidateListWidget.SelectionStyle.TEXT_COLOR)
    }

    private fun parsePanelPreset(raw: String?): CandidateListWidget.PanelPreset {
        return runCatching { CandidateListWidget.PanelPreset.valueOf(raw?.uppercase(Locale.ROOT) ?: "") }
            .getOrDefault(CandidateListWidget.PanelPreset.CUSTOM)
    }

    private fun parseCandidatePreset(raw: String?): CandidateListWidget.CandidatePreset {
        return runCatching { CandidateListWidget.CandidatePreset.valueOf(raw?.uppercase(Locale.ROOT) ?: "") }
            .getOrDefault(CandidateListWidget.CandidatePreset.CUSTOM)
    }
}