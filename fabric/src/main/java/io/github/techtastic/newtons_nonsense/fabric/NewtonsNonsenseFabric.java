package io.github.techtastic.newtons_nonsense.fabric;

import io.github.techtastic.newtons_nonsense.physics.pipeline.Orchard;
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
                Orchard.MaterialRegistry.MATERIAL_REGISTRY_KEY,
                Orchard.MaterialRegistry.MATERIAL_CODEC,
                Orchard.MaterialRegistry.MATERIAL_CODEC
        );

        ServerChunkEvents.CHUNK_LOAD.register(Orchard::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(Orchard::onChunkUnload);
    }
}
