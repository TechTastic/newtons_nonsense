package io.github.techtastic.newtons_nonsense.neoforge;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

@Mod(NewtonsNonsense.MOD_ID)
public final class NewtonsNonsenseNeoForge {
    public NewtonsNonsenseNeoForge() {
        NewtonsNonsense.init();

        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
    }

    private void onFlywheelReload(ReloadLevelRendererEvent event) {
        // Get ServerPhysicsWorld
        // Get objects
        // Add all to VisualizationHelper
        //   VisualizationHelper.queueAdd();
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
