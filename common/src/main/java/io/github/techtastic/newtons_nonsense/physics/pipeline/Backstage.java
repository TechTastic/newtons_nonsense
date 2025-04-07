package io.github.techtastic.newtons_nonsense.physics.pipeline;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.cooking.PxTriangleMeshDesc;
import physx.extensions.PxRigidBodyExt;
import physx.extensions.PxSerialization;
import physx.extensions.PxSerializationRegistry;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxGeometry;
import physx.geometry.PxTriangleMesh;
import physx.physics.*;
import physx.support.PxArray_PxU32;
import physx.support.PxArray_PxVec3;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Backstage {
    public static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();

    public static final PxFoundation foundation;
    public static final PxPhysics physics;
    public static final PxCookingParams cookingParams;

    public static final PxCpuDispatcher defaultDispatcher;
    public static final PxMaterial defaultMaterial;
    public static final PxFilterData defaultFilterData;

    public static final PxSerializationRegistry serializer;

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

    public static void onServerStop(MinecraftServer server) {
        server.getAllLevels().forEach(level -> Stage.getOrCreateStage(level).free());
    }

    public static void init() {}

    public static PxScene createEmptyScene() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxSceneDesc sceneDesc = PxSceneDesc.createAt(mem, MemoryStack::nmalloc, physics.getTolerancesScale());
            sceneDesc.setGravity(PxVec3.createAt(mem, MemoryStack::nmalloc, 0f, -9.81f, 0f));
            sceneDesc.setCpuDispatcher(defaultDispatcher);
            sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
            return physics.createScene(sceneDesc);
        }
    }

    public static PxRigidDynamic createDefaultBox(float posX, float posY, float posZ) {
        return createDefaultBox(posX, posY, posZ, defaultFilterData);
    }

    public static PxRigidDynamic createDefaultBox(float posX, float posY, float posZ, PxFilterData simFilterData) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxTransform pose = PxTransform.createAt(mem, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            PxShape shape = createDefaultBoxShape(posX, posY, posZ, pose);
            PxRigidDynamic body = physics.createRigidDynamic(pose);
            shape.setSimulationFilterData(simFilterData);
            body.attachShape(shape);
            shape.release();
            PxRigidBodyExt.setMassAndUpdateInertia(body, 1f);
            return body;
        }
    }

    public static PxShape createDefaultBoxShape(float posX, float posY, float posZ) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            return createDefaultBoxShape(posX, posY, posZ, PxTransform.createAt(mem, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity));
        }
    }

    public static PxShape createDefaultBoxShape(float posX, float posY, float posZ, PxTransform pose) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxBoxGeometry box = createBoxGeometry(0.5f, 0.5f, 0.5f);
            pose.setP(PxVec3.createAt(mem, MemoryStack::nmalloc, posX, posY, posZ));
            PxShape shape = physics.createShape(box, defaultMaterial, true);
            shape.setLocalPose(pose);
            shape.setSimulationFilterData(Backstage.defaultFilterData);
            return shape;
        }
    }

    public static PxBoxGeometry createBoxGeometry(float lenX, float lenY, float lenZ) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            return PxBoxGeometry.createAt(mem, MemoryStack::nmalloc, lenX, lenY, lenZ);
        }
    }

    public static PxRigidStatic createStaticBody(PxGeometry fromGeometry, float posX, float posY, float posZ) {
        PxShape shape = physics.createShape(fromGeometry, defaultMaterial, true);
        shape.setSimulationFilterData(defaultFilterData);
        return createStaticBody(shape, posX, posY, posZ);
    }

    public static PxRigidStatic createStaticBody(PxShape fromShape, float posX, float posY, float posZ) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxTransform pose = PxTransform.createAt(mem, MemoryStack::nmalloc, PxIDENTITYEnum.PxIdentity);
            pose.setP(PxVec3.createAt(mem, MemoryStack::nmalloc, posX, posY, posZ));
            PxRigidStatic body = physics.createRigidStatic(pose);
            body.attachShape(fromShape);
            return body;
        }
    }

    public static PxTriangleMesh createTriangleMesh(Vec3[] points) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxArray_PxVec3 pointVector = new PxArray_PxVec3();
            PxArray_PxU32 indexVector = new PxArray_PxU32();
            PxVec3 tmpVec = PxVec3.createAt(mem, MemoryStack::nmalloc, 0f, 0f, 0f);
            for (int i = 0; i < points.length; i++) {
                Vec3 point = points[i];
                tmpVec.setX((float) point.x);
                tmpVec.setY((float) point.y);
                tmpVec.setZ((float) point.z);
                pointVector.pushBack(tmpVec);
                if (i > 0) {
                    indexVector.pushBack(0);
                    indexVector.pushBack(i);
                    indexVector.pushBack(i + 1);
                }
            }

            // create mesh descriptor
            PxBoundedData pointsData = PxBoundedData.createAt(mem, MemoryStack::nmalloc);
            pointsData.setCount(pointVector.size());
            pointsData.setStride(PxVec3.SIZEOF);
            pointsData.setData(pointVector.begin());

            PxBoundedData triangles = PxBoundedData.createAt(mem, MemoryStack::nmalloc);
            triangles.setCount(indexVector.size() / 3);
            triangles.setStride(4 * 3);     // 3 4-byte integer indices per triangle
            triangles.setData(indexVector.begin());

            PxTriangleMeshDesc desc = PxTriangleMeshDesc.createAt(mem, MemoryStack::nmalloc);
            desc.setPoints(pointsData);
            desc.setTriangles(triangles);

            // cook mesh and delete input data afterwards (no need to keep them around anymore)
            PxTriangleMesh mesh = PxTopLevelFunctions.CreateTriangleMesh(Backstage.cookingParams, desc);

            pointVector.destroy();
            indexVector.destroy();

            return mesh;
        }
    }

    public static void debugSimulateScene(PxScene scene, float duration, PxRigidActor printActor) {
        float step = 1/60f;
        float t = 0;
        for (int i = 0; i < duration / step; i++) {
            // print position of printActor 2 times per simulated sec
            if (printActor != null && i % 30 == 0) {
                PxVec3 pos = printActor.getGlobalPose().getP();
                System.out.printf(Locale.ENGLISH, "t = %.2f s, pos(%6.3f, %6.3f, %6.3f)\n", t, pos.getX(), pos.getY(), pos.getZ());
            }
            scene.simulate(step);
            scene.fetchResults(true);
            t += step;
        }
    }

    static {
        // create PhysX foundation object
        PxDefaultAllocator allocator = new PxDefaultAllocator();
        PxErrorCallback errorCb = new CustomErrorCallback();
        foundation = PxTopLevelFunctions.CreateFoundation(PX_PHYSICS_VERSION, allocator, errorCb);

        // create PhysX main physics object
        PxTolerancesScale tolerances = new PxTolerancesScale();
        physics = PxTopLevelFunctions.CreatePhysics(PX_PHYSICS_VERSION, foundation, tolerances);
        defaultMaterial = physics.createMaterial(0.5f, 0.5f, 0.5f);
        defaultFilterData = new PxFilterData(0, 0, 0, 0);
        defaultFilterData.setWord0(1);          // collision group: 0 (i.e. 1 << 0)
        defaultFilterData.setWord1(0xffffffff); // collision mask: collide with everything
        defaultFilterData.setWord2(0);          // no additional collision flags
        defaultFilterData.setWord3(0);          // word3 is currently not used

        cookingParams = new PxCookingParams(tolerances);

        defaultDispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);

        PxTopLevelFunctions.InitExtensions(physics);
        PxVehicleTopLevelFunctions.InitVehicleExtension(foundation);

        serializer = PxSerialization.createSerializationRegistry(physics);
    }
}
