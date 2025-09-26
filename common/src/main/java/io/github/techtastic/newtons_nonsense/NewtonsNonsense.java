package io.github.techtastic.newtons_nonsense;


import com.mojang.logging.LogUtils;
import io.github.techtastic.newtons_nonsense.util.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("PhysX Version: {}.{}.{}",
                Constants.PX_PHYSICS_VERSION >> 24,
                (Constants.PX_PHYSICS_VERSION >> 16) & 0xff,
                (Constants.PX_PHYSICS_VERSION >> 8) & 0xff
        );
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
