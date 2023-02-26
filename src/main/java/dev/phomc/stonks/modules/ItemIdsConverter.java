package dev.phomc.stonks.modules;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * <p>Convert item id from/to {@link ItemStack}. The {@link #toItemStack(String)} method will create a brand new
 * item.</p>
 *
 * <p>You can implement this interface to add support for your custom items.</p>
 * @author nahkd
 *
 */
public interface ItemIdsConverter {
	String fromItemStack(ItemStack stack);
	ItemStack toItemStack(String id);

	public static final ItemIdsConverter DEFAULT_CONVERTER = new ItemIdsConverter() {
		@Override
		public ItemStack toItemStack(String id) {
			return BuiltInRegistries.ITEM.get(new ResourceLocation(id)).getDefaultInstance();
		}
		
		@Override
		public String fromItemStack(ItemStack stack) {
			return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		}
	};
}
