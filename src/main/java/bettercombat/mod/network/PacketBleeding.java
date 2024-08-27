package bettercombat.mod.network;

import bettercombat.mod.client.ParticleBlood;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketBleeding implements IMessage
{
	private Integer entityId;
	private Integer numberOfParticles;

	//effect @e bettercombat:bleeding 100 1
	public PacketBleeding()
	{

	}
	
	public PacketBleeding( Integer entId, Integer numberOfParticles )
	{
		this.entityId = entId;
		this.numberOfParticles = numberOfParticles;
	}

	public void fromBytes( ByteBuf buf )
	{
		this.entityId = ByteBufUtils.readVarInt(buf, 4);
		this.numberOfParticles = ByteBufUtils.readVarInt(buf, 1);
	}

	public void toBytes( ByteBuf buf )
	{
		ByteBufUtils.writeVarInt(buf, this.entityId, 4);
		ByteBufUtils.writeVarInt(buf, this.numberOfParticles, 1);
	}

	public void handleClientSide( PacketBleeding message, EntityPlayer player )
	{
	}
	
    public static class Handler implements IMessageHandler<PacketBleeding, IMessage>
    {
    	@Override
		public IMessage onMessage( final PacketBleeding message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketBleeding message, MessageContext ctx )
		{
			Entity target = Minecraft.getMinecraft().player.world.getEntityByID(message.entityId);

			if ( target != null )
			{				
				while ( --message.numberOfParticles >= 0 )
				{
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlood.Factory().createParticle(0, target.world, target.posX, target.posY, target.posZ, 0, 0, 0));
				}
			}
        }
    }
}