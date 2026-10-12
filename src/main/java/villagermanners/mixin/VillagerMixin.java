package villagermanners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import villagermanners.access.WorkstationMemory;

@Mixin(Villager.class)
public abstract class VillagerMixin implements WorkstationMemory {
	@Unique
	private @Nullable BlockPos villagermanners$blockedWorkstation;
	@Unique
	private long villagermanners$blockedSince;

	@Override
	public @Nullable BlockPos villagermanners$blockedWorkstation() {
		return this.villagermanners$blockedWorkstation;
	}

	@Override
	public long villagermanners$blockedSince() {
		return this.villagermanners$blockedSince;
	}

	@Override
	public void villagermanners$sidesBlocked(BlockPos workstation, long gameTime) {
		this.villagermanners$blockedWorkstation = workstation;
		this.villagermanners$blockedSince = gameTime;
	}

	@Override
	public void villagermanners$sidesReached(BlockPos workstation) {
		if (workstation.equals(this.villagermanners$blockedWorkstation)) {
			this.villagermanners$blockedWorkstation = null;
		}
	}
}
