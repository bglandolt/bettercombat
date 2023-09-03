package bettercombat.mod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
	public static SimpleNetworkWrapper instance = null;

	public static void registerMessages( String channelName )
	{
		instance = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
		registerMessages();
	}

	public static void registerMessages()
	{
		instance.registerMessage(PacketParried.Handler.class, PacketParried.class, 1, Side.CLIENT);
		instance.registerMessage(PacketOffhandAttack.Handler.class, PacketOffhandAttack.class, 2, Side.SERVER);
		instance.registerMessage(PacketMainhandAttack.Handler.class, PacketMainhandAttack.class, 3, Side.SERVER);
		instance.registerMessage(PacketShieldBash.Handler.class, PacketShieldBash.class, 4, Side.SERVER);
		instance.registerMessage(PacketFastEquip.Handler.class, PacketFastEquip.class, 5, Side.SERVER);
		instance.registerMessage(PacketBreakBlock.Handler.class, PacketBreakBlock.class, 6, Side.SERVER);
		instance.registerMessage(PacketFatigue.Handler.class, PacketFatigue.class, 7, Side.SERVER);
		instance.registerMessage(PacketOnItemUse.Handler.class, PacketOnItemUse.class, 8, Side.SERVER);
		instance.registerMessage(PacketParrying.Handler.class, PacketParrying.class, 9, Side.SERVER);
		instance.registerMessage(PacketStopActiveHand.Handler.class, PacketStopActiveHand.class, 10, Side.SERVER);
	}

//	@SideOnly( Side.CLIENT )
//	public static void registerClientMessages()
//	{
//		instance.registerMessage(PacketOffhandCooldown.ClientHandler.class, PacketOffhandCooldown.class, 4, Side.CLIENT);
//	}
}