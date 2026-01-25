/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battlefactory

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.platform.events.PlatformEvents

/**
 * Event handler for Battle Factory system.
 * 
 * Registers all critical safety hooks to ensure temporary party backups
 * are always restored correctly, even in crash scenarios.
 * 
 * Safety hooks:
 * - Player logout → auto-restore
 * - Player death → auto-restore  
 * - Player login → check for orphaned backups and restore
 * - Battle end → managed by session
 * 
 * @author Cobblemon Contributors
 * @since January 2026
 */
object BattleFactoryEventHandler {
    
    private var initialized = false
    
    /**
     * Registers all event listeners for Battle Factory safety.
     * This should be called during mod initialization.
     */
    fun register() {
        if (initialized) {
            Cobblemon.LOGGER.warn("BattleFactoryEventHandler already initialized")
            return
        }
        
        // Register dialogue components
        com.cobblemon.mod.common.api.dialogue.DialogueAction.types[BattleFactoryDialogueAction.TYPE] = BattleFactoryDialogueAction::class.java
        com.cobblemon.mod.common.api.dialogue.DialoguePredicate.types[HasBattleFactorySessionPredicate.NAME] = HasBattleFactorySessionPredicate::class.java
        
        // Register Tower dialogue action
        com.cobblemon.mod.common.api.dialogue.DialogueAction.types[TowerStartDialogueAction.TYPE] = TowerStartDialogueAction::class.java
        
        // Register NPCDefineBattleActor event listener for Tower battles
        // This overrides the default NPCBattleActor with TowerBattleActor to fix UI initialization
        CobblemonEvents.NPC_DEFINE_BATTLE_ACTOR.subscribe { event ->
            Cobblemon.LOGGER.info("[TOWER DEBUG] NPC_DEFINE_BATTLE_ACTOR event fired for player: ${event.player.name.string}")
            Cobblemon.LOGGER.info("[TOWER DEBUG] NPC Entity: ${event.npcEntity.name.string}, UUID: ${event.npcEntity.uuid}")
            
            val session = BattleFactoryTowerManager.getActiveSession(event.player)
            Cobblemon.LOGGER.info("[TOWER DEBUG] Session check result: ${if (session != null) "FOUND (Arena ${session.currentArena + 1}/7)" else "NULL - No active session"}")
            
            if (session != null) {
                // This is a Tower Trainer battle - override with our custom actor
                Cobblemon.LOGGER.info("[TOWER DEBUG] Attempting to get NPC party for challenge...")
                val npcParty = event.npcEntity.getPartyForChallenge(listOf(event.player))
                Cobblemon.LOGGER.info("[TOWER DEBUG] NPC Party result: ${if (npcParty != null) "FOUND (${npcParty.size()} Pokémon)" else "NULL - No party"}")
                
                if (npcParty != null) {
                    val battleTeam = npcParty.toBattleTeam(healPokemon = false)
                    Cobblemon.LOGGER.info("[TOWER DEBUG] Battle team created with ${battleTeam.size} Pokémon")
                    
                    event.battleActor = TowerBattleActor(
                        npc = event.npcEntity,
                        pokemonList = battleTeam,
                        skill = event.npcEntity.skill ?: event.npcEntity.npc.skill
                    )
                    Cobblemon.LOGGER.info("[TOWER DEBUG] Successfully overrode NPCBattleActor with TowerBattleActor for ${event.player.name.string}")
                } else {
                    Cobblemon.LOGGER.warn("[TOWER DEBUG] NPC party was NULL - cannot override actor")
                }
            } else {
                Cobblemon.LOGGER.info("[TOWER DEBUG] No active Tower session - skipping override (this is normal for non-Tower battles)")
            }
        }
        
        // Hook 1: Player logout → force restore
        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe { event ->
            TemporaryPartyManagerImpl.onPlayerLogout(event.player)
        }
        
        // Hook 2: Player death → force restore
        PlatformEvents.PLAYER_DEATH.subscribe { event ->
            TemporaryPartyManagerImpl.onPlayerDeath(event.player)
        }
        
        // Hook 3: Player login → check for orphaned backups
        PlatformEvents.SERVER_PLAYER_LOGIN.subscribe { event ->
            TemporaryPartyManagerImpl.onPlayerLogin(event.player)
        }
        
        // Hook 4: Battle victory → session.onWin() OR tower.onArenaVictory()
        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val winners = event.winners
            winners.forEach { battleActor ->
                if (battleActor is com.cobblemon.mod.common.battles.actor.PlayerBattleActor) {
                    val player = battleActor.entity as? net.minecraft.server.level.ServerPlayer ?: return@forEach
                    
                    // Check for Tower session first
                    if (BattleFactoryTowerManager.hasActiveSession(player)) {
                        BattleFactoryTowerManager.onArenaVictory(player)
                        return@forEach
                    }
                    
                    // Otherwise check for regular Battle Factory session
                    val session = FacilitySessionManager.getSession(player)
                    if (session is BattleFactorySession && session.isActive()) {
                        session.onWin()
                    }
                }
            }
        }
        
        // Hook 5: Battle fainted (all player Pokemon fainted) → session.onLose() OR tower.onArenaDefeat()
        CobblemonEvents.BATTLE_FAINTED.subscribe { event ->
            val pokemon = event.killed
            val actor = pokemon.actor
            
            if (actor is com.cobblemon.mod.common.battles.actor.PlayerBattleActor) {
                val player = actor.entity as? net.minecraft.server.level.ServerPlayer ?: return@subscribe
                
                // Check if ALL of the player's Pokemon have fainted
                if (actor.pokemonList.all { it.health == 0 }) {
                    // Check for Tower session first
                    if (BattleFactoryTowerManager.hasActiveSession(player)) {
                        BattleFactoryTowerManager.onArenaDefeat(player)
                        return@subscribe
                    }
                    
                    // Otherwise check for regular Battle Factory session
                    val session = FacilitySessionManager.getSession(player)
                    if (session is BattleFactorySession && session.isActive()) {
                        session.onLose()
                    }
                }
            }
        }

        // Hook 6: Server Tick for Watchdog
        // Checks active TowerBattleActors for animation hangs and recovers them
        PlatformEvents.SERVER_TICK_POST.subscribe {
            com.cobblemon.mod.common.Cobblemon.battleRegistry.getBattles().forEach { battle ->
                battle.actors.filterIsInstance<TowerBattleActor>().forEach { actor ->
                    actor.checkWatchdog()
                }
            }
        }
        
        initialized = true
        Cobblemon.LOGGER.info("Battle Factory event handlers registered successfully")
    }
}
