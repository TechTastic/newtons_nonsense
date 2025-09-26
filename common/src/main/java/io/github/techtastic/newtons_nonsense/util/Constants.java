package io.github.techtastic.newtons_nonsense.util;

import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.physics.PxPhysics;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final int PX_PHYSICS_VERSION;

    public static final PxFoundation FOUNDATION;
    public static final PxPhysics PHYSICS;
    public static final PxCookingParams COOKING_PARAMS;

    public static final PxCpuDispatcher DEFAULT_DISPATCHER;

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
        PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();

        // create PhysX foundation object
        PxDefaultAllocator allocator = new PxDefaultAllocator();
        PxErrorCallback errorCb = new CustomErrorCallback();
        FOUNDATION = PxTopLevelFunctions.CreateFoundation(PX_PHYSICS_VERSION, allocator, errorCb);

        // create PhysX main physics object
        PxTolerancesScale tolerances = new PxTolerancesScale();
        PHYSICS = PxTopLevelFunctions.CreatePhysics(PX_PHYSICS_VERSION, FOUNDATION, tolerances);
        //defaultMaterial = physics.createMaterial(0.5f, 0.5f, 0.5f);
        //defaultFilterData = new PxFilterData(0, 0, 0, 0);
        //defaultFilterData.setWord0(1);          // collision group: 0 (i.e. 1 << 0)
        //defaultFilterData.setWord1(0xffffffff); // collision mask: collide with everything
        //defaultFilterData.setWord2(0);          // no additional collision flags
        //defaultFilterData.setWord3(0);          // word3 is currently not used

        COOKING_PARAMS = new PxCookingParams(tolerances);

        DEFAULT_DISPATCHER = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        PxTopLevelFunctions.InitExtensions(PHYSICS);
        PxVehicleTopLevelFunctions.InitVehicleExtension(FOUNDATION);
    }
}
