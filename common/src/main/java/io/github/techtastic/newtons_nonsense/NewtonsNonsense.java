package io.github.techtastic.newtons_nonsense;

import dev.architectury.event.events.common.*;
import io.github.techtastic.newtons_nonsense.commands.NNCommands;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";

    public static void init() {
        // Write common init code here.

        System.out.println("PhysX Version: " + PxTopLevelFunctions.getPHYSICS_VERSION());

        Backstage.init();

        LifecycleEvent.SERVER_STOPPED.register(Backstage::onServerStop);

        LifecycleEvent.SERVER_LEVEL_LOAD.register(Stage::onServerLevelLoad);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(Stage::onServerLevelUnload);
        TickEvent.SERVER_LEVEL_POST.register(Stage::onServerLevelPostTick);

        //ChunkEvent.LOAD_DATA.register(Stage::onChunkLoad);
        BlockEvent.BREAK.register(Stage::onBlockBreak);
        BlockEvent.PLACE.register(Stage::onBlockPlace);

        CommandRegistrationEvent.EVENT.register(NNCommands::register);
    }
}
