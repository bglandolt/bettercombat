package bettercombat.mod.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStopActiveHand implements IMessage
{
	public PacketStopActiveHand()
	{

	}
	
	public void fromBytes( ByteBuf buf )
	{
		
	}

	public void toBytes( ByteBuf buf )
	{
		
	}

	public void handleClientSide( PacketStopActiveHand message, EntityPlayer player )
	{

	}

	public static class Handler implements IMessageHandler<PacketStopActiveHand, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketStopActiveHand message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketStopActiveHand message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.stopActiveHand();
		}
	}
}