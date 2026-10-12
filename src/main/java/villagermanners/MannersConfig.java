package villagermanners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class MannersConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static volatile MannersConfig current = new MannersConfig();

	public boolean workstations = true;
	public boolean beds = true;
	public boolean childrenJumpOnBeds = true;
	public boolean bells = true;
	public boolean chests = true;
	public boolean craftingTablesAndFurnaces = true;
	public boolean otherFurniture = true;
	public boolean stairs = true;
	public boolean pressurePlates = true;

	public static MannersConfig get() {
		return current;
	}

	public MannersConfig copy() {
		return GSON.fromJson(GSON.toJson(this), MannersConfig.class);
	}

	public static void set(MannersConfig config) {
		current = config.copy();
	}

	static void load() {
		Path path = file();
		MannersConfig loaded = null;
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				loaded = GSON.fromJson(reader, MannersConfig.class);
			} catch (IOException | JsonParseException e) {
				Path broken = path.resolveSibling(path.getFileName() + ".broken");
				VillagerManners.LOGGER.error("Could not read {}, using the defaults (the file is kept as {})", path, broken.getFileName(), e);
				try {
					Files.copy(path, broken, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ignored) {
				}
			}
		}
		current = loaded != null ? loaded : new MannersConfig();
		save();
	}

	public static void save() {
		Path path = file();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException | JsonIOException e) {
			VillagerManners.LOGGER.error("Could not save {}", path, e);
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve(VillagerManners.MOD_ID + ".json");
	}
}
