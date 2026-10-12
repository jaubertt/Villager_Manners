package villagermanners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villagermanners.PlayerBeds;

@Mixin(VillagerGoalPackages.class)
public abstract class VillagerGoalPackagesMixin {
	@Inject(method = "validateBedPoi(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
	private static void villagermanners$skipPlayerBed(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (PlayerBeds.claimed(level, pos)) {
			cir.setReturnValue(false);
		}
	}
}
