/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.factory

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.reactive.Observable.Companion.emitWhile
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.api.storage.adapter.SerializedStore
import com.cobblemon.mod.common.api.storage.adapter.flatfile.FileStoreAdapter
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.subscribeOnServer
import java.util.UUID
import java.util.concurrent.Executors
import net.minecraft.core.RegistryAccess
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A [PokemonStoreFactory] that is backed by a file. This implementation will now handle persistence and scheduling
 * for saving, as well as simple map cache.
 *
 * @author Hiroku
 * @since November 29th, 2021
 */
open class FileBackedPokemonStoreFactory<S>(
    protected val adapter: FileStoreAdapter<S>,
    protected val createIfMissing: Boolean,
    val partyConstructor: (UUID) -> PlayerPartyStore = { PlayerPartyStore(it) },
    val pcConstructor: (UUID) -> PCStore = { PCStore(it) }
) : PokemonStoreFactory {

    var passedTicks = 0
    protected val saveSubscription = PlatformEvents.SERVER_TICK_PRE.subscribe {
        passedTicks++
        if (passedTicks > 20 * Cobblemon.config.pokemonSaveIntervalSeconds) {
            saveAll(it.server.registryAccess())
            passedTicks = 0
        }

        if (passedTicks % 10 == 0) {
            storeCaches.forEach { (store, cache) ->
                for (key in cache.cacheMap.keys()) {
                    if (key.getPlayer() == null) {
                        cache.cacheMap.remove(key)
                        LOGGER.info("Removed logged out player '$key' from ${store.name} cache.")
                    }
                }
            }
        }
    }

    protected var saveExecutor = Executors.newSingleThreadExecutor()
    protected val storeCaches = ConcurrentHashMap<Class<out PokemonStore<*>>, StoreCache<*, *>>()
    protected inner class StoreCache<E : StorePosition, T : PokemonStore<E>> {
        val cacheMap = ConcurrentHashMap<UUID, T>()
    }

    protected fun <E : StorePosition, T : PokemonStore<E>> getStoreCache(storeClass: Class<T>): StoreCache<E, T> {
        val cache = storeCaches.getOrPut(storeClass) { StoreCache<E, T>() }
        return cache as StoreCache<E, T>
    }

    private val dirtyStores = CopyOnWriteArrayList<PokemonStore<out StorePosition>>()

    override fun getPlayerParty(playerID: UUID, registryAccess: RegistryAccess) = getStore(PlayerPartyStore::class.java, playerID, registryAccess, partyConstructor)
    override fun getPC(playerID: UUID, registryAccess: RegistryAccess) = getStore(PCStore::class.java, playerID, registryAccess, pcConstructor)

    override fun <E : StorePosition, T : PokemonStore<E>> getCustomStore(storeClass: Class<T>, uuid: UUID, registryAccess: RegistryAccess) = getStore(storeClass, uuid, registryAccess)

    fun <E : StorePosition, T : PokemonStore<E>> getStore(
        storeClass: Class<T>,
        uuid: UUID,
        registryAccess: RegistryAccess,
        constructor: ((UUID) -> T) = { storeClass.getConstructor(UUID::class.java).newInstance(it) }
    ): T? {
        val cache = getStoreCache(storeClass).cacheMap
        val cached = cache[uuid]
        if (cached != null) {
            return cached
        } else {
            val loaded = adapter.load(storeClass, uuid, registryAccess)
                ?: run {
                    if (createIfMissing) {
                        return@run constructor(uuid)
                    } else {
                        return@run null
                    }
                }
                ?: return null

            loaded.initialize()
            track(loaded)
            cache[uuid] = loaded
            LOGGER.info("Loaded $uuid from and now put in ${storeClass.name} cache.")
            return loaded
        }
    }

    fun save(store: PokemonStore<*>, registryAccess: RegistryAccess) {
        val serialized = SerializedStore(store::class.java, store.uuid, adapter.serialize(store, registryAccess))
        dirtyStores.remove(store)
        saveExecutor.submit {
            try {
                adapter.save(serialized.storeClass, serialized.uuid, serialized.serializedForm)
            } catch (e: Exception) {
                LOGGER.error("Save: Failed to save ${serialized.uuid} of ${serialized.storeClass.name}.", e)
            }
        }
    }

    fun saveAll(registryAccess: RegistryAccess) {
        LOGGER.info("Serializing ${dirtyStores.size} Pokémon stores.")
        val serializedStores = dirtyStores.map { SerializedStore(it::class.java, it.uuid, adapter.serialize(it as PokemonStore<*>, registryAccess)) }
        dirtyStores.clear()
        LOGGER.info("Queueing save.")
        saveExecutor.submit {
            serializedStores.forEach {
                try {
                    adapter.save(it.storeClass, it.uuid, it.serializedForm)
                } catch (e: Exception) {
                    LOGGER.error("Save All: Failed to save ${it.uuid} of ${it.storeClass.name}.", e)
                }
            }
            LOGGER.info("Saved ${serializedStores.size} Pokémon stores.")
        }
    }

    fun isCached(store: PokemonStore<*>) = storeCaches[store::class.java]?.cacheMap?.containsKey(store.uuid) == true

    fun track(store: PokemonStore<*>) {
        store.getAnyChangeObservable()
            .pipe(emitWhile { isCached(store) })
            .subscribeOnServer { dirtyStores.add(store) }
    }

    override fun shutdown(registryAccess: RegistryAccess) {
        saveSubscription.unsubscribe()
        saveAll(registryAccess)
        saveExecutor.shutdown()
    }

    override fun onPlayerDisconnect(player: ServerPlayer) {
        dirtyStores.filter { it.uuid == player.uuid }.forEach { save(it, player.registryAccess()) }
        storeCaches.forEach { (_, cache) -> cache.cacheMap.remove(player.uuid) }
    }
}