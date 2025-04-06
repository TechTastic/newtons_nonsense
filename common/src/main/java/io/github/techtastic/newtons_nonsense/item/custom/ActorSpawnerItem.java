package io.github.techtastic.newtons_nonsense.item.custom;

import io.github.techtastic.newtons_nonsense.mixinducks.BlockDisplayStateSetter;
import io.github.techtastic.newtons_nonsense.physics.Mimic;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.jetbrains.annotations.NotNull;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.physics.PxRigidDynamic;

public class ActorSpawnerItem extends Item {
    public ActorSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        // Spawn Actor at Clicked Position

        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel sLevel))
            return InteractionResult.SUCCESS;

        BlockPos pos = useOnContext.getClickedPos().relative(useOnContext.getClickedFace());
        Player player = useOnContext.getPlayer();

        createMimic(sLevel, pos, player != null && player.isShiftKeyDown());
        return InteractionResult.SUCCESS;
    }

    public void createMimic(ServerLevel level, BlockPos pos, boolean isStatic) {
        System.out.println(isStatic);
        Display.BlockDisplay mimic = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        ((BlockDisplayStateSetter) mimic).newtons_nonsense$setBlockState(Blocks.GOLD_BLOCK.defaultBlockState());
        mimic.setNoGravity(isStatic);
        mimic.setPos(pos.getX(), pos.getY(), pos.getZ());
        level.addFreshEntity(mimic);
        Stage.getOrCreateStage(level).mimics.add(new Mimic(mimic, isStatic));
    }
}
