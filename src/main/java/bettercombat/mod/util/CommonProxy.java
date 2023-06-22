package bettercombat.mod.util;

import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
    public void spawnSweep(EntityPlayer player)
    {
    	
    }

	public void postInit( FMLPostInitializationEvent event )
	{
		ConfigurationHandler.postConfig();
	}

	public void init(FMLInitializationEvent event)
	{
		
	}
	
	public void preInit( FMLPreInitializationEvent event )
	{
		MinecraftForge.EVENT_BUS.register(new EventHandlers());
		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
        ConfigurationHandler.createInstLists();
        
		PacketHandler.registerMessages(Reference.MOD_ID);
		SoundHandler.registerSounds();

//		registerParticles();
	}

	// PARTICLE
//	public abstract ParticleWizardry createParticle(ResourceLocation type, World world, double x, double y, double z);
//	
//	public abstract void registerParticles();
//	
}