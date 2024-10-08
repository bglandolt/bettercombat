package bettercombat.mod.server;

import bettercombat.mod.client.SoundHandler;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
	/* I&F */
    public static Class<?> partEntityClass = null;
    
	public void init(FMLInitializationEvent event)
	{
		
	}
	
	public void preInit( FMLPreInitializationEvent event )
	{
		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
        ConfigurationHandler.createInstLists();

		MinecraftForge.EVENT_BUS.register(new EventHandlers());
		PacketHandler.registerChannel(Reference.MOD_ID);
		PacketHandler.registerServerMessages();
		SoundHandler.registerSounds();
	}

	public void postInit( FMLPostInitializationEvent event ) throws Exception
	{
		ConfigurationHandler.postConfig();
		
		if ( partEntityClass == null )
		{
        	try
			{
				partEntityClass = Class.forName("net.ilexiconn.llibrary.server.entity.multipart.PartEntity");
			}
			catch ( ClassNotFoundException e )
			{

			}
		}
	}

	// PARTICLE
//	public abstract ParticleWizardry createParticle(ResourceLocation type, World world, double x, double y, double z);
//	
//	public abstract void registerParticles();
	
}