package villagermanners.access;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public interface WorkstationMemory {
	@Nullable BlockPos villagermanners$blockedWorkstation();

	long villagermanners$blockedSince();

	void villagermanners$sidesBlocked(BlockPos workstation, long gameTime);

	void villagermanners$sidesReached(BlockPos workstation);
}
