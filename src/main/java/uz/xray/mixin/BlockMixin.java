package uz.xray.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uz.xray.XrayConfig;

@Mixin(Block.class)
public class BlockMixin {
    // Yangi imzo (3 argument)
    @Inject(at = @At("HEAD"), cancellable = true, require = 0,
            method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z")
    private static void xray$draw3(BlockState state, BlockState other, Direction side,
                                   CallbackInfoReturnable<Boolean> cir) {
        XrayConfig c = XrayConfig.get();
        if (c.xray) cir.setReturnValue(c.visible(state.getBlock()));
    }

    // Eski imzo (5 argument) - qaysi biri mavjud bo'lsa o'shanisi ishlaydi
    @Inject(at = @At("HEAD"), cancellable = true, require = 0,
            method = "shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z")
    private static void xray$draw5(BlockState state, BlockView world, BlockPos pos, Direction side,
                                   BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        XrayConfig c = XrayConfig.get();
        if (c.xray) cir.setReturnValue(c.visible(state.getBlock()));
    }
}
