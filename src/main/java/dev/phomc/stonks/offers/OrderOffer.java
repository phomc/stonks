package dev.phomc.stonks.offers;

import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;

import dev.phomc.stonks.modules.ItemIdsConverter;
import dev.phomc.stonks.ui.menus.MarketMenu;
import dev.phomc.stonks.ui.menus.offers.OfferMenu;
import dev.phomc.stonks.utils.DisplayUtils;
import dev.phomc.stonks.utils.GeneralConverter;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class OrderOffer {
	public final UUID playerId;
	public final OfferType type;
	public final ItemStack item;
	public final int amount;
	public final double pricePerUnit;
	public final long expireOn;

	public ObjectId offerId;
	public int filled = 0, claimed = 0;

	public OrderOffer(UUID playerId, OfferType type, ItemStack item, int amount, double pricePerUnit, long expireOn) {
		this.playerId = playerId;
		this.type = type;
		this.item = item;
		this.amount = amount;
		this.pricePerUnit = pricePerUnit;
		this.expireOn = expireOn;
	}

	public OrderOffer(Document doc, ItemIdsConverter itemIds) {
		if (doc.containsKey("_id")) offerId = doc.getObjectId("_id");

		playerId = GeneralConverter.uuidFromDashless(doc.getString("player"));
		type = OfferType.valueOf(doc.getString("type"));
		item = itemIds.toItemStack(doc.getString("item"));
		amount = doc.getInteger("amount", 0);
		pricePerUnit = doc.getDouble("pricePerUnit").doubleValue();
		expireOn = doc.getLong("expireOn");
		filled = doc.getInteger("filled", 0);
		claimed = doc.getInteger("claimed", 0);
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > expireOn;
	}

	public boolean isFullyFilled() {
		return filled >= amount;
	}

	public GuiElement buildElement() {
		boolean full = filled >= amount;
		boolean expired = isExpired();

		return new GuiElementBuilder(item.getItem())
				.setName(Component.empty()
						.append(item.getHoverName())
						.append(Component.literal(full? " FULL" : (" (" + filled + "/" + amount + ")"))
								.withStyle(s -> s.withColor(full? ChatFormatting.GREEN : ChatFormatting.GRAY).withBold(full)))
						.append(Component.literal(expired? " EXPIRED" : "")
								.withStyle(s -> s.withColor(ChatFormatting.RED).withBold(true))))
				.addLoreLine(Component.empty().withStyle(ChatFormatting.DARK_GRAY).append(type.display))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.literal(amount + " item" + (amount != 1? "s" : "") + " @ " + DisplayUtils.PRICE_FORMATTER.format(pricePerUnit) + "/ea").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.literal(DisplayUtils.PRICE_FORMATTER.format(amount * pricePerUnit) + " total").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.empty())
				.addLoreLine(Component.empty().withStyle(ChatFormatting.GRAY)
						.append(Component.literal("Click ").withStyle(ChatFormatting.GOLD))
						.append("for details"))
				.setCallback((idx, click, action, gui) -> {
					if (gui instanceof MarketMenu mm) {
						OfferMenu menu = new OfferMenu(mm.market, mm, mm.getPlayer(), this);
						menu.open();
					}
				})
				.build();
	}

	public Document createNewOfferDocument(ItemIdsConverter itemIds) {
		return new Document("player", GeneralConverter.uuidToDashless(playerId))
				.append("type", type.toString())
				.append("item", itemIds.fromItemStack(item))
				.append("amount", amount)
				.append("pricePerUnit", pricePerUnit)
				.append("expireOn", expireOn)
				.append("filled", filled)
				.append("claimed", claimed);
	}
}
