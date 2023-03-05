package dev.phomc.stonks.ui.menus;

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
