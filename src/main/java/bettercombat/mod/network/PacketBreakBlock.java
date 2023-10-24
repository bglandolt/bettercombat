package bettercombat.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketBreakBlock implements IMessage
{
	private int x;
	private int y;
	private int z;

	public PacketBreakBlock()
	{
	}

	public PacketBreakBlock( int x, int y, int z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.x = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 4) : ByteBufUtils.readVarInt(buf, 4);
		this.y = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 2) : ByteBufUtils.readVarInt(buf, 2);
		this.z = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 4) : ByteBufUtils.readVarInt(buf, 4);
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		if ( this.x < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.x, 4);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.x, 4);
		}
		
		if ( this.y < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.y, 2);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.y, 2);
		}
		
		if ( this.z < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.z, 4);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.z, 4);
		}
	}

	public static class Handler implements IMessageHandler<PacketBreakBlock, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketBreakBlock message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketBreakBlock message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			player.getEntityWorld().destroyBlock(new BlockPos(message.x,message.y,message.z), true);
			// player.getEntityWorld().setBlockToAir(new BlockPos(message.x,message.y,message.z));
		}
	}
}