package io.github.techtastic.newtons_nonsense.physics;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.physics.PxPhysics;

import java.util.HashMap;
import java.util.Map;

public class Backend {
    private static PxFoundation foundation;
    private static PxPhysics physics;

    private static final Logger LOGGER = LoggerFactory.getLogger("NewtonsNonsense/Backend");
    private static final HashMap<ResourceKey<Level>, ServerPhysicsWorld> SERVER_WORLDS = new HashMap<>();
    private static final HashMap<ResourceKey<Level>, ClientPhysicsWorld> CLIENT_WORLDS = new HashMap<>();
    private static Backend instance;
    private final MinecraftServer server;
    private final PxCpuDispatcher dispatcher;

    private Backend(MinecraftServer server) {
        this.server = server;

        LOGGER.info("Initializing PhysX on the server...");

        Backend.getFoundation();
        Backend.getPhysics();
        this.dispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        LOGGER.info("PhysX has been Initialized!");
    }

    @NotNull
    public static Backend getOrCreateInstance(@NotNull MinecraftServer server) {
        if (instance == null)
            instance = new Backend(server);
        return instance;
    }

    @NotNull
    public static ServerPhysicsWorld getOrCreateServerPhysicsWorld(@NotNull ServerLevel level) {
        return SERVER_WORLDS.computeIfAbsent(level.dimension(), ignored -> new ServerPhysicsWorld(Backend.getOrCreateInstance(level.getServer()), level));
    }

    @NotNull
    public static ClientPhysicsWorld getOrCreateClientPhysicsWorld(@NotNull ClientLevel level) {
        return CLIENT_WORLDS.computeIfAbsent(level.dimension(), ignored -> new ClientPhysicsWorld(level));
    }

    public static PxFoundation getFoundation() {
        if (foundation == null)
            foundation = PxTopLevelFunctions.CreateFoundation(
                    PxTopLevelFunctions.getPHYSICS_VERSION(),
                    new PxDefaultAllocator(),
                    new OrchardErrorCallback()
            );
        return foundation;
    }

    public static PxPhysics getPhysics() {
        if (physics == null)
            physics = PxTopLevelFunctions.CreatePhysics(
                    PxTopLevelFunctions.getPHYSICS_VERSION(),
                    getFoundation(),
                    new PxTolerancesScale()
            );
        return physics;
    }

    @Environment(EnvType.SERVER)
    MinecraftServer getServer() {
        return this.server;
    }

    PxCpuDispatcher getDispatcher() {
        return this.dispatcher;
    }

    public void cleanup() {
        this.dispatcher.destroy();
        physics.destroy();
        foundation.release();
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
