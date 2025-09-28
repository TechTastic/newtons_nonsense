package io.github.techtastic.newtons_nonsense.fabric;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.PhysicsObjectTypes;
import io.github.techtastic.newtons_nonsense.registries.Material;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;

public final class NewtonsNonsenseFabric implements ModInitializer {
    public static Registry<PhysicsObjectType<?>> PHYSICS_OBJECT_TYPES_REGISTRY;

    @Override
    public void onInitialize() {
        NewtonsNonsense.init();

        DynamicRegistries.register(Material.REGISTRY_KEY, Material.CODEC);
        PHYSICS_OBJECT_TYPES_REGISTRY = FabricRegistryBuilder.createSimple(PhysicsObjectType.REGISTRY_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

        ServerChunkEvents.CHUNK_GENERATE.register(NewtonsNonsense::onChunkGenerate);
        ServerChunkEvents.CHUNK_LOAD.register(NewtonsNonsense::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(NewtonsNonsense::onChunkUnload);

        ReloadLevelRendererCallback.EVENT.register(NewtonsNonsense::onVisualReload);

        PhysicsObjectTypes.register();
    }
}
