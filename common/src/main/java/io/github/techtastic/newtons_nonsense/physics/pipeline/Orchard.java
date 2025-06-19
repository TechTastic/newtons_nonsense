package io.github.techtastic.newtons_nonsense.physics.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.techtastic.newtons_nonsense.util.PhysxUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.cooking.PxCookingParams;
import physx.geometry.PxBoxGeometry;
import physx.physics.*;
import physx.vehicle2.PxVehicleTopLevelFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.techtastic.newtons_nonsense.NewtonsNonsense.MOD_ID;

public class Orchard {
    public static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
    private static final PxFoundation PX_FOUNDATION;
    protected static final PxPhysics PX_PHYSICS;
    private static final PxCookingParams PX_COOKING_PARAMS;
    private static final PxCpuDispatcher PX_DEFAULT_DISPATCHER;
    private static final PxFilterData DEFAULT_FILTER_DATA;

    private static final HashMap<ResourceKey<Level>, AppleTree> TREES;
    private static final ConcurrentHashMap<BlockState, ImmutableList<PxShape>> STATE_SHAPE_CACHE;

    public static void init() {}

    protected static PxMaterial createMaterial(float staticFriction, float dynamicFriction, float restitution) {
        return PX_PHYSICS.createMaterial(staticFriction, dynamicFriction, restitution);
    }

    protected static PxScene createEmptyScene() {
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

                    holder.value().getStateDefinition().getPossibleStates().forEach(state ->
                            getOrCreateShapesForState(server.registryAccess(), createDummyBlockGetter(state), new BlockPos(0, 0, 0), state)
                    );
                })
        );
    }

    public static void onServerStop(MinecraftServer server) {
        TREES.values().forEach(AppleTree::free);
        STATE_SHAPE_CACHE.values().forEach(list -> list.forEach(PxShape::release));
    }

    public static void onLevelLoad(ServerLevel level) {
        getTreeFromLevel(level).togglePause(false);
    }

    public static void onLevelUnload(ServerLevel level) {
        getTreeFromLevel(level).togglePause(true);
    }

    public static void onChunkLoad(ServerLevel level, ChunkAccess chunkAccess) {
        getTreeFromLevel(level).onChunkLoad(level, chunkAccess);
    }

    public static void onChunkUnload(ServerLevel level, ChunkAccess chunkAccess) {
        getTreeFromLevel(level).onChunkUnload(level, chunkAccess);
    }

    protected static ImmutableList<PxShape> getShapesFromDummyLevel(RegistryAccess access, BlockPos pos, BlockState state) {
        return getOrCreateShapesForState(access, createDummyBlockGetter(state), pos, state);
    }

    protected static ImmutableList<PxShape> getOrCreateShapesForState(RegistryAccess access, BlockGetter blockGetter, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getCollisionShape(blockGetter, pos);

        //TODO: Get Material Here
        // This is temporary!
        Registry<PxMaterial> materials = access.lookupOrThrow(MaterialRegistry.MATERIAL_REGISTRY_KEY);
        PxMaterial defaultMaterial = materials.getValue(MaterialRegistry.DEFAULT_MATERIAL);

        if (shape.isEmpty()) return ImmutableList.of();
        return STATE_SHAPE_CACHE.computeIfAbsent(state, s -> ImmutableList.copyOf(shape.toAabbs().stream().map(aabb ->
                getShapeFromAABB(aabb, defaultMaterial)
        ).toList()));
    }

    public static AppleTree getTreeFromLevel(ServerLevel level) {
        return TREES.computeIfAbsent(level.dimension(), key -> new AppleTree());
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

    private static PxShape getShapeFromAABB(AABB aabb, PxMaterial material) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            PxBoxGeometry geom = PxBoxGeometry.createAt(mem,
                    MemoryStack::nmalloc,
                    (float) aabb.getXsize() / 2,
                    (float) aabb.getYsize() / 2,
                    (float) aabb.getZsize() / 2
            );

            PxShape shape = PX_PHYSICS.createShape(geom, material, false);
            shape.getLocalPose().setP(PhysxUtils.toPxVec3(aabb.getCenter()));

            return shape;
        }
    }

    private static class CustomErrorCallback extends PxErrorCallbackImpl {
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

    public static class MaterialRegistry {
        public static final Codec<PxMaterial> MATERIAL_CODEC;
        public static final ResourceKey<Registry<PxMaterial>> MATERIAL_REGISTRY_KEY;
        public static final ResourceKey<PxMaterial> DEFAULT_MATERIAL;

        static {
            MATERIAL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("staticFriction").forGetter(PxMaterial::getStaticFriction),
                    Codec.FLOAT.fieldOf("dynamicFriction").forGetter(PxMaterial::getDynamicFriction),
                    Codec.FLOAT.fieldOf("restitution").forGetter(PxMaterial::getRestitution)
            ).apply(instance, Orchard::createMaterial));

            MATERIAL_REGISTRY_KEY = ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "materials"));

            DEFAULT_MATERIAL = ResourceKey.create(MATERIAL_REGISTRY_KEY,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "default"));
        }
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

        TREES = new HashMap<>();
        STATE_SHAPE_CACHE = new ConcurrentHashMap<>();
    }
}
