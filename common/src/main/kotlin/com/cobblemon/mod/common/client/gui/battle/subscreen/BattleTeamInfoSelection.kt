package com.cobblemon.mod.common.client.gui.battle.subscreen

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.ClientBattleActor
import com.cobblemon.mod.common.client.battle.ClientBattleInformationRepository
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.battle.BattleGUI
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.client.render.getDepletableRedGreen
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.renderScaledGuiItemIcon
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.questionMarkIcon
import com.cobblemon.mod.common.net.messages.client.battle.BattlePokemonDTO
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.collections.get
import kotlin.math.ceil
import kotlin.math.floor

class BattleTeamInfoSelection(
    battleGUI: BattleGUI
) : BattleActionSelection(
    battleGUI,
    x = 0,
    y = if (Minecraft.getInstance().window.guiScaledHeight > 304) (Minecraft.getInstance().window.guiScaledHeight / 2) - (BACKGROUND_HEIGHT / 2)
    else Minecraft.getInstance().window.guiScaledHeight - (BACKGROUND_HEIGHT + 78),
    width = Minecraft.getInstance().window.guiScaledWidth,
    height = Minecraft.getInstance().window.guiScaledHeight,
    "cobblemon.battle.team_info".asTranslated()
) {
    companion object {
        const val SLOT_HORIZONTAL_SPACING = 4F
        const val SLOT_VERTICAL_SPACING = 2F

        const val BACKGROUND_HEIGHT = 148
        val underlayTexture = cobblemonResource("textures/gui/battle/selection_underlay.png")
        var visible = false
    }

    val tiles = mutableListOf<PokemonTile>()
    val backButton = BattleBackButton(x + 9F, Minecraft.getInstance().window.guiScaledHeight - 22F)

    init {
        addTiles()
    }

    fun addTiles() {
        val battle = CobblemonClient.battle ?: return

        val actors = mutableListOf<ClientBattleActor>()
        battle.sides.forEach { side ->
            side.actors.forEach { actors.add(it) }
        }

        if (actors.size != 2) return
        val displayOrder = if (actors.last().uuid == Minecraft.getInstance().player?.uuid) actors.reversed() else actors
        val actorToPokemon = displayOrder.map { it to ClientBattleInformationRepository.actors[it.uuid] }
        val activeSide1Pokemon = battle.side1.activeClientBattlePokemon.mapNotNull { it.battlePokemon?.uuid }
        val activeSide2Pokemon = battle.side2.activeClientBattlePokemon.mapNotNull { it.battlePokemon?.uuid }
        actorToPokemon.first().second?.forEachIndexed { index, pokemon ->
            val (slotX, slotY) = getSlotPosition(index, 1)
            val isCurrentlyInBattle = pokemon.uuid in activeSide1Pokemon || pokemon.uuid in activeSide2Pokemon
            tiles.add(PokemonTile(slotX, slotY, actorToPokemon.first().first, pokemon, isCurrentlyInBattle, this))
        }
        actorToPokemon.last().second?.forEachIndexed { index, pokemon ->
            val (slotX, slotY) = getSlotPosition(index, 2)
            val isCurrentlyInBattle = pokemon.uuid in activeSide1Pokemon || pokemon.uuid in activeSide2Pokemon
            tiles.add(PokemonTile(slotX, slotY, actorToPokemon.last().first, pokemon, isCurrentlyInBattle, this))
        }
    }

    fun getSlotPosition(index: Int, side: Int): Pair<Float, Float> {
        val startX = if (side == 1) 10 else width - 10 - (PokemonTile.SELECT_WIDTH + SLOT_HORIZONTAL_SPACING) * 2
        val startY = y + 34
        val row = index / 2
        val column = index % 2
        val slotX = startX.toFloat() + column * (SLOT_HORIZONTAL_SPACING + PokemonTile.SELECT_WIDTH)
        val slotY = startY.toFloat() + row * (SLOT_VERTICAL_SPACING + PokemonTile.SELECT_HEIGHT)
        return Pair(slotX, slotY)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (opacity <= 0.05F) return

        val matrixStack = context.pose()
        blitk(
            matrixStack = matrixStack,
            texture = underlayTexture,
            x = x,
            y = y,
            width = width,
            height = BACKGROUND_HEIGHT
        )

        // Draw Title Text
        val text = "cobblemon.battle.team_info".asTranslated()
        val textWidth = Minecraft.getInstance().font.width(text)
        drawScaledText(
            context = context,
            text = text,
            x = (width - textWidth) / 2,
            y = y + 17,
            shadow = true
        )

        for (index in 0 until 6) {
            val (slotX, slotY) = getSlotPosition(index, 1)
            blitk(
                matrixStack = matrixStack,
                texture = PokemonTile.partySelectDisabledResourse,
                x = slotX,
                y = slotY,
                width = PokemonTile.SELECT_WIDTH,
                height = PokemonTile.SELECT_HEIGHT - 7,
                vOffset = PokemonTile.SELECT_HEIGHT,
                textureHeight = PokemonTile.SELECT_HEIGHT * 2,
            )
        }

        for (index in 0 until 6) {
            val (slotX, slotY) = getSlotPosition(index, 2)
            blitk(
                matrixStack = matrixStack,
                texture = PokemonTile.partySelectDisabledResourse,
                x = slotX,
                y = slotY,
                width = PokemonTile.SELECT_WIDTH,
                height = PokemonTile.SELECT_HEIGHT - 7,
                vOffset = PokemonTile.SELECT_HEIGHT,
                textureHeight = PokemonTile.SELECT_HEIGHT * 2,
            )
        }

        tiles.forEach { it.render(context, mouseX.toDouble(), mouseY.toDouble(), delta) }
        backButton.render(context, mouseX, mouseY, delta)
    }

    override fun mousePrimaryClicked(mouseX: Double, mouseY: Double): Boolean {
        if (backButton.isHovered(mouseX, mouseY)) {
            battleGUI.changeActionSelection(null)
            playDownSound(Minecraft.getInstance().soundManager)
            Companion.visible = false
            return true
        }
        return false
    }

    override fun defaultButtonNarrationText(builder: NarrationElementOutput) {
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun narrationPriority() = NarratableEntry.NarrationPriority.HOVERED

    class PokemonTile(
        val x: Float,
        val y: Float,
        val actor: ClientBattleActor,
        val pokemonDto: BattlePokemonDTO,
        val isCurrentlyInBattle: Boolean,
        val teamInfoSelection: BattleTeamInfoSelection
    ) {
        companion object {
            const val SELECT_WIDTH = 94
            const val SELECT_HEIGHT = 29
            const val SCALE = 0.5F
            val partySelectResource = cobblemonResource("textures/gui/battle/party_select.png")
            val partySelectDisabledResourse = cobblemonResource("textures/gui/battle/party_select_disabled.png")
            val expandedPokemonInfo = cobblemonResource("textures/gui/battle/expanded_pokemon_info_center.png")

            private val decimalFormat = DecimalFormat("0.00").also {
                it.roundingMode = RoundingMode.CEILING
            }
        }

        val state = FloatingState()

        fun isHovered(mouseX: Double, mouseY: Double) = mouseX in x..(x + SELECT_WIDTH) && mouseY in (y..(y + SELECT_HEIGHT))

        fun render(context: GuiGraphics, mouseX: Double, mouseY: Double, deltaTicks: Float) {
            val pokemon = pokemonDto.activeBattlePokemonDTO ?: return
            state.currentAspects = pokemon.aspects
            val matrixStack = context.pose()
            val isFainted = pokemonDto.fainted

            val clientPokemon = ClientBattlePokemon(
                uuid = pokemon.uuid,
                properties = pokemon.properties,
                aspects = pokemon.aspects,
                displayName = pokemon.displayName,
                hpValue = pokemon.hpValue,
                maxHp = pokemon.maxHp,
                isHpFlat = pokemon.isFlatHp,
                status = pokemon.status,
                statChanges = pokemon.statChanges,
            )
            clientPokemon.actor = actor

            val status = pokemon.status?.showdownName
            if (!isFainted && status != null) {
                blitk(
                    matrixStack = matrixStack,
                    texture = cobblemonResource("textures/gui/interact/party_select_status_$status.png"),
                    x = x + 27,
                    y = y + 24,
                    height = 5,
                    width = 37
                )

                drawScaledText(
                    context = context,
                    text = lang("ui.status.$status").bold(),
                    x = x + 32.5,
                    y = y + 24.5,
                    shadow = true,
                    scale = SCALE
                )
            }

            blitk(
                matrixStack = matrixStack,
                texture = if (!isFainted) partySelectResource else partySelectDisabledResourse,
                x = x,
                y = y,
                width = SELECT_WIDTH,
                height = SELECT_HEIGHT,
                vOffset = if (!isFainted) SELECT_HEIGHT else 0,
                textureHeight = SELECT_HEIGHT * 2,
            )

            val ballIcon = cobblemonResource("textures/gui/ball/poke_ball.png")
            val ballHeight = 22
            blitk(
                matrixStack = matrixStack,
                texture = ballIcon,
                x = (x + 85) / SCALE,
                y = (y - 3) / SCALE,
                height = ballHeight,
                width = 18,
                vOffset = if (isCurrentlyInBattle) ballHeight else 0,
                textureHeight = ballHeight * 2,
                scale = SCALE
            )

            // Render Pokémon
            matrixStack.pushPose()
            matrixStack.translate(x + SELECT_WIDTH - (25 / 2.0) - 4, y - 1.0, 0.0)
            matrixStack.scale(2.5F, 2.5F, 1F)
            drawProfilePokemon(
                species = clientPokemon.species.resourceIdentifier,
                matrixStack = matrixStack,
                rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
                state = state,
                scale = 4.5F,
                partialTicks = deltaTicks
            )
            matrixStack.popPose()

            // Ensure elements are not hidden behind Pokémon render
            matrixStack.pushPose()
            matrixStack.translate(0.0, 0.0, 100.0)
            // Held Item
            val heldItem = pokemonDto.heldItem ?: ItemStack.EMPTY
            if (!heldItem.isEmpty) {
                renderScaledGuiItemIcon(
                    matrixStack = matrixStack,
                    itemStack = heldItem,
                    x = x + 81.0,
                    y = y + 11.0,
                    scale = 0.5
                )
            }

            val textOpacity = if (isFainted) 0.7F else 1F

            // Target Level
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.lv").bold(),
                x = x + 5,
                y = y + 4,
                opacity = textOpacity,
                shadow = true
            )
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = clientPokemon.level.toString().text().bold(),
                x = x + 5 + 13,
                y = y + 4,
                opacity = textOpacity,
                shadow = true
            )

            val displayText = clientPokemon.displayName.bold()
            // Pokémon Display Name
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = displayText,
                x = x + 5,
                y = y + 11,
                opacity = textOpacity,
                shadow = true
            )

            // Gender
            val gender = clientPokemon.gender
            val pokemonDisplayNameWidth = Minecraft.getInstance().font.width(displayText.font(CobblemonResources.DEFAULT_LARGE))
            if (gender != Gender.GENDERLESS) {
                val isMale = gender == Gender.MALE
                val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = textSymbol,
                    x = x + 6 + pokemonDisplayNameWidth,
                    y = y + 11,
                    colour = if (isMale) 0x32CBFF else 0xFC5454,
                    opacity = textOpacity,
                    shadow = true
                )
            }

            // HP
            val barWidthMax = 90
            val hpRatio = if (clientPokemon.isHpFlat) clientPokemon.hpValue / clientPokemon.maxHp else clientPokemon.hpValue
            val barWidth = hpRatio * barWidthMax
            val (red, green) = getDepletableRedGreen(hpRatio)

            blitk(
                matrixStack = matrixStack,
                texture = CobblemonResources.WHITE,
                x = x + 1,
                y = y + 22,
                width = barWidth,
                height = 1,
                textureWidth = barWidth / hpRatio,
                uOffset = barWidthMax - barWidth,
                red = red * 0.8F,
                green = green * 0.8F,
                blue = 0.27F
            )

            val text = if (clientPokemon.isHpFlat) {
                "${clientPokemon.hpValue.toInt()}/${clientPokemon.maxHp.toInt()}"
            } else {
                "${ceil(clientPokemon.hpValue * 100)}%"
            }.text()

            drawScaledText(
                context = context,
                text = text,
                x = x + 14,
                y = y + 24.5,
                scale = SCALE,
                centered = true
            )
            matrixStack.popPose()

            if (isHovered(mouseX, mouseY)) {
                renderExpandedInfo(context, clientPokemon)
            }
        }

        private fun renderExpandedInfo(context: GuiGraphics, pokemon: ClientBattlePokemon) {
            context.pose().pushPose()
            context.pose().translate(0.0, 0.0, 300.0)
            val startX = Minecraft.getInstance().window.guiScaledWidth / 2 - (137 / 2)
            val startY = teamInfoSelection.y + 30
            val species = pokemon.species
            val form = species.getForm(pokemon.state.currentAspects)

            val actor = pokemon.actor.uuid
            val actorTeam = ClientBattleInformationRepository.actors[actor]
            val dto = actorTeam?.let { it.firstOrNull { dto -> pokemon.uuid == dto.uuid } }

            val expandedInfoTexture = expandedPokemonInfo

            blitk(
                matrixStack = context.pose(),
                texture = expandedInfoTexture,
                x = startX,
                y = startY,
                height = 112,
                width = 137,
                textureHeight = 112,
                textureWidth = 137,
            )

            drawScaledText(
                context = context,
                text = "cobblemon.battle.label.pokemon_form".asTranslated().bold(),
                x = startX + 36,
                y = startY + 6.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            drawScaledText(
                context = context,
                text = getFormText(species, form),
                x = startX + 36,
                y = startY + 13.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            drawScaledText(
                context = context,
                text = "cobblemon.battle.label.buffs".asTranslated().bold(),
                x = startX + 36,
                y = startY + 23.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            drawScaledText(
                context = context,
                text = "cobblemon.stat.attack.name".asTranslated(),
                x = startX + 10,
                y = startY + 31.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.ATTACK)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 31.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.stat.defence.name".asTranslated(),
                x = startX + 10,
                y = startY + 39.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.DEFENCE)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 39.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.battle.stats.special_attack".asTranslated(),
                x = startX + 10,
                y = startY + 47.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.SPECIAL_ATTACK)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 47.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.battle.stats.special_defence".asTranslated(),
                x = startX + 10,
                y = startY + 55.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.SPECIAL_DEFENCE)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 55.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.stat.speed.name".asTranslated(),
                x = startX + 10,
                y = startY + 63.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.SPEED)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 63.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.stat.accuracy.name".asTranslated(),
                x = startX + 10,
                y = startY + 71.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.ACCURACY)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 71.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.stat.evasion.name".asTranslated(),
                x = startX + 10,
                y = startY + 79.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = false
            )

            dto?.buffs?.get(Stats.EVASION)?.let {
                drawScaledText(
                    context = context,
                    text = getMultiplierText(it),
                    x = startX + 50,
                    y = startY + 79.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = false
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.battle.label.ability".asTranslated().bold(),
                x = startX + 100,
                y = startY + 6.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            val ability = dto?.ability?.let { Abilities.get(it) }
            val abilityName = ability?.displayName?.text() ?: "?".text()

            drawScaledText(
                context = context,
                text = abilityName,
                x = startX + 100,
                y = startY + 13.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            drawScaledText(
                context = context,
                text = "cobblemon.battle.label.moves".asTranslated().bold(),
                x = startX + 100,
                y = startY + 23.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            if (dto?.moves?.size != 4) {
                for (i in 0 until 4) {
                    drawScaledText(
                        context = context,
                        text = "?".text(),
                        x = startX + 100,
                        y = startY + 31.5 + i * 8,
                        scale = BattleOverlay.Companion.SCALE,
                        shadow = true,
                        centered = true
                    )
                }
            }
            else {
                val questionMarkText = "?".text()
                dto.moves.forEachIndexed { index, moveDTO ->
                    val move = moveDTO?.move ?: questionMarkText
                    drawScaledText(
                        context = context,
                        text = move.string.text(),
                        x = if (move == questionMarkText) startX + 100 else startX + 74,
                        y = startY + 31.5 + index * 8,
                        scale = BattleOverlay.Companion.SCALE,
                        shadow = true,
                        centered = move == questionMarkText
                    )

                    if (moveDTO != null) {
                        drawScaledTextJustifiedRight(
                            context = context,
                            text = moveDTO.timesUsed.toString().text(),
                            x = startX + 127.5,
                            y = startY + 31.5 + index * 8,
                            scale = BattleOverlay.Companion.SCALE,
                            shadow = true,
                        )
                    }
                }
            }

            if (dto?.speed != null) {
                val speed = dto.speed!!
                val speedBoost = dto.buffs.get(Stats.SPEED) ?: 1.0
                val speedAfterBoost = (speed * speedBoost).toInt()
                drawScaledText(
                    context = context,
                    text = "cobblemon.stat.speed.name".asTranslated(),
                    x = startX + 100,
                    y = startY + 65.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = true
                )

                drawScaledText(
                    context = context,
                    text = speedAfterBoost.toString().text(),
                    x = startX + 100,
                    y = startY + 72.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = true
                )
            }
            else {
                drawScaledText(
                    context = context,
                    text = "cobblemon.battle.label.speed_tier".asTranslated(),
                    x = startX + 100,
                    y = startY + 65.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = true
                )

                val speedBoost = dto?.buffs?.get(Stats.SPEED) ?: 1.0
                val speedTier = getSpeedRange(form, pokemon.level, speedBoost)

                drawScaledText(
                    context = context,
                    text = speedTier,
                    x = startX + 100,
                    y = startY + 72.5,
                    scale = BattleOverlay.Companion.SCALE,
                    shadow = true,
                    centered = true
                )
            }

            drawScaledText(
                context = context,
                text = "cobblemon.battle.label.held_item".asTranslated(),
                x = startX + 40,
                y = startY + 99.5,
                scale = BattleOverlay.Companion.SCALE,
                shadow = true,
                centered = true
            )

            TypeIcon(
                x = startX + 100.5,
                y = startY + 88,
                type = form.primaryType,
                secondaryType = form.secondaryType,
                small = false,
                centeredX = true
            ).render(context)

            if (form.secondaryType != null) {
                blitk(
                    matrixStack = context.pose(),
                    texture = cobblemonResource("textures/gui/battle/type_spacer_double.png"),
                    x = (startX + 69.0) / 0.45,
                    y = (startY + 91.5) / 0.45,
                    height = 24,
                    width = 140,
                    textureHeight = 24,
                    textureWidth = 140,
                    scale = 0.45f
                )
            }
            else {
                blitk(
                    matrixStack = context.pose(),
                    texture = cobblemonResource("textures/gui/summary/type_spacer.png"),
                    x = (startX + 70.5) / 0.45,
                    y = (startY + 91.5) / 0.45,
                    height = 24,
                    width = 132,
                    textureHeight = 24,
                    textureWidth = 132,
                    scale = 0.45f
                )
            }

            val heldItem = dto?.heldItem
            if (heldItem != null && !heldItem.isEmpty()) {
                context.pose().pushPose()
                context.pose().scale(0.95F, 0.95F, 0.95F)
                val itemX = (startX + 4) / 0.95
                val itemY = (startY + 91) / 0.95
                context.renderItem(heldItem, itemX.toInt(), itemY.toInt())
                context.pose().popPose()
            }
            else {
                blitk(
                    matrixStack = context.pose(),
                    texture = questionMarkIcon,
                    x = startX + 6,
                    y = startY + 92.5,
                    height = 11,
                    width = 10,
                )
            }
            context.pose().popPose()
        }

        private fun getSpeedRange(form: FormData, level: Int, speedBoost: Double): MutableComponent {
            val base = form.baseStats[Stats.SPEED] ?: 0
            val maxIV = 31
            val maxEV = 252
            val minNatureMod = 0.9
            val maxNatureMod = 1.1

            val minSpeed = getStat(base, level, 0, 0, minNatureMod) * speedBoost
            val maxSpeed = getStat(base, level, maxIV, maxEV, maxNatureMod) * speedBoost
            return "cobblemon.battle.speed_tier".asTranslated(minSpeed.toInt(), maxSpeed.toInt())
        }

        private fun getStat(base: Int, level: Int, iv: Int, ev: Int, natureMod: Double): Int {
            return floor((floor(((2.0 * base + iv + floor(ev / 4.0)) * level) / 100) + 5) * natureMod).toInt()
        }

        private fun getMultiplierText(multiplier: Double): MutableComponent {
            val rounded = decimalFormat.format(multiplier)
            val str = "x$rounded"
            return when {
                multiplier > 1.0 -> str.green()
                multiplier < 1.0 -> str.red()
                else -> str.text()
            }
        }

        private fun getFormText(species: Species, form: FormData): MutableComponent {
            val speciesName = species.name
            val formName = form.name
            return if (formName == "Normal") speciesName.text() else "$speciesName-$formName".text()
        }
    }
}