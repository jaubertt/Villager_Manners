package villagermanners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villagermanners.PlayerBeds;

@Mixin(ValidateNearbyPoi.class)
public abstract class PlayerBedMixin {
	@Inject(method = "bedIsOccupied(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private static void villagermanners$leavePlayerBed(ServerLevel level, BlockPos pos, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (PlayerBeds.claimed(level, pos)) {
			cir.setReturnValue(true);
		}
	}
}
