package bettercombat.mod.network;

import bettercombat.mod.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketParried implements IMessage
{
	public PacketParried()
	{

	}

	public void fromBytes( ByteBuf buf )
	{
	}

	public void toBytes( ByteBuf buf )
	{
	}

	public void handleClientSide( PacketParried message, EntityPlayer player )
	{
	}
	
    public static class Handler implements IMessageHandler<PacketParried, IMessage>
    {
        @Override
        public IMessage onMessage(PacketParried message, MessageContext ctx)
        {
            Minecraft.getMinecraft().player.swingArm(EnumHand.MAIN_HAND);

			ClientProxy.AH_INSTANCE.parryingAnimationTimer -= 5;
			
			if ( ClientProxy.AH_INSTANCE.parryingAnimationTimer < 0 )
			{
				ClientProxy.AH_INSTANCE.parryingAnimationTimer = 0;
			}
			
			ClientProxy.AH_INSTANCE.parriedTimer = 10;
			
            return null;
        }
    }
}