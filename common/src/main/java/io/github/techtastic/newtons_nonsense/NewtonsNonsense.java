package io.github.techtastic.newtons_nonsense;


import com.mojang.logging.LogUtils;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import io.github.techtastic.newtons_nonsense.physics.networking.PhysicsObjectPayload;
import io.github.techtastic.newtons_nonsense.physics.object.box.BoxPhysicsObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);

    private static final RegistrySupplier<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties()) {
        @Override
        public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
            if (useOnContext.getHand() != InteractionHand.MAIN_HAND)
                return InteractionResult.SUCCESS;

            if (useOnContext.getLevel() instanceof ServerLevel level) {
                Vec3 pos = useOnContext.getClickLocation();
                BoxPhysicsObject boxObject = new BoxPhysicsObject(pos, new Vec3(.5, .5, .5), level, level.getBlockState(useOnContext.getClickedPos()));

                Backend.getOrCreateServerPhysicsWorld(level).addNewPhysicsObject(boxObject);
                LOGGER.info("Test Stick Used Successfully!");
                return InteractionResult.SUCCESS;
            }

            return super.useOn(useOnContext);
        }
    });

    public static void init() {
        int version = PxTopLevelFunctions.getPHYSICS_VERSION();
        LOGGER.info("PhysX Version: {}.{}.{}",
                version >> 24,
                (version >> 16) & 0xff,
                (version >> 8) & 0xff
        );

        LifecycleEvent.SERVER_STARTED.register(Backend::getOrCreateInstance);
        LifecycleEvent.SERVER_STOPPED.register(server -> Backend.getOrCreateInstance(server).cleanup());

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> Backend.getOrCreateServerPhysicsWorld(level));
        //LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> Backend.getOrCreateServerPhysicsWorld(level));
        TickEvent.SERVER_LEVEL_POST.register(level -> Backend.getOrCreateServerPhysicsWorld(level).tick());
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(Backend::getOrCreateClientPhysicsWorld);

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PhysicsObjectPayload.TYPE, PhysicsObjectPayload.CODEC, (payload, context) -> {
            context.queue(() -> {
                ClientLevel level = Minecraft.getInstance().level;
                assert level != null;
                Backend.getOrCreateClientPhysicsWorld(level).update(payload.object());
            });
        });

        ITEMS.register();
    }

    public static void onVisualReload(ClientLevel level) {
        Backend.getOrCreateClientPhysicsWorld(level).onVisualReload(level);
    }

    public static void onChunkGenerate(ServerLevel level, LevelChunk chunk) {
        // Create/Load Ground and LOD ground
    }

    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        // Swap LOD Ground to Ground
        // Load all Physics Objects
    }

    public static void onChunkUnload(ServerLevel level, LevelChunk chunk) {
        // Swap Ground to LOD Ground
    }
}
