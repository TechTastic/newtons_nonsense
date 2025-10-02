package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.registries.Material;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.physics.PxPhysics;

import java.util.HashMap;
import java.util.Map;

public class Backend {
    protected static final PxFoundation FOUNDATION = PxTopLevelFunctions
            .CreateFoundation(PxTopLevelFunctions.getPHYSICS_VERSION(), new PxDefaultAllocator(), new OrchardErrorCallback());
    public static final PxPhysics PHYSICS = PxTopLevelFunctions
            .CreatePhysics(PxTopLevelFunctions.getPHYSICS_VERSION(), FOUNDATION, new PxTolerancesScale());
    protected static final PxCpuDispatcher DISPATCHER = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);
    private static final Logger LOGGER = LoggerFactory.getLogger("NewtonsNonsense/Backend");
    private static final HashMap<ResourceKey<Level>, ServerPhysicsWorld> SERVER_WORLDS = new HashMap<>();
    private static final HashMap<ResourceKey<Level>, ClientPhysicsWorld> CLIENT_WORLDS = new HashMap<>();

    public static void init() {
        LOGGER.info("PhysX has been Initialized!");
    }

    @NotNull
    public static PhysicsWorld<?> getOrCreatePhysicsWorld(@NotNull Level level) {
        if (level instanceof ServerLevel serverLevel)
            return SERVER_WORLDS.computeIfAbsent(level.dimension(), ignored ->
                    new ServerPhysicsWorld(serverLevel));
        if (level instanceof ClientLevel clientLevel)
            return CLIENT_WORLDS.computeIfAbsent(level.dimension(), ignored ->
                    new ClientPhysicsWorld(clientLevel));
        throw new RuntimeException("Invalid Level for Physics! This should NEVER trigger!");
    }

    public static void cleanup(@Nullable MinecraftServer server) {
        if (server != null)
            server.registryAccess().lookup(Material.REGISTRY_KEY).ifPresent(reg -> reg.listElements().forEach(ref -> ref.value().destroy()));

        DISPATCHER.destroy();
        PHYSICS.destroy();
        FOUNDATION.release();
    }

    private static class OrchardErrorCallback extends PxErrorCallbackImpl {
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
                case eDEBUG_INFO, eDEBUG_WARNING -> LOGGER.debug(logMessage);
                case ePERF_WARNING -> LOGGER.warn(logMessage);
                default -> LOGGER.error(logMessage);
            }
        }
    }
}
