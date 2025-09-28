package io.github.techtastic.newtons_nonsense.neoforge;

import dev.architectury.platform.Platform;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.PhysicsObjectTypes;
import io.github.techtastic.newtons_nonsense.registries.Material;
import io.github.techtastic.newtons_nonsense.registries.PhysicsObjectType;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@Mod(NewtonsNonsense.MOD_ID)
public final class NewtonsNonsenseNeoForge {
    public static Registry<PhysicsObjectType<?>> PHYSICS_OBJECT_TYPES_REGISTRY;

    public NewtonsNonsenseNeoForge(IEventBus bus) {
        NewtonsNonsense.init();

        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(this::onFlywheelReload);

        bus.addListener(this::commonSetup);
        bus.addListener(this::registerRegistries);
        bus.addListener(this::registerDatapackRegistries);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        PhysicsObjectTypes.register();
    }

    private void registerRegistries(NewRegistryEvent event) {
        PHYSICS_OBJECT_TYPES_REGISTRY = event.create(new RegistryBuilder<>(PhysicsObjectType.REGISTRY_KEY).sync(true));
    }

    private void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        if (Platform.getEnv() == Dist.DEDICATED_SERVER)
            event.dataPackRegistry(Material.REGISTRY_KEY, Material.CODEC, null);
    }

    private void onFlywheelReload(ReloadLevelRendererEvent event) {
        NewtonsNonsense.onVisualReload(event.level());
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            if (event.isNewChunk())
                NewtonsNonsense.onChunkGenerate(level, chunk);
            else
                NewtonsNonsense.onChunkLoad(level, chunk);
        }
    }

    private void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk)
            NewtonsNonsense.onChunkUnload(level, chunk);
    }
}
