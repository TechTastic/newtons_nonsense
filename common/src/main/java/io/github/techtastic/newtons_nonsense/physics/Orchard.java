package io.github.techtastic.newtons_nonsense.physics;

import com.mojang.logging.LogUtils;
import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.util.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.physics.PxPhysics;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.HashMap;
import java.util.Map;

public class Orchard {
    private static Orchard INSTANCE;
    private final HashMap<ResourceKey<Level>, AppleTree> TREES = new HashMap<>();

    private final PxFoundation foundation;
    private final PxPhysics physics;
    private final PxCpuDispatcher dispatcher;

    public Orchard(MinecraftServer server) {
        int version = PxTopLevelFunctions.getPHYSICS_VERSION();
        NewtonsNonsense.LOGGER.info("PhysX Version: {}.{}.{}",
                Constants.PX_PHYSICS_VERSION >> 24,
                (Constants.PX_PHYSICS_VERSION >> 16) & 0xff,
                (Constants.PX_PHYSICS_VERSION >> 8) & 0xff
        );

        PxDefaultAllocator allocator = new PxDefaultAllocator();
        PxErrorCallback errorCb = new PhysicsErrorCallback();
        this.foundation = PxTopLevelFunctions.CreateFoundation(version, allocator, errorCb);
        if (this.foundation == null)
            throw new RuntimeException("Failed to create PhysX Foundation object!");

        this.physics = PxTopLevelFunctions.CreatePhysics(version, this.foundation, new PxTolerancesScale());
        if (this.physics == null)
            throw new RuntimeException("Failed to create PhysX Foundation object!");

        this.dispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        PxTopLevelFunctions.InitExtensions(this.physics);
        PxVehicleTopLevelFunctions.InitVehicleExtension(this.foundation);

        INSTANCE = this;
    }

    public AppleTree getOrCreatePhysicsLevel(ServerLevel level) {
        return TREES.computeIfAbsent(level.dimension(), ignored -> new AppleTree(level));
    }

    public void cleanup() {
        this.dispatcher.destroy();
        this.physics.destroy();
        this.foundation.release();
    }

    public static Orchard getInstance() {
        return INSTANCE;
    }

    static class PhysicsErrorCallback extends PxErrorCallbackImpl {
        private final Logger errorLogger = LogUtils.getLogger();

        private final Map<PxErrorCodeEnum, String> codeNames = new HashMap<>() {{
            put(PxErrorCodeEnum.eDEBUG_INFO, "DEBUG_INFO");
            put(PxErrorCodeEnum.eDEBUG_WARNING, "DEBUG_WARNING");
            put(PxErrorCodeEnum.eINVALID_PARAMETER, "INVALID_PARAMETER");
            put(PxErrorCodeEnum.eINVALID_OPERATION, "INVALID_OPERATION");
            put(PxErrorCodeEnum.eOUT_OF_MEMORY, "OUT_OF_MEMORY");
            put(PxErrorCodeEnum.eINTERNAL_ERROR, "INTERNAL_ERROR");
            put(PxErrorCodeEnum.eABORT, "ABORT");
            put(PxErrorCodeEnum.ePERF_WARNING, "PERF_WARNING");
        }};

        @Override
        public void reportError(PxErrorCodeEnum code, String message, String file, int line) {
            String codeName = codeNames.getOrDefault(code, "code: " + code);
            String logMessage = String.format("[%s] %s (%s:%d)\n", codeName, message, file, line);

            switch (code) {
                case eDEBUG_INFO, eDEBUG_WARNING -> this.errorLogger.debug(logMessage);
                case ePERF_WARNING -> this.errorLogger.warn(logMessage);
                default -> this.errorLogger.error(logMessage);
            }
        }
    }
}
