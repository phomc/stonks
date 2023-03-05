package dev.phomc.stonks.ui.menus.offers;

import java.util.concurrent.RejectedExecutionException;

import dev.phomc.stonks.markets.Market;
import dev.phomc.stonks.offers.OfferType;
import dev.phomc.stonks.offers.OrderOffer;
import dev.phomc.stonks.ui.menus.MarketMenu;
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

		setSlot(4, new GuiElement(offer.item, (idx, click, action, gui) -> {}));

		setSlot(20, new GuiElementBuilder(Items.RED_CONCRETE_POWDER)
				.setName(Component.literal("Cancel offer").withStyle(ChatFormatting.RED))
				.addLoreLine(Component.literal("Cancel this offer and receive").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.literal("refund").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to cancel").withStyle(ChatFormatting.GRAY)))
				.setCallback(this::cancelOffer));

		int availableToClaim = offer.filled - offer.claimed;
		String claimWhat = offer.type == OfferType.BUY? availableToClaim + " items" : (availableToClaim * offer.pricePerUnit) + " money";

		setSlot(offer.type == OfferType.BUY? 22 : 24, new GuiElementBuilder(Items.DIAMOND)
				.setName(Component.literal("Claim all").withStyle(ChatFormatting.GOLD))
				.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.YELLOW)
						.append(Component.literal("to claim " + claimWhat).withStyle(ChatFormatting.GRAY)))
				.setCallback(this::claim));

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

	public void cancelOffer() {
		close();
		player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelingOffer", "Canceling offer...").withStyle(ChatFormatting.GRAY));

		market.service.cancelOffer(offer.offerId).thenAccept(updatedOffer -> {
			int delta = updatedOffer.amount - updatedOffer.filled;
			double money = delta * updatedOffer.pricePerUnit;

			if (offer.type == OfferType.BUY) {
				market.currency.send(player.getUUID(), money).thenAccept($_ -> {
					market.getTemporaryData(player).offers.removeIf(v -> v.offerId.equals(offer.offerId));
					player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelledOffer", "Cancelled offer for %s!", offer.item.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
				}).exceptionally(t -> {
					throw new RejectedExecutionException(t);
				});
			}
			if (offer.type == OfferType.SELL) {
				player.getInventory().placeItemBackInInventory(offer.item.copyWithCount(delta));
				market.getTemporaryData(player).offers.removeIf(v -> v.offerId.equals(offer.offerId));
				player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelledOffer", "Cancelled offer for %s!", offer.item.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
			}
		}).exceptionally(t -> {
			player.sendSystemMessage(Component.translatableWithFallback("stonks.messages.cancelFailure", "Failed to cancel offer for %s", offer.item.getHoverName().getString()).withStyle(ChatFormatting.RED));
			t.printStackTrace();
			return null;
		});
	}

	public void claim() {
		close();
		player.sendSystemMessage(Component.literal("Claiming offer...").withStyle(ChatFormatting.GRAY));

		market.service.claimItems(offer.offerId).thenAccept(amount -> {
			if (offer.type == OfferType.BUY) {
				player.getInventory().placeItemBackInInventory(offer.item.copyWithCount(amount));
				player.sendSystemMessage(Component.literal("Claimed " + amount + "x " + offer.item.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
			}
			if (offer.type == OfferType.SELL) {
				market.currency.send(player.getUUID(), amount * offer.pricePerUnit).thenAccept($_ -> {
					player.sendSystemMessage(Component.literal("Claimed " + (amount * offer.pricePerUnit) + " money by selling " + amount + "x " + offer.item.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
				}).exceptionally(t -> {
					t.printStackTrace();
					player.sendSystemMessage(Component.literal("Failed to claim money").withStyle(ChatFormatting.RED));
					return null;
				});
			}
		}).exceptionally(t -> {
			t.printStackTrace();
			player.sendSystemMessage(Component.literal("Failed to claim " + (offer.type == OfferType.BUY? "items" : "money")).withStyle(ChatFormatting.RED));
			return null;
		});
	}
}
