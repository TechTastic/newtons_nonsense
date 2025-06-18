package io.github.techtastic.newtons_nonsense.physics.pipeline;

import com.google.common.collect.ImmutableList;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.cooking.PxTriangleMeshDesc;
import physx.extensions.PxGjkQueryExt;
import physx.extensions.PxSerialization;
import physx.extensions.PxSerializationRegistry;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxGeometry;
import physx.physics.*;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Orchard {
    public static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
    private static final PxFoundation PX_FOUNDATION;
    private static final PxPhysics PX_PHYSICS;
    private static final PxCookingParams PX_COOKING_PARAMS;
    private static final PxCpuDispatcher PX_DEFAULT_DISPATCHER;
    private static final PxFilterData DEFAULT_FILTER_DATA;
    private static final PxSerializationRegistry PX_SERIALIZATION_REGISTRY;

    private static final ConcurrentHashMap<BlockState, ImmutableList<PxShape>> STATE_SHAPE_MAP = new ConcurrentHashMap<>();

    public static void init() {}

    public static PxMaterial createMaterial(float staticFriction, float dynamicFriction, float restitution) {
        return PX_PHYSICS.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    public static PxScene createEmptyScene() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxSceneDesc sceneDesc = PxSceneDesc.createAt(mem, MemoryStack::nmalloc, PX_PHYSICS.getTolerancesScale());
            PxVec3 tempVec = PxVec3.createAt(mem, MemoryStack::nmalloc, 0f, -9.81f, 0f);
            sceneDesc.setGravity(tempVec);
            sceneDesc.setCpuDispatcher(PX_DEFAULT_DISPATCHER);
            sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());
            sceneDesc.setBroadPhaseType(PxBroadPhaseTypeEnum.eABP);

            return PX_PHYSICS.createScene(sceneDesc);
        }
    }

    public static void onServerLoad(MinecraftServer server) {
        System.out.println("Initializing State-to-Shapes Map...");
        server.registryAccess().lookup(BuiltInRegistries.BLOCK.key()).ifPresent(reg ->
                reg.asHolderIdMap().forEach(holder -> {
                    System.out.println("\tInitializing Shapes for " + holder.getRegisteredName() + "...");

                    holder.value().getStateDefinition().getPossibleStates().forEach(s -> {
                        VoxelShape shape = s.getCollisionShape(createDummyBlockGetter(s), new BlockPos(0, 0, 0));
                        if (shape.isEmpty()) return;
                        STATE_SHAPE_MAP.computeIfAbsent(s, state -> {
                            ImmutableList<PxShape> list = ImmutableList.copyOf(shape.toAabbs().stream().map(Orchard::getShapeFromAABB).toList());

                            System.out.println("\t\t" + state + " has " + list.size() + " shapes!");

                            return list;
                        });
                    });
                })
        );
    }

    public static void onLevelLoad(ServerLevel level) {
    }

    private static BlockGetter createDummyBlockGetter(BlockState state) {
        return new BlockGetter() {
            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos blockPos) {
                return null;
            }

            @Override
            public @NotNull BlockState getBlockState(BlockPos blockPos) {
                return state;
            }

            @Override
            public @NotNull FluidState getFluidState(BlockPos blockPos) {
                return state.getFluidState();
            }

            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public int getMinY() {
                return 0;
            }
        };
    }

    public static PxShape getShapeFromAABB(AABB aabb) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxBoxGeometry geom = PxBoxGeometry.createAt(mem, MemoryStack::nmalloc, .5f, .5f, .5f);
            return PX_PHYSICS.createShape(geom, createMaterial(.5f, .5f, .5f), false);
        }
    }

    public static PxTransform makeNewTransform() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxVec3 vec = PxVec3.createAt(mem, MemoryStack::nmalloc, 0, 0, 0);
            PxQuat quat = PxQuat.createAt(mem, MemoryStack::nmalloc, 0, 0, 0, 0);

            return new PxTransform(vec, quat);
        }
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
