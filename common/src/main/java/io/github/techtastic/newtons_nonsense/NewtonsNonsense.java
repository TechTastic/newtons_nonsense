package io.github.techtastic.newtons_nonsense;


import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import io.github.techtastic.newtons_nonsense.physics.Orchard;
import io.github.techtastic.newtons_nonsense.physics.AppleTree;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(Orchard::new);
        LifecycleEvent.SERVER_STOPPED.register(server -> Orchard.getInstance().cleanup());

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> Orchard.getInstance().getOrCreatePhysicsLevel(level).pause(false));
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> Orchard.getInstance().getOrCreatePhysicsLevel(level).pause(true));
        TickEvent.SERVER_LEVEL_POST.register(level -> Orchard.getInstance().getOrCreatePhysicsLevel(level).tryAndTick());
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
