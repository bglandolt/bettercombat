package bettercombat.mod.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFatigue implements IMessage
{
	private int fatigue = 0;

	public PacketFatigue()
	{
		
	}

	public PacketFatigue( int f )
	{
		this.fatigue = f;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.fatigue = ByteBufUtils.readVarInt(buf, 1);
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		ByteBufUtils.writeVarInt(buf, this.fatigue, 1);
	}

	public static class Handler implements IMessageHandler<PacketFatigue, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketFatigue message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketFatigue message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;

			if ( message.fatigue > 0 )
			{
				player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 50, message.fatigue - 1, true, false));
			}
		}
	}
}