package io.github.techtastic.newtons_nonsense.fabric;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

public final class NewtonsNonsenseFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NewtonsNonsense.init();

        ServerChunkEvents.CHUNK_GENERATE.register(NewtonsNonsense::onChunkGenerate);
        ServerChunkEvents.CHUNK_LOAD.register(NewtonsNonsense::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(NewtonsNonsense::onChunkUnload);

        ReloadLevelRendererCallback.EVENT.register(level -> {

        });
    }
}
