package bettercombat.mod.network.server;

import static bettercombat.mod.util.ConfigurationHandler.elanaiDodgeMainHandFeatherCost;
import static com.elenai.elenaidodge2.api.FeathersHelper.decreaseFeathers;

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

			if ( ConfigurationHandler.elanaiDodgeEnabled )
			{
				decreaseFeathers(player, elanaiDodgeMainHandFeatherCost);
			}

			if ( message.entityId != null )
			{
				Entity target = player.getServerWorld().getEntityByID(message.entityId);

				if ( target != null )
				{
					Helpers.playerAttackVictim(player, target, true);
				}
			}

			Helpers.applySwingInteria(player);
		}
	}
}