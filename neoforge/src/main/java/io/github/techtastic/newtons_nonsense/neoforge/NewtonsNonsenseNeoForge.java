package io.github.techtastic.newtons_nonsense.neoforge;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.neoforge.registry.NeoForgeNNRegistries;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

@Mod(NewtonsNonsense.MOD_ID)
public final class NewtonsNonsenseNeoForge {
    public NewtonsNonsenseNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        NewtonsNonsense.init();

        modEventBus.addListener(NeoForgeNNRegistries::registerDatapackRegistries);

        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel sLevel)) return;

        Stage.onChunkLoad(event.getChunk(), sLevel, null);
    }

    private void onChunkUnload(ChunkEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel sLevel)) return;

        Stage.getOrCreateStage(sLevel).removeAndFreeChunk(event.getChunk().getPos());
    }
}
