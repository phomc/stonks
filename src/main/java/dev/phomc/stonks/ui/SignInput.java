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

package dev.phomc.stonks.ui;

import java.util.function.Consumer;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.server.level.ServerPlayer;

public class SignInput extends SignGui {
	public final int inputLine;
	public final Consumer<String> inputConsumer;

	public SignInput(ServerPlayer player, int inputLine, Consumer<String> inputConsumer) {
		super(player);
		this.inputLine = inputLine;
		this.inputConsumer = inputConsumer;
	}

	public SignInput(ServerPlayer player, Consumer<String> inputConsumer) {
		this(player, 0, inputConsumer);
	}

	@Override
	public void onClose() {
		inputConsumer.accept(getLine(inputLine).getString());
	}
}
