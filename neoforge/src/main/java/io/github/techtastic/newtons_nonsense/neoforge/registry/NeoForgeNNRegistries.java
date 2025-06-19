package io.github.techtastic.newtons_nonsense.neoforge.registry;

import io.github.techtastic.newtons_nonsense.physics.pipeline.Orchard;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class NeoForgeNNRegistries {
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                Orchard.MaterialRegistry.MATERIAL_REGISTRY_KEY,
                Orchard.MaterialRegistry.MATERIAL_CODEC,
                Orchard.MaterialRegistry.MATERIAL_CODEC,
                builder -> builder.maxId(256).defaultKey(Orchard.MaterialRegistry.DEFAULT_MATERIAL)
        );
    }
}
