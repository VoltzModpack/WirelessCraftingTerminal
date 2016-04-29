package net.p455w0rd.wirelesscraftingterminal.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.core.sync.WCTPacket;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.INetworkInfo;
import net.p455w0rd.wirelesscraftingterminal.handlers.LocaleHandler;
import net.p455w0rd.wirelesscraftingterminal.items.ItemMagnet;

public class PacketSwitchMagnetMode extends WCTPacket {

	int magnetDamage;

	public PacketSwitchMagnetMode(final ByteBuf stream) {
		this.magnetDamage = stream.readInt();
	}

	// api
	public PacketSwitchMagnetMode(int itemDamage) {
		this.magnetDamage = itemDamage;
		final ByteBuf data = Unpooled.buffer();

		data.writeInt(this.getPacketID());
		data.writeInt(this.magnetDamage);
		this.configureWrite(data);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = RandomUtils.getMagnet(player.inventory);
		if (!(magnetItem.getItem() instanceof ItemMagnet)) {
			return;
		}
		if (magnetItem != null) {
			
			if (magnetItem.getItemDamage() == 2) {
				//magnetItem.setItemDamage(0);
				RandomUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short)0);
				player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode1.getLocal()));
			}
			else {
				RandomUtils.getWirelessTerm(player.inventory).getTagCompound().getTagList("MagnetSlot", 10).getCompoundTagAt(0).setShort("Damage", (short)(magnetItem.getItemDamage() + 1));
				if (magnetItem.getItemDamage() == 0) {
					player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode2.getLocal()));
				}
				else{
					player.addChatMessage(new ChatComponentText(LocaleHandler.MagnetMode3.getLocal()));
				}
			}
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WCTPacket packet, final EntityPlayer player) {
		ItemStack magnetItem = RandomUtils.getMagnet(player.inventory);
		if (!(magnetItem.getItem() instanceof ItemMagnet)) {
			return;
		}
		if (magnetItem != null) {
			((ItemMagnet) magnetItem.getItem()).switchMagnetMode(magnetItem, player);
			//magnetItem.setItemDamage(this.magnetDamage);
		}
	}
}