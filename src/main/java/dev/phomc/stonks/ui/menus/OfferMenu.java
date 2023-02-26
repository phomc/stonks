package dev.phomc.stonks.ui.menus;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class OfferMenu extends MarketMenu {
	private OrderOffer offer;

	public OfferMenu(Market market, MarketMenu previousMenu, ServerPlayer player, OrderOffer offer) {
		super(market, previousMenu, MenuType.GENERIC_9x4, player, false);
		setTitle(Component.translatableWithFallback("stonks.menu.offer.title", "Market > My Offers > %s", offer.item.getHoverName().getString()));
		this.offer = offer;

		for (int i = 0; i < 9; i++) setSlot(i, CommonElements.EMPTY);
		if (previousMenu != null) setSlot(1, CommonElements.PREV_PAGE);
		setSlot(4, new GuiElement(offer.item, (idx, click, action, gui) -> {}));

		setSlot(offer.type == OfferType.BUY? 20 : 22, new GuiElementBuilder(Items.RED_CONCRETE_POWDER)
				.setName(Component.literal("Cancel offer").withStyle(ChatFormatting.RED))
				.addLoreLine(Component.literal("Cancel this offer and receive").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.literal("pending money/items").withStyle(ChatFormatting.GRAY))
				.setCallback(this::cancelOffer));

		if (offer.type == OfferType.BUY) {
			setSlot(24, new GuiElementBuilder(Items.EMERALD)
					.setName(Component.literal("Quick flip").withStyle(ChatFormatting.GREEN))
					.addLoreLine(Component.literal("Quickly flip this order to sell").withStyle(ChatFormatting.GRAY))
					.addLoreLine(Component.literal("items for profit").withStyle(ChatFormatting.GRAY))
					.addLoreLine(Component.empty())
					.addLoreLine(Component.literal("Need ").withStyle(ChatFormatting.GRAY)
							.append(Component.literal("Supporter").withStyle(ChatFormatting.GREEN))
							.append(" rank to use!")));
		}
	}

	private void cancelOffer() {
		close();
		player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelingOffer", "Canceling offer...").withStyle(ChatFormatting.GRAY));

		market.service.cancelOffer(offer.offerId).thenRun(() -> {
			// TODO: refund
			player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelledOffer", "Cancelled offer for %s!", offer.item.getHoverName().getString()));
		}).exceptionally(t -> {
			player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelFailure", "Failed to cancel offer for %s", offer.item.getHoverName().getString()));
			t.printStackTrace();
			return null;
		});
	}
}
