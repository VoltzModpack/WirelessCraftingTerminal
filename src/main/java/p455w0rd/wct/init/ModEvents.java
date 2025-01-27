/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.init;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.wct.client.gui.GuiWCT;
import p455w0rd.wct.items.ItemMagnet;
import p455w0rd.wct.util.WCTUtils;
import p455w0rdslib.LibGlobals.Mods;

/**
 * @author p455w0rd
 *
 */
@EventBusSubscriber(modid = ModGlobals.MODID)
public class ModEvents {

	public static long CLIENT_TICKS = 0L;

	@SubscribeEvent
	public static void onItemRegistryReady(final RegistryEvent.Register<Item> event) {
		ModItems.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void tickEvent(final TickEvent.PlayerTickEvent e) {
		final EntityPlayer player = e.player;
		if (!(player instanceof EntityPlayerMP)) {
			return;
		}
		final InventoryPlayer playerInv = player.inventory;
		final Set<Pair<Boolean, Pair<Integer, ItemStack>>> terminals = WCTUtils.getCraftingTerminals(player);
		final int invSize = playerInv.getSizeInventory();
		if (invSize <= 0) {
			return;
		}
		for (final Pair<Boolean, Pair<Integer, ItemStack>> termPair : terminals) {
			final ItemStack wct = termPair.getRight().getRight();
			if (!ItemMagnet.getMagnetFromWCT(wct).isEmpty()) {
				((ItemMagnet) ItemMagnet.getMagnetFromWCT(wct).getItem()).doMagnet(player, wct, termPair.getLeft(), termPair.getRight().getLeft());
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onClientTick(final ClientTickEvent e) {
		if (e.phase == Phase.END) {
			if (CLIENT_TICKS > Long.MAX_VALUE - 1000) {
				CLIENT_TICKS = 0L;
			}
			CLIENT_TICKS++;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onKeyInput(final KeyInputEvent e) {
		WCTUtils.handleKeybind();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseEvent(final MouseEvent event) {
		WCTUtils.handleKeybind();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onGuiMouseInput(final MouseInputEvent.Post event) {
		if (event.getGui() instanceof GuiWCT) {
			final GuiWCT gui = (GuiWCT) event.getGui();
			if (GuiScreen.isShiftKeyDown() && Mouse.isButtonDown(0)) {
				final Slot s = gui.getSlotUnderMouse();
				if (s != null) {
					Minecraft.getMinecraft().playerController.windowClick(gui.inventorySlots.windowId, s.slotNumber, 0, ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onkeyTyped(final GuiScreenEvent.KeyboardInputEvent.Post e) {
		if (Mods.JEI.isLoaded() && Minecraft.getMinecraft().currentScreen instanceof GuiWCT) {
			final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
			if (searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
				final GuiWCT gui = (GuiWCT) Minecraft.getMinecraft().currentScreen;
				final String searchText = Integrations.jei().getSearchText();
				if (gui.getSearchField() != null) {
					gui.getRepo().setSearchString(searchText);
					gui.getRepo().updateView();
					gui.getSearchField().setText(searchText);
					GuiWCT.memoryText = searchText;
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPickup(final EntityItemPickupEvent e) {
		if (e.getEntityPlayer() != null && e.getEntityPlayer() instanceof EntityPlayerMP) {
			if (!WTApi.instance().getConfig().isOldInfinityMechanicEnabled() && e.getItem().getItem().getItem() == WTApi.instance().getBoosterCard()) {
				if (Mods.BAUBLES.isLoaded()) {
					for (final Pair<Integer, ItemStack> termPair : WTApi.instance().getBaublesUtility().getAllWTBaubles(e.getEntityPlayer())) {
						final ItemStack wirelessTerminal = termPair.getRight();
						if (!wirelessTerminal.isEmpty() && WTApi.instance().shouldConsumeBoosters(wirelessTerminal)) {
							e.setCanceled(true);
							final ItemStack boosters = e.getItem().getItem().copy();
							WTApi.instance().addInfinityBoosters(wirelessTerminal, boosters);
							WTApi.instance().getNetHandler().sendTo(WTApi.instance().getNetHandler().createInfinityEnergySyncPacket(WTApi.instance().getInfinityEnergy(wirelessTerminal), e.getEntityPlayer().getUniqueID(), true, termPair.getLeft()), (EntityPlayerMP) e.getEntityPlayer());
							e.getItem().setDead();
							return;
						}
					}
				}
				for (final Pair<Boolean, Pair<Integer, ItemStack>> termPair : WCTUtils.getCraftingTerminals(e.getEntityPlayer())) {
					final ItemStack wirelessTerminal = termPair.getRight().getRight();
					final boolean shouldConsume = WTApi.instance().shouldConsumeBoosters(wirelessTerminal);
					if (!wirelessTerminal.isEmpty() && shouldConsume) {
						e.setCanceled(true);
						final ItemStack boosters = e.getItem().getItem().copy();
						WTApi.instance().addInfinityBoosters(wirelessTerminal, boosters);
						WTApi.instance().getNetHandler().sendTo(WTApi.instance().getNetHandler().createInfinityEnergySyncPacket(WTApi.instance().getInfinityEnergy(wirelessTerminal), e.getEntityPlayer().getUniqueID(), true, termPair.getRight().getLeft()), (EntityPlayerMP) e.getEntityPlayer());
						e.getItem().setDead();
						return;
					}
				}
			}
		}
	}
}
