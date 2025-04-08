package io.github.techtastic.newtons_nonsense.neoforge.registry;

import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class NeoForgeNNRegistries {
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                PhysicsMaterialRegistry.MATERIAL_CODEC,
                PhysicsMaterialRegistry.MATERIAL_CODEC,
                builder -> builder.maxId(256).defaultKey(PhysicsMaterialRegistry.DEFAULT_MATERIAL)
        );
    }
}
