package bettercombat.mod.network.server;

import bettercombat.mod.util.Helpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketShieldBash implements IMessage
{
	private Integer entityId;

	public PacketShieldBash()
	{

	}
	
	public PacketShieldBash( Integer entId ) //, int damage, int knockback )
	{
		this.entityId = entId;
	}

	public void fromBytes( ByteBuf buf )
	{
		if ( buf.readBoolean() )
		{
			this.entityId = ByteBufUtils.readVarInt(buf, 4);
		}
	}

	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean(this.entityId != null);
		
		if ( this.entityId != null )
		{
			ByteBufUtils.writeVarInt(buf, this.entityId, 4);
		}
	}

	public void handleClientSide( PacketShieldBash message, EntityPlayer player )
	{

	}

	public PacketShieldBash( int f, Integer parEntityId )
	{
		this.entityId = parEntityId;
	}

	public static class Handler implements IMessageHandler<PacketShieldBash, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketShieldBash message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketShieldBash message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			if ( message.entityId != null )
			{
				Entity target = player.getServerWorld().getEntityByID(message.entityId);
				
				if ( target != null )
				{
					Helpers.playerAttackVictim(player, target, false);
				}
			}

			player.stopActiveHand();

			Helpers.applySwingInteria(player);
		}
	}
}