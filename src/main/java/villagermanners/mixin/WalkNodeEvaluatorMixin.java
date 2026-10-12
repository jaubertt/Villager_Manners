package villagermanners.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import villagermanners.MannersConfig;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {
	@ModifyReturnValue(method = "getPathTypeOfMob(Lnet/minecraft/world/level/pathfinder/PathfindingContext;IIILnet/minecraft/world/entity/Mob;)Lnet/minecraft/world/level/pathfinder/PathType;", at = @At("RETURN"))
	private PathType villagermanners$avoidOpenTrapdoors(PathType type, PathfindingContext context, int x, int y, int z, Mob mob) {
		if ((type != PathType.TRAPDOOR && type != PathType.WALKABLE) || !(mob instanceof Villager) || !MannersConfig.get().trapdoors) {
			return type;
		}
		if (villagermanners$openTrapdoor(context, x, y, z) || villagermanners$openTrapdoor(context, x, y + 1, z)) {
			return PathType.BLOCKED;
		}
		return type;
	}

	@Unique
	private static boolean villagermanners$openTrapdoor(PathfindingContext context, int x, int y, int z) {
		BlockState state = context.getBlockState(new BlockPos(x, y, z));
		return state.is(BlockTags.TRAPDOORS) && state.getOptionalValue(BlockStateProperties.OPEN).orElse(false);
	}
}
