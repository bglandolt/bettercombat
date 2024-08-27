package bettercombat.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketParrying implements IMessage
{
	private boolean parrying;

	public PacketParrying()
	{

	}
	
	public PacketParrying( boolean b )
	{
		this.parrying = b;
	}

	public void fromBytes( ByteBuf buf )
	{
		this.parrying = buf.readBoolean();
	}

	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean(this.parrying);
	}

	public void handleClientSide( PacketParrying message, EntityPlayer player )
	{

	}
	
	public static class Handler implements IMessageHandler<PacketParrying, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketParrying message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketParrying message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
						
			if ( message.parrying )
			{
				player.setActiveHand(EnumHand.MAIN_HAND);
				player.getEntityData().setBoolean("isParrying", true);
			}
			else
			{
				player.stopActiveHand();
				player.getEntityData().setBoolean("isParrying", false);
			}
		}
	}
}