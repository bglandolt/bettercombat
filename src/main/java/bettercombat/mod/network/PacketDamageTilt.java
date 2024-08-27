package bettercombat.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDamageTilt implements IMessage
{
	private float attackedAtYaw;
	
    public PacketDamageTilt()
    {

    }
    
    public PacketDamageTilt(float f)
    {
        this.attackedAtYaw = f;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.attackedAtYaw = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeFloat(this.attackedAtYaw);
    }

    public static class Handler implements IMessageHandler<PacketDamageTilt, IMessage>
    {
    	@Override
		public IMessage onMessage( final PacketDamageTilt message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketDamageTilt message, MessageContext ctx )
		{
            EntityPlayer player = Minecraft.getMinecraft().player;

            if ( player != null )
            {
            	player.attackedAtYaw = message.attackedAtYaw;
            	player.maxHurtTime = 10;
        		player.hurtTime = player.maxHurtTime;
            }
        }
    }
    
    public static float getDamageTilt(EntityLivingBase player, EntityLivingBase source)
    {
    	double deltaX = player.posX - source.posX;
        double deltaZ = player.posZ - source.posZ;

        // Compute the angle (in degrees) relative to the player's yaw
        float attackYaw = (float) (Math.atan2(deltaZ, deltaX) * (180D / Math.PI)) - player.rotationYaw;

        // Adjust the attackedAtYaw to create an upward knock effect
        return player.attackedAtYaw = attackYaw; // ConfigurationHandler.inverseDamageTiltAngle ? -attackYaw : attackYaw;
    }
}