package villagermanners.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import villagermanners.Workstations;

@Mixin(WorkAtPoi.class)
public abstract class WorkAtPoiMixin {
	@ModifyReturnValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;)Z",
		at = @At("RETURN"))
	private boolean villagermanners$notFromOnTop(boolean canStart, @Local(argsOnly = true) Villager villager) {
		return canStart && !Workstations.onOwnWorkstation(villager);
	}

	@ModifyReturnValue(method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)Z",
		at = @At("RETURN"))
	private boolean villagermanners$stopsOnTop(boolean canGoOn, @Local(argsOnly = true) Villager villager) {
		return canGoOn && !Workstations.onOwnWorkstation(villager);
	}
}
