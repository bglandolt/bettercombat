package bettercombat.mod.network;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Helpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMainhandAttack implements IMessage
{
	private Integer entityId;

	public PacketMainhandAttack()
	{
	}

	public PacketMainhandAttack( Integer parEntityId )
	{
		this.entityId = parEntityId;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		if ( buf.readBoolean() )
		{
			this.entityId = ByteBufUtils.readVarInt(buf, 4);
		}
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean(this.entityId != null);
		
		if ( this.entityId != null )
		{
			ByteBufUtils.writeVarInt(buf, this.entityId, 4);
		}
	}

	public static class Handler implements IMessageHandler<PacketMainhandAttack, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketMainhandAttack message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketMainhandAttack message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;

			if ( message.entityId != null )
			{
				Entity target = player.world.getEntityByID(message.entityId);
				
				if ( target != null )
				{
					Helpers.playerAttackVictim(player, target, true);
				}
			}
			
			if ( ConfigurationHandler.inertiaOnAttack != 1.0F )
			{
				if ( player.onGround )
				{
					player.motionX *= ConfigurationHandler.inertiaOnAttack;
					player.motionZ *= ConfigurationHandler.inertiaOnAttack;

					player.velocityChanged = true;
				}
			}
			
//			if ( ConfigurationHandler.momentumOnAttack != 0.0F )
//			{
//				if ( player.onGround )
//				{
//					player.motionY += 0.001D;
//				}
//		
//				player.motionX -= ConfigurationHandler.momentumOnAttack*MathHelper.sin(player.rotationYaw * 0.017453292F);
//				player.motionZ += ConfigurationHandler.momentumOnAttack*MathHelper.cos(player.rotationYaw * 0.017453292F);
//				
//				player.velocityChanged = true;
//			}
		}
	}
}