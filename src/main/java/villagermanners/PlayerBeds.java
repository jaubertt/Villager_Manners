package villagermanners;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jspecify.annotations.Nullable;

public final class PlayerBeds {
	private PlayerBeds() {
	}

	public static boolean claimed(ServerLevel level, BlockPos pos) {
		if (!MannersConfig.get().playerBeds) {
			return false;
		}
		BlockPos head = head(level, pos);
		if (head == null) {
			return false;
		}
		for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
			ServerPlayer.RespawnConfig respawn = player.getRespawnConfig();
			if (respawn == null || respawn.respawnData().dimension() != level.dimension()) {
				continue;
			}
			if (head.equals(head(level, respawn.respawnData().pos()))) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable BlockPos head(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.is(BlockTags.BEDS) || !state.hasProperty(BedBlock.PART)) {
			return null;
		}
		return state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(BedBlock.FACING));
	}
}
