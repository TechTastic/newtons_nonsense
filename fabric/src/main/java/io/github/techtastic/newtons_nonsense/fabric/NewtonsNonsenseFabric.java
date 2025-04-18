package io.github.techtastic.newtons_nonsense.fabric;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.fabricmc.api.ModInitializer;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class NewtonsNonsenseFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        NewtonsNonsense.init();

        DynamicRegistries.registerSynced(
                PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                PhysicsMaterialRegistry.MATERIAL_CODEC,
                PhysicsMaterialRegistry.MATERIAL_CODEC
        );

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> Stage.onChunkLoad(chunk, level));
        //ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> Stage.getOrCreateStage(level).removeAndFreeChunk(chunk.getPos()));
    }
}
