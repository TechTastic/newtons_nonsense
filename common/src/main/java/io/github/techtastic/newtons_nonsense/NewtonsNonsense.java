package io.github.techtastic.newtons_nonsense;

import dev.architectury.event.events.common.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.techtastic.newtons_nonsense.commands.NNCommands;
import io.github.techtastic.newtons_nonsense.item.custom.ActorSpawnerItem;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<ActorSpawnerItem> ACTOR_SPAWNER = ITEMS.register("actor_spawner",
            () -> new ActorSpawnerItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "actor_spawner"))))
    );

    public static void init() {
        // Write common init code here.

        System.out.println("PhysX Version: " + PxTopLevelFunctions.getPHYSICS_VERSION());

        Backstage.init();

        LifecycleEvent.SERVER_STOPPED.register(Backstage::onServerStop);

        LifecycleEvent.SERVER_LEVEL_LOAD.register(Stage::onServerLevelLoad);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(Stage::onServerLevelUnload);
        TickEvent.SERVER_LEVEL_POST.register(Stage::onServerLevelPostTick);

        ChunkEvent.LOAD_DATA.register(Stage::onChunkLoad);

        CommandRegistrationEvent.EVENT.register(NNCommands::register);

        // Debug Stuff

        ITEMS.register();
    }
}
