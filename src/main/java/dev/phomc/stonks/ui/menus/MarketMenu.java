package dev.phomc.stonks.ui.menus;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.ui.CommonElements;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

public abstract class MarketMenu extends SimpleGui {
	public final Market market;
	public final MarketMenu previousMenu;

	public MarketMenu(Market market, MarketMenu previousMenu, MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
		super(type, player, manipulatePlayerSlots);
		this.market = market;
		this.previousMenu = previousMenu;

		for (int i = 0; i < 9; i++) setSlot(i, CommonElements.EMPTY);
		if (previousMenu != null) setSlot(1, CommonElements.PREV_PAGE);
	}
}
