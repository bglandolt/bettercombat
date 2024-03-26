package bettercombat.mod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketHandler
{
	public static SimpleNetworkWrapper instance = null;

	public static void registerMessages( String channelName )
	{
		instance = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
		registerMessages();
	}

	@SideOnly( Side.CLIENT )
	public static void registerClientMessages()
	{
		instance.registerMessage(PacketParried.Handler.class, PacketParried.class, 1, Side.CLIENT);
		instance.registerMessage(PacketBleeding.Handler.class, PacketBleeding.class, 2, Side.CLIENT);
	}
	
	public static void registerMessages()
	{
		instance.registerMessage(PacketOffhandAttack.Handler.class, PacketOffhandAttack.class, 3, Side.SERVER);
		instance.registerMessage(PacketMainhandAttack.Handler.class, PacketMainhandAttack.class, 4, Side.SERVER);
		instance.registerMessage(PacketShieldBash.Handler.class, PacketShieldBash.class, 5, Side.SERVER);
		instance.registerMessage(PacketFastEquip.Handler.class, PacketFastEquip.class, 6, Side.SERVER);
		instance.registerMessage(PacketBreakBlock.Handler.class, PacketBreakBlock.class, 7, Side.SERVER);
		instance.registerMessage(PacketFatigue.Handler.class, PacketFatigue.class, 8, Side.SERVER);
		instance.registerMessage(PacketOnItemUse.Handler.class, PacketOnItemUse.class, 9, Side.SERVER);
		instance.registerMessage(PacketParrying.Handler.class, PacketParrying.class, 10, Side.SERVER);
		instance.registerMessage(PacketStopActiveHand.Handler.class, PacketStopActiveHand.class, 11, Side.SERVER);
	}
}