package io.github.techtastic.newtons_nonsense;

import dev.architectury.event.events.common.*;
import io.github.techtastic.newtons_nonsense.commands.NNCommands;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Orchard;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";

    public static void init() {
        // Write common init code here.

        System.out.println("PhysX Version: " + PxTopLevelFunctions.getPHYSICS_VERSION());

        Orchard.init();

        LifecycleEvent.SERVER_STARTED.register(Orchard::onServerLoad);
        LifecycleEvent.SERVER_STOPPED.register(Orchard::onServerStop);
        LifecycleEvent.SERVER_LEVEL_LOAD.register(Orchard::onLevelLoad);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(Orchard::onLevelUnload);
        //TickEvent.SERVER_LEVEL_POST.register(Stage::onServerLevelPostTick);

        //BlockEvent.BREAK.register(Stage::onBlockBreak);
        //BlockEvent.PLACE.register(Stage::onBlockPlace);

        CommandRegistrationEvent.EVENT.register(NNCommands::register);
    }
}
