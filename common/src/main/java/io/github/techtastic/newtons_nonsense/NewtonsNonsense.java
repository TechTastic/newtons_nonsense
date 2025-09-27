package io.github.techtastic.newtons_nonsense;


import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        int version = PxTopLevelFunctions.getPHYSICS_VERSION();
        LOGGER.info("PhysX Version: {}.{}.{}",
                version >> 24,
                (version >> 16) & 0xff,
                (version >> 8) & 0xff
        );

        LifecycleEvent.SERVER_STARTED.register(Backend::getOrCreateInstance);
        LifecycleEvent.SERVER_STOPPED.register(server -> Backend.getOrCreateInstance(server).cleanup());

        //LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> Backend.getInstance().getOrCreatePhysicsLevel(level).pause(false));
        //LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> Backend.getInstance().getOrCreatePhysicsLevel(level).pause(true));
        //TickEvent.SERVER_LEVEL_POST.register(level -> Backend.getInstance().getOrCreatePhysicsLevel(level).tryAndTick());
    }

    public static void onChunkGenerate(ServerLevel level, LevelChunk chunk) {
        // Create/Load Ground and LOD ground
    }

    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        // Swap LOD Ground to Ground
        // Load all Physics Objects
    }

    public static void onChunkUnload(ServerLevel level, LevelChunk chunk) {
        // Swap Ground to LOD Ground
    }
}
