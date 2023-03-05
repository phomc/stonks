package dev.phomc.stonks.modules;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * <p>Compare 2 items so Stonks can know which item to remove.</p>
 * @author nahkd
 *
 */
public interface ItemsComparator {
	boolean isSimilar(ItemStack a, ItemStack b);

	default int countInContainers(ItemStack item, Container container) {
		int count = 0;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack is = container.getItem(i);
			if (isSimilar(item, is)) count += is.getCount();
		}

		return count;
	}

	default int removeInContainers(ItemStack item, Container container, int count) {
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack is = container.getItem(i);
			if (!isSimilar(item, is)) continue;

			int toRemove = Math.min(is.getCount(), count);
			count -= toRemove;
			is.setCount(is.getCount() - toRemove);

			if (is.getCount() <= 0) is = Items.AIR.getDefaultInstance();
			container.setItem(i, is);
		}

		return count;
	}

	public static final ItemsComparator DEFAULT_COMPARATOR = new ItemsComparator() {
		@Override
		public boolean isSimilar(ItemStack a, ItemStack b) {
			return ItemStack.isSameItemSameTags(a, b);
		}
	};
}
