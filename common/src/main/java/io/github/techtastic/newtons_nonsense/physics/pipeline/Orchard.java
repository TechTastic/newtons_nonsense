package io.github.techtastic.newtons_nonsense.physics.pipeline;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.cooking.PxTriangleMeshDesc;
import physx.extensions.PxGjkQueryExt;
import physx.extensions.PxSerialization;
import physx.extensions.PxSerializationRegistry;
import physx.physics.PxFilterData;
import physx.physics.PxPhysics;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.HashMap;
import java.util.Map;

public class Orchard {
    public static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
    private static final PxFoundation PX_FOUNDATION;
    private static final PxPhysics PX_PHYSICS;
    private static final PxCookingParams PX_COOKING_PARAMS;
    private static final PxCpuDispatcher PX_DEFAULT_DISPATCHER;
    private static final PxFilterData DEFAULT_FILTER_DATA;
    private static final PxSerializationRegistry PX_SERIALIZATION_REGISTRY;

    public static void onLevelLoad(ServerLevel level) {
    }

    public static void getShapeFromState(BlockGetter level, BlockState state, BlockPos pos) {
        VoxelShape voxel = state.getCollisionShape(level, pos);

        PxTriangleMeshDesc desc = new PxTriangleMeshDesc();
    }

    static class CustomErrorCallback extends PxErrorCallbackImpl {
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
            System.out.printf("[%s] %s (%s:%d)\n", codeName, message, file, line);
        }
    }

    static {
        // create PhysX foundation object
        PxDefaultAllocator allocator = new PxDefaultAllocator();
        PxErrorCallback errorCb = new Backstage.CustomErrorCallback();
        PX_FOUNDATION = PxTopLevelFunctions.CreateFoundation(PX_PHYSICS_VERSION, allocator, errorCb);

        // create PhysX main physics object
        PxTolerancesScale tolerances = new PxTolerancesScale();
        PX_PHYSICS = PxTopLevelFunctions.CreatePhysics(PX_PHYSICS_VERSION, PX_FOUNDATION, tolerances);
        DEFAULT_FILTER_DATA = new PxFilterData(0, 0, 0, 0);
        DEFAULT_FILTER_DATA.setWord0(1);          // collision group: 0 (i.e. 1 << 0)
        DEFAULT_FILTER_DATA.setWord1(0xffffffff); // collision mask: collide with everything
        DEFAULT_FILTER_DATA.setWord2(0);          // no additional collision flags
        DEFAULT_FILTER_DATA.setWord3(0);          // word3 is currently not used

        PX_COOKING_PARAMS = new PxCookingParams(tolerances);

        PX_DEFAULT_DISPATCHER = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        PxTopLevelFunctions.InitExtensions(PX_PHYSICS);
        PxVehicleTopLevelFunctions.InitVehicleExtension(PX_FOUNDATION);

        PX_SERIALIZATION_REGISTRY = PxSerialization.createSerializationRegistry(PX_PHYSICS);
    }
}
