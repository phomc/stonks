package dev.phomc.stonks.modules;

import net.minecraft.world.item.ItemStack;

/**
 * <p>Compare 2 items so Stonks can know which item to remove.</p>
 * @author nahkd
 *
 */
public interface ItemsComparator {
	boolean isSimilar(ItemStack a, ItemStack b);

	public static final ItemsComparator DEFAULT_COMPARATOR = new ItemsComparator() {
		@Override
		public boolean isSimilar(ItemStack a, ItemStack b) {
			return ItemStack.isSameItemSameTags(a, b);
		}
	};
}
