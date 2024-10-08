package bettercombat.mod.network;

import bettercombat.mod.network.client.PacketBleeding;
import bettercombat.mod.network.client.PacketDamageTilt;
import bettercombat.mod.network.client.PacketParried;
import bettercombat.mod.network.server.PacketBreakBlock;
import bettercombat.mod.network.server.PacketFastEquip;
import bettercombat.mod.network.server.PacketFatigue;
import bettercombat.mod.network.server.PacketMainhandAttack;
import bettercombat.mod.network.server.PacketOffhandAttack;
import bettercombat.mod.network.server.PacketOnItemUse;
import bettercombat.mod.network.server.PacketParrying;
import bettercombat.mod.network.server.PacketShieldBash;
import bettercombat.mod.network.server.PacketStopActiveHand;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
	public static SimpleNetworkWrapper instance = null;

	public static void registerChannel( String channelName )
	{
		instance = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
	}

	public static void registerClientMessages()
	{
		instance.registerMessage(PacketParried.Handler.class, PacketParried.class, 1, Side.CLIENT);
		instance.registerMessage(PacketBleeding.Handler.class, PacketBleeding.class, 2, Side.CLIENT);
		instance.registerMessage(PacketDamageTilt.Handler.class, PacketDamageTilt.class, 3, Side.CLIENT);
	}
	
	public static void registerServerMessages()
	{
		instance.registerMessage(PacketParried.Handler.class, PacketParried.class, 1, Side.SERVER);
		instance.registerMessage(PacketBleeding.Handler.class, PacketBleeding.class, 2, Side.SERVER);
		instance.registerMessage(PacketDamageTilt.Handler.class, PacketDamageTilt.class, 3, Side.SERVER);
		
		instance.registerMessage(PacketOffhandAttack.Handler.class, PacketOffhandAttack.class, 4, Side.SERVER);
		instance.registerMessage(PacketMainhandAttack.Handler.class, PacketMainhandAttack.class, 5, Side.SERVER);
		instance.registerMessage(PacketShieldBash.Handler.class, PacketShieldBash.class, 6, Side.SERVER);
		instance.registerMessage(PacketFastEquip.Handler.class, PacketFastEquip.class, 7, Side.SERVER);
		instance.registerMessage(PacketBreakBlock.Handler.class, PacketBreakBlock.class, 8, Side.SERVER);
		instance.registerMessage(PacketFatigue.Handler.class, PacketFatigue.class, 9, Side.SERVER);
		instance.registerMessage(PacketOnItemUse.Handler.class, PacketOnItemUse.class, 10, Side.SERVER);
		instance.registerMessage(PacketParrying.Handler.class, PacketParrying.class, 11, Side.SERVER);
		instance.registerMessage(PacketStopActiveHand.Handler.class, PacketStopActiveHand.class, 12, Side.SERVER);
	}
}