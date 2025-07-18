package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.physics.*;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.HashMap;

public class Orchard {
    public static final int PHYSX_VERSION;
    private static final PxDefaultAllocator ALLOCATOR;
    private static final LoggerErrorCallback ERROR_CALLBACK;
    private static final PxFoundation FOUNDATION;

    private static final PxTolerancesScale TOLERANCES;
    private static final PxPhysics PHYSICS;
    private static final PxFilterData DEFAULT_FILTER;
    private static final PxCookingParams COOKING_PARAMS;
    private static final PxDefaultCpuDispatcher DISPATCHER;

    public static final HashMap<ResourceKey<Level>, Tree> TREES;

    private static PxMaterial createMaterial(float staticFriction, float dynamicFriction, float restitution) {
        return PHYSICS.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    protected static PxRigidStatic createEmptyStaticBody(Vector3f pos, Quaternionf rot) {
        return PHYSICS.createRigidStatic(new PxTransform(new PxVec3(pos.x, pos.y, pos.z), new PxQuat(rot.x, rot.y, rot.z, rot.w)));
    }

    private static class LoggerErrorCallback extends PxErrorCallbackImpl {
        @Override
        public void reportError(PxErrorCodeEnum code, String message, String file, int line) {
            switch (code) {
                case eDEBUG_INFO -> NewtonsNonsense.LOGGER.debug("[INFO] {} ({}:{})", message, file, line);
                case eDEBUG_WARNING -> NewtonsNonsense.LOGGER.debug("[WARNING] {} ({}:{})", message, file, line);
                case eINVALID_PARAMETER -> NewtonsNonsense.LOGGER.error("[INVALID PARAMETER] {} ({}:{})", message, file, line);
                case eINVALID_OPERATION -> NewtonsNonsense.LOGGER.error("[INVALID OPERATION] {} ({}:{})", message, file, line);
                case eOUT_OF_MEMORY -> NewtonsNonsense.LOGGER.error("[OUT OF MEMORY] {} ({}:{})", message, file, line);
                case eINTERNAL_ERROR -> NewtonsNonsense.LOGGER.error("[INTERNAL ERROR] {} ({}:{})", message, file, line);
                case eABORT -> NewtonsNonsense.LOGGER.error("[ABORT] {} ({}:{})", message, file, line);
                case ePERF_WARNING -> NewtonsNonsense.LOGGER.warn("[PERFORMANCE] {} ({}:{})", message, file, line);
            }
        }
    }

    static {
        PHYSX_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
        ALLOCATOR = new PxDefaultAllocator();
        ERROR_CALLBACK = new LoggerErrorCallback();
        FOUNDATION = PxTopLevelFunctions.CreateFoundation(PHYSX_VERSION, ALLOCATOR, ERROR_CALLBACK);

        TOLERANCES = new PxTolerancesScale();
        PHYSICS = PxTopLevelFunctions.CreatePhysics(PHYSX_VERSION, FOUNDATION, TOLERANCES);
        DEFAULT_FILTER = new PxFilterData(
                1,              // collision group
                0xffffffff,     // collision mask
                0,              // additional collision flags
                0               // unused
        );
        COOKING_PARAMS = new PxCookingParams(TOLERANCES);
        DISPATCHER = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        PxTopLevelFunctions.InitExtensions(PHYSICS);
        PxVehicleTopLevelFunctions.InitVehicleExtension(FOUNDATION);

        TREES = new HashMap<>();
    }
}