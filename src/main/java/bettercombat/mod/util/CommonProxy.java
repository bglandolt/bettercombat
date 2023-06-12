package bettercombat.mod.util;

import bettercombat.mod.handler.EventHandlers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public abstract class CommonProxy
{
    public abstract void spawnSweep(EntityPlayer player);

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
//		registerParticles();
	}

	// PARTICLE
//	public abstract ParticleWizardry createParticle(ResourceLocation type, World world, double x, double y, double z);
//	
//	public abstract void registerParticles();
//	
}