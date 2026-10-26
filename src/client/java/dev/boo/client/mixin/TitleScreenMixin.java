package dev.boo.client.mixin;

import dev.boo.client.GhostRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	@Unique
	private long boo$openedAt = 0L;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void boo$drawGhost(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (boo$openedAt == 0L) boo$openedAt = System.nanoTime();
		double seconds = (System.nanoTime() - boo$openedAt) / 1_000_000_000.0;
		GhostRenderer.draw(g, this.width, this.height, mouseX, mouseY, seconds);
	}
}
