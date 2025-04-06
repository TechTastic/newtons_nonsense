package io.github.techtastic.newtons_nonsense.mixin;

import io.github.techtastic.newtons_nonsense.mixinducks.BlockDisplayStateSetter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.BlockDisplay.class)
public abstract class MixinBlockDisplay implements BlockDisplayStateSetter {

    @Shadow public abstract void setBlockState(BlockState blockState);

    @Override
    public void newtons_nonsense$setBlockState(BlockState state) {
        setBlockState(state);
    }
}
