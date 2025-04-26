package io.github.techtastic.newtons_nonsense;

import dev.architectury.event.events.common.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.techtastic.newtons_nonsense.commands.NNCommands;
import io.github.techtastic.newtons_nonsense.physics.PhysicsChunkManager;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;
import physx.PxTopLevelFunctions;

import java.util.Set;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> DEBUG_STICK = ITEMS.register("debug_stick", () -> new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse("newtons_nonsense:debug_stick")))) {
        @Override
        public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
            Level level = useOnContext.getLevel();
            if (level.isClientSide) return InteractionResult.SUCCESS;

            BlockPos pos = useOnContext.getClickedPos();
            ChunkAccess chunk = level.getChunk(pos);

            Set<BlockPos> exposedBlocks = PhysicsChunkManager.findExposedBlocks(chunk, (ServerLevel) level);
            for (BlockPos exposed : exposedBlocks) {
                level.setBlockAndUpdate(exposed, Blocks.GOLD_BLOCK.defaultBlockState());
            }
            return InteractionResult.SUCCESS;
        }
    });

    public static void init() {
        // Write common init code here.

        System.out.println("PhysX Version: " + PxTopLevelFunctions.getPHYSICS_VERSION());

        Backstage.init();

        LifecycleEvent.SERVER_STOPPED.register(Backstage::onServerStop);

        LifecycleEvent.SERVER_LEVEL_LOAD.register(Stage::onServerLevelLoad);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(Stage::onServerLevelUnload);
        TickEvent.SERVER_LEVEL_POST.register(Stage::onServerLevelPostTick);

        BlockEvent.BREAK.register(Stage::onBlockBreak);
        BlockEvent.PLACE.register(Stage::onBlockPlace);

        CommandRegistrationEvent.EVENT.register(NNCommands::register);

        ITEMS.register();
    }
}
