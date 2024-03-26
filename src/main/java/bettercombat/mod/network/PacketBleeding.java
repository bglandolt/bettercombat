package bettercombat.mod.network;

import bettercombat.mod.client.ParticleBlood;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketBleeding implements IMessage
{
	private Integer entityId;

	public PacketBleeding()
	{

	}
	
	public PacketBleeding( Integer entId ) //, int damage, int knockback )
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

	public void handleClientSide( PacketParried message, EntityPlayer player )
	{
	}
	
    public static class Handler implements IMessageHandler<PacketBleeding, IMessage>
    {
        @Override
        public IMessage onMessage(PacketBleeding message, MessageContext ctx)
        {
			Entity target = Minecraft.getMinecraft().player.world.getEntityByID(message.entityId);

			if ( target != null )
			{
				int i = 0;
				
				while ( i++ < 100 ) Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlood.Factory().createParticle(0, target.world, target.posX, target.posY, target.posZ, 0, 0, 0));
			}
			
			return null;
        }
    }
}