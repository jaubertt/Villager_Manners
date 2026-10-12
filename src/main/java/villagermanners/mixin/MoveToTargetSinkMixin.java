package villagermanners.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villagermanners.Workstations;

@Mixin(MoveToTargetSink.class)
public abstract class MoveToTargetSinkMixin {
	@WrapOperation(method = "tryComputePath", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;"))
	private @Nullable Path villagermanners$besideWorkstation(PathNavigation navigation, BlockPos target, int reachRange, Operation<Path> original,
		@Local(argsOnly = true) Mob body, @Local(argsOnly = true) WalkTarget walkTarget) {
		if (body instanceof Villager villager) {
			return Workstations.walkPath(villager, navigation, walkTarget, target, () -> original.call(navigation, target, reachRange));
		}
		return original.call(navigation, target, reachRange);
	}

	@ModifyReturnValue(method = "reachedTarget", at = @At("RETURN"))
	private boolean villagermanners$arrived(boolean reached, @Local(argsOnly = true) Mob body, @Local(argsOnly = true) WalkTarget walkTarget) {
		return body instanceof Villager villager ? Workstations.arrived(villager, walkTarget, reached) : reached;
	}

	@Inject(method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)V", at = @At("HEAD"))
	private void villagermanners$stuckSteppingOff(ServerLevel level, Mob body, long timestamp, CallbackInfo ci) {
		if (body instanceof Villager villager) {
			Workstations.walkStopped(villager);
		}
	}
}
