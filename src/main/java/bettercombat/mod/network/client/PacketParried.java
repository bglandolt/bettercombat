package bettercombat.mod.network.client;

import bettercombat.mod.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/* Animate parried an attack */
public class PacketParried implements IMessage
{
	private boolean parried;
	
	public PacketParried()
	{

	}
	
	public PacketParried( boolean b )
	{
		this.parried = b;
	}

	public void fromBytes( ByteBuf buf )
	{
		this.parried = buf.readBoolean();
	}

	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean(this.parried);
	}

	public void handleClientSide( PacketParried message, EntityPlayer player )
	{

	}

    public static class Handler implements IMessageHandler<PacketParried, IMessage>
    {
    	@Override
		public IMessage onMessage( final PacketParried message, final MessageContext ctx )
		{
    		/* CLIENT */
    		Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketParried message, MessageContext ctx )
		{
            EntityPlayerSP player = Minecraft.getMinecraft().player;

            if ( player != null )
            {
                player.swingArm(EnumHand.MAIN_HAND);

                ClientProxy.AH_INSTANCE.parryingAnimationEnergy -= 5;
                
                if ( ClientProxy.AH_INSTANCE.parryingAnimationEnergy < 0 )
                {
                    ClientProxy.AH_INSTANCE.parryingAnimationEnergy = 0;
                }

                ClientProxy.AH_INSTANCE.parriedEnergy = 10;
                
//                if ( message.parried )
//                {
//                	player.maxHurtTime = 10;
//                	player.hurtTime = 10;
//                	player.attackedAtYaw = player.rotationYaw; // TODO
//                }
            }
        }
    }
}