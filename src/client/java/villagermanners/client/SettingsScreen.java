package villagermanners.client;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import villagermanners.MannersConfig;

public final class SettingsScreen extends OptionsSubScreen {
	private static final String KEY = "villager_manners.settings.";

	private final MannersConfig config = MannersConfig.get().copy();
	private @Nullable CycleButton<Boolean> children;

	public SettingsScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
	}

	@Override
	protected void addOptions() {
		if (this.list == null) {
			return;
		}
		MannersConfig c = this.config;
		this.list.addHeader(Component.translatable(KEY + "section"));
		this.list.addBig(this.toggle("workstations", c.workstations, v -> c.workstations = v));
		this.list.addBig(this.toggle("beds", c.beds, v -> c.beds = v));
		this.children = this.toggle("children_jump_on_beds", c.childrenJumpOnBeds, v -> c.childrenJumpOnBeds = v);
		this.list.addBig(this.children);
		this.list.addBig(this.toggle("bells", c.bells, v -> c.bells = v));
		this.list.addBig(this.toggle("chests", c.chests, v -> c.chests = v));
		this.list.addBig(this.toggle("crafting_tables_and_furnaces", c.craftingTablesAndFurnaces, v -> c.craftingTablesAndFurnaces = v));
		this.list.addBig(this.toggle("other_furniture", c.otherFurniture, v -> c.otherFurniture = v));
		this.list.addBig(this.toggle("stairs", c.stairs, v -> c.stairs = v));
		this.list.addBig(this.toggle("pressure_plates", c.pressurePlates, v -> c.pressurePlates = v));
		this.refresh();
	}

	private CycleButton<Boolean> toggle(String key, boolean value, Consumer<Boolean> setter) {
		Tooltip tooltip = Tooltip.create(Component.translatable(KEY + key + ".tooltip"));
		return CycleButton.onOffBuilder(value)
			.withTooltip(v -> tooltip)
			.create(Component.translatable(KEY + key), (button, v) -> {
				setter.accept(v);
				this.changed();
			});
	}

	private void changed() {
		MannersConfig.set(this.config);
		this.refresh();
	}

	private void refresh() {
		if (this.children != null) {
			this.children.active = this.config.beds;
		}
	}

	@Override
	public void removed() {
		super.removed();
		MannersConfig.save();
	}
}
