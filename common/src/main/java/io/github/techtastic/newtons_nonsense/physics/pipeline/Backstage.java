package io.github.techtastic.newtons_nonsense.physics.pipeline;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.extensions.*;
import physx.geometry.*;
import physx.physics.*;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Backstage {
    public static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
    private static final PxFoundation PX_FOUNDATION;
    private static final PxPhysics PX_PHYSICS;
    private static final PxCookingParams PX_COOKING_PARAMS;
    private static final PxCpuDispatcher PX_DEFAULT_DISPATCHER;
    private static final PxFilterData DEFAULT_FILTER_DATA;
    private static final PxSerializationRegistry PX_SERIALIZATION_REGISTRY;

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
            PxSceneDesc sceneDesc = PxSceneDesc.createAt(mem, MemoryStack::nmalloc, PX_PHYSICS.getTolerancesScale());
            PxVec3 tempVec = PxVec3.createAt(mem, MemoryStack::nmalloc, 0f, -9.81f, 0f);
            sceneDesc.setGravity(tempVec);
            sceneDesc.setCpuDispatcher(PX_DEFAULT_DISPATCHER);
            sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
            sceneDesc.setSanityBounds(getMaxBounds());
            sceneDesc.setBroadPhaseType(PxBroadPhaseTypeEnum.eABP);

            return PX_PHYSICS.createScene(sceneDesc);
        }
    }

    public static PxBounds3 getMaxBounds() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxBounds3 bounds = PxBounds3.createAt(mem, MemoryStack::nmalloc);
            PxVec3 tempVec = PxVec3.createAt(mem, MemoryStack::nmalloc, 30_000_000, 1_000, 30_000_000);
            bounds.setMaximum(tempVec);
            tempVec.setX(-tempVec.getX());
            tempVec.setY(-128);
            tempVec.setZ(-tempVec.getZ());
            bounds.setMinimum(tempVec);
            return bounds;
        }
    }

    public static PxMaterial createMaterial(float staticFriction, float dynamicFriction, float restitution) {
        return PX_PHYSICS.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    public static PxRigidDynamic createDynamicBox(float globalX, float globalY, float globalZ, PxMaterial material) {
        PxShape shape = createBoxShape(.5f, .5f, .5f, 0, 0, 0, material);
        return createDynamicBodyWithShapes(globalX, globalY, globalZ, shape);
    }

    public static PxRigidStatic createStaticBodyWithShapes(float globalX, float globalY, float globalZ, PxShape... shapes) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 globalPos = PxVec3.createAt(mem, MemoryStack::nmalloc, globalX, globalY, globalZ);
            PxTransform pose = PxTransform.createAt(mem, MemoryStack::nmalloc, globalPos);
            PxRigidStatic body = PX_PHYSICS.createRigidStatic(pose);
            for (PxShape shape : shapes) {
                body.attachShape(shape);
            }
            return body;
        }
    }

    public static PxRigidDynamic createDynamicBodyWithShapes(float globalX, float globalY, float globalZ, PxShape... shapes) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 globalPos = PxVec3.createAt(mem, MemoryStack::nmalloc, globalX, globalY, globalZ);
            PxTransform pose = PxTransform.createAt(mem, MemoryStack::nmalloc, globalPos);
            PxRigidDynamic body = PX_PHYSICS.createRigidDynamic(pose);
            for (PxShape shape : shapes) {
                body.attachShape(shape);
            }
            return body;
        }
    }

    public static PxShape createBoxShape(float lenX, float lenY, float lenZ, float offsetX, float offsetY, float offsetZ, PxMaterial material) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxBoxGeometry geo = PxBoxGeometry.createAt(mem, MemoryStack::nmalloc, lenX, lenY, lenZ);
            PxVec3 offset = PxVec3.createAt(mem, MemoryStack::nmalloc, offsetX, offsetY, offsetZ);
            PxTransform pose = PxTransform.createAt(mem, MemoryStack::nmalloc, offset);
            PxShapeFlags shapeFlags = new PxShapeFlags((byte) (PxShapeFlagEnum.eSIMULATION_SHAPE.value | PxShapeFlagEnum.eSCENE_QUERY_SHAPE.value));
            PxShape shape = PX_PHYSICS.createShape(geo, material, false, shapeFlags);
            shape.setLocalPose(pose);
            shape.setSimulationFilterData(DEFAULT_FILTER_DATA);
            return shape;
        }
    }

    public static PxShape[] getChunkAsShapes(ChunkAccess chunk, @NotNull ServerLevel level) {
        Registry<PxMaterial> materialRegistry = level.registryAccess().lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY);
        PxMaterial defaultMaterial = materialRegistry.getValueOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

        ArrayList<PxShape> shapes = new ArrayList<>();

        // Loop over sections that could contain blocks
        for (int s = chunk.getMinSectionY(); s <= chunk.getHighestFilledSectionIndex(); s++) {
            // Ignore empty sections
            if (chunk.isSectionEmpty(s))
                continue;

            // Loop over Blocks in Section
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(s));
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                for (int y = 0; y < LevelChunkSection.SECTION_HEIGHT; y++) {
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        // Ignore Air
                        if (state.isAir())
                            continue;

                        BlockPos truePos = chunk.getPos().getBlockAt(x, s * LevelChunkSection.SECTION_HEIGHT + y, z);

                        // I'll make a VoxelShape to PxShape later...
                        // Adjust Material here

                        PxShape shape = Backstage.createBoxShape(
                                .5f,
                                .5f,
                                .5f,
                                (float) (truePos.getCenter().x - chunk.getPos().x),
                                (float) truePos.getCenter().y,
                                (float) (truePos.getCenter().z - chunk.getPos().z),
                                defaultMaterial
                        );

                        shapes.addLast(shape);
                    }
                }
            }
        }

        return shapes.toArray(new PxShape[0]);
    }

    static {
        // create PhysX foundation object
        PxDefaultAllocator allocator = new PxDefaultAllocator();
        PxErrorCallback errorCb = new CustomErrorCallback();
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
