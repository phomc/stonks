/*
 * Copyright (c) 2023 PhoMC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
