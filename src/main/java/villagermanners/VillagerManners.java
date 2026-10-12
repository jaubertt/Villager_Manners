package villagermanners;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerManners implements ModInitializer {
	public static final String MOD_ID = "villager_manners";
	public static final Logger LOGGER = LoggerFactory.getLogger("Villager Manners");

	@Override
	public void onInitialize() {
		MannersConfig.load();
	}
}
