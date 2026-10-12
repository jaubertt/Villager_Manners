package villagermanners;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import villagermanners.access.WorkstationMemory;

public final class Workstations {
	public static final TagKey<Block> BEDS = kind("beds");
	public static final TagKey<Block> BELLS = kind("bells");
	public static final TagKey<Block> CHESTS = kind("chests");
	public static final TagKey<Block> CRAFTING_TABLES_AND_FURNACES = kind("crafting_tables_and_furnaces");
	public static final TagKey<Block> OTHER_FURNITURE = kind("other_furniture");
	public static final TagKey<Block> STAIRS = kind("stairs");
	public static final TagKey<Block> PRESSURE_PLATES = kind("pressure_plates");
	public static final int RETRY_TICKS = 1200;
	public static final double MAX_FEET_OFFSET = 0.65;
	public static final double MAX_DROP = 1.125;
	public static final double SLIDE_OFF_SPEED = 0.2;
	public static final double SLIDE_OFF_RATE = 0.35;

	private Workstations() {
	}

	private static TagKey<Block> kind(String name) {
		return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(VillagerManners.MOD_ID, "stand_beside/" + name));
	}

	public static boolean isWorkstation(BlockState state) {
		return PoiTypes.forState(state).map(type -> type.is(PoiTypeTags.ACQUIRABLE_JOB_SITE)).orElse(false);
	}

	public static boolean standsBeside(Villager villager, BlockState state) {
		MannersConfig config = MannersConfig.get();
		return config.workstations && isWorkstation(state)
			|| config.beds && state.is(BEDS)
				&& !(config.childrenJumpOnBeds && villager.isBaby() && state.is(BlockTags.VILLAGER_BABIES_CAN_JUMP_ON_BED))
			|| config.bells && state.is(BELLS)
			|| config.chests && state.is(CHESTS)
			|| config.craftingTablesAndFurnaces && state.is(CRAFTING_TABLES_AND_FURNACES)
			|| config.otherFurniture && state.is(OTHER_FURNITURE)
			|| config.stairs && state.is(STAIRS)
			|| config.pressurePlates && state.is(PRESSURE_PLATES);
	}

	private static boolean isTop(BlockState state) {
		return state.is(PRESSURE_PLATES);
	}

	private static boolean usedFromBeside(BlockState state) {
		return isWorkstation(state) || state.is(BlockTags.VILLAGERS_CAN_SLEEP_ON_BED);
	}

	public static boolean restsOnBlockBelow(BlockState state, Level level, BlockPos pos) {
		VoxelShape shape = state.getCollisionShape(level, pos);
		return shape.isEmpty() || shape.max(Direction.Axis.Y) < 0.5;
	}

	public static @Nullable BlockPos blockToStandBeside(Villager villager, BlockPos spot) {
		Level level = villager.level();
		if (!level.isLoaded(spot)) {
			return null;
		}
		BlockState state = level.getBlockState(spot);
		if (!standsBeside(villager, state) && isSolid(state)) {
			BlockPos above = spot.above();
			BlockState aboveState = level.getBlockState(above);
			if (isSolid(aboveState) && !standsBeside(villager, aboveState)) {
				BlockState top = level.getBlockState(above.above());
				return isTop(top) && standsBeside(villager, top) ? above : null;
			}
			spot = above;
			state = aboveState;
		}
		if (standsBeside(villager, state)) {
			return isTop(state) ? spot.below() : spot;
		}
		BlockPos below = spot.below();
		if (!restsOnBlockBelow(state, level, spot) || !level.isLoaded(below)) {
			return null;
		}
		BlockState belowState = level.getBlockState(below);
		if (!standsBeside(villager, belowState)) {
			return null;
		}
		return isTop(belowState) ? below.below() : below;
	}

	@SuppressWarnings("deprecation")
	private static boolean isSolid(BlockState state) {
		return state.isSolid();
	}

	public static Set<BlockPos> spotsBeside(Villager villager, BlockPos block) {
		Level level = villager.level();
		boolean closeEnoughOnly = usedFromBeside(level.getBlockState(block));
		Set<BlockPos> spots = new HashSet<>();
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos beside = block.relative(side);
			if (!level.isLoaded(beside)) {
				continue;
			}
			BlockState state = level.getBlockState(beside);
			if (standsBeside(villager, state)) {
				continue;
			}
			BlockPos spot = restsOnBlockBelow(state, level, beside) ? beside : beside.above();
			if (spot != beside && standsBeside(villager, level.getBlockState(spot))) {
				continue;
			}
			if (!level.getFluidState(spot).isEmpty()) {
				continue;
			}
			if (!closeEnoughOnly || Math.abs(feetHeight(level, spot) - (block.getY() + 0.5)) <= MAX_FEET_OFFSET) {
				spots.add(spot);
			}
		}
		return spots;
	}

	private static double feetHeight(Level level, BlockPos spot) {
		VoxelShape shape = level.getBlockState(spot).getCollisionShape(level, spot);
		return shape.isEmpty() ? WalkNodeEvaluator.getFloorLevel(level, spot) : spot.getY() + shape.max(Direction.Axis.Y);
	}

	public static boolean standsOn(Villager villager, BlockPos block) {
		BlockPos feet = villager.blockPosition();
		if (feet.getX() != block.getX() || feet.getZ() != block.getZ()) {
			return false;
		}
		if (feet.getY() == block.getY()) {
			return true;
		}
		Level level = villager.level();
		return feet.getY() == block.getY() + 1 && restsOnBlockBelow(level.getBlockState(feet), level, feet);
	}

	private static @Nullable BlockPos edgeHoldingUp(Villager villager) {
		if (villager.isPassenger() || villager.isSleeping() || !villager.onGround()) {
			return null;
		}
		BlockPos support = villager.mainSupportingBlockPos.orElse(null);
		if (support == null) {
			return null;
		}
		BlockPos feet = villager.blockPosition();
		if (support.getX() == feet.getX() && support.getZ() == feet.getZ()) {
			return null;
		}
		Level level = villager.level();
		BlockState above = level.getBlockState(support.above());
		return standsBeside(villager, level.getBlockState(support)) || isTop(above) && standsBeside(villager, above) ? support : null;
	}

	private static boolean slidesOff(Villager villager) {
		BlockPos edge = edgeHoldingUp(villager);
		return edge != null && canSlideOff(villager, edge);
	}

	private static boolean canSlideOff(Villager villager, BlockPos edge) {
		return !sidesBlocked(villager, edge) && landsSafely(villager);
	}

	private static boolean landsSafely(Villager villager) {
		Level level = villager.level();
		BlockPos.MutableBlockPos pos = villager.blockPosition().mutable();
		for (int i = 0; i < 3; i++) {
			BlockState state = level.getBlockState(pos);
			if (!state.getFluidState().isEmpty()) {
				return false;
			}
			double top = state.getCollisionShape(level, pos).max(Direction.Axis.Y, 0.5, 0.5);
			if (top != Double.NEGATIVE_INFINITY) {
				double drop = villager.getY() - (pos.getY() + top);
				return drop >= -1.0E-4 && drop <= MAX_DROP
					&& villager.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(villager, pos.above())) >= 0.0F;
			}
			pos.move(Direction.DOWN);
		}
		return false;
	}

	private static void slideToMiddle(Villager villager) {
		BlockPos feet = villager.blockPosition();
		double dx = towardsMiddle(feet.getX() + 0.5 - villager.getX());
		double dz = towardsMiddle(feet.getZ() + 0.5 - villager.getZ());
		villager.setDeltaMovement(dx, villager.getDeltaMovement().y, dz);
		villager.getMoveControl().setWantedPosition(villager.getX(), villager.getY(), villager.getZ(), 0.0);
	}

	private static double towardsMiddle(double offset) {
		return Math.max(-SLIDE_OFF_SPEED, Math.min(SLIDE_OFF_SPEED, offset * SLIDE_OFF_RATE));
	}

	private static BlockPos node(Villager villager) {
		return BlockPos.containing(villager.getX(), villager.getY() + 0.5, villager.getZ());
	}

	public static boolean walkToAPlace(Villager villager, WalkTarget walkTarget) {
		return !villager.isPassenger() && walkTarget.getTarget() instanceof BlockPosTracker && walkTarget.getCloseEnoughDist() <= 1;
	}

	public static @Nullable Path walkPath(Villager villager, PathNavigation navigation, WalkTarget walkTarget, BlockPos target,
		Supplier<@Nullable Path> vanilla) {
		if (!walkToAPlace(villager, walkTarget)) {
			return vanilla.get();
		}
		BlockPos block = blockToStandBeside(villager, target);
		if (block == null || sidesBlocked(villager, block)) {
			return vanilla.get();
		}
		Set<BlockPos> spots = spotsBeside(villager, block);
		Path beside = spots.isEmpty() ? null : navigation.createPath(spots, 0);
		if (beside != null && beside.canReach()) {
			memory(villager).villagermanners$sidesReached(block);
			return beside;
		}
		boolean searched = spots.isEmpty() || beside != null;
		if (searched && standsOn(villager, block)) {
			blocked(villager, block);
		}
		return vanilla.get();
	}

	public static boolean arrived(Villager villager, WalkTarget walkTarget, boolean reached) {
		if (!walkToAPlace(villager, walkTarget)) {
			return reached;
		}
		BlockPos target = walkTarget.getTarget().currentBlockPosition();
		if (reached) {
			BlockPos block = blockToStandBeside(villager, target);
			return block == null || (!standsOn(villager, block) && !slidesOff(villager)) || sidesBlocked(villager, block);
		}
		BlockPos node = node(villager);
		if (Math.abs(node.getX() - target.getX()) + Math.abs(node.getZ() - target.getZ()) != 1) {
			return false;
		}
		BlockPos block = blockToStandBeside(villager, target);
		if (block == null) {
			return false;
		}
		Set<BlockPos> spots = spotsBeside(villager, block);
		BlockPos edge = edgeHoldingUp(villager);
		if (!spots.contains(node) && (edge == null || !spots.contains(node.below()))) {
			return false;
		}
		return edge == null || !canSlideOff(villager, edge) || sidesBlocked(villager, block);
	}

	public static void walkStopped(Villager villager) {
		WalkTarget walkTarget = villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
		if (walkTarget == null) {
			return;
		}
		if (villager.getNavigation().isStuck()) {
			BlockPos block = blockToStepOff(villager, walkTarget);
			if (block != null) {
				blocked(villager, block);
			}
			return;
		}
		if (slidesOff(villager)) {
			slideToMiddle(villager);
		}
	}

	private static @Nullable BlockPos blockToStepOff(Villager villager, WalkTarget walkTarget) {
		if (!walkToAPlace(villager, walkTarget)) {
			return null;
		}
		BlockPos block = blockToStandBeside(villager, walkTarget.getTarget().currentBlockPosition());
		return block != null && standsOn(villager, block) && !sidesBlocked(villager, block) ? block : null;
	}

	public static boolean onOwnWorkstation(Villager villager) {
		if (villager.isPassenger()) {
			return false;
		}
		GlobalPos jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElse(null);
		if (jobSite == null || jobSite.dimension() != villager.level().dimension()) {
			return false;
		}
		BlockPos pos = jobSite.pos();
		return standsOn(villager, pos) && standsBeside(villager, villager.level().getBlockState(pos))
			&& !pos.equals(memory(villager).villagermanners$blockedWorkstation());
	}

	public static boolean sidesBlocked(Villager villager, BlockPos block) {
		WorkstationMemory memory = memory(villager);
		return block.equals(memory.villagermanners$blockedWorkstation())
			&& villager.level().getGameTime() - memory.villagermanners$blockedSince() < RETRY_TICKS;
	}

	private static void blocked(Villager villager, BlockPos block) {
		memory(villager).villagermanners$sidesBlocked(block.immutable(), villager.level().getGameTime());
	}

	private static WorkstationMemory memory(Villager villager) {
		return (WorkstationMemory) villager;
	}
}
