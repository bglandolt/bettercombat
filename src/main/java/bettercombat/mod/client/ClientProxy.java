package bettercombat.mod.client;

import org.lwjgl.input.Keyboard;

import bettercombat.mod.capability.StorageOffHandAttack;
import bettercombat.mod.client.handler.EventHandlersClient;
import bettercombat.mod.client.particle.EntitySweepAttack2FX;
import bettercombat.mod.combat.DefaultImplOffHandAttack;
import bettercombat.mod.combat.IOffHandAttack;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.util.CommonProxy;
import bettercombat.mod.util.Reference;
import bettercombat.mod.util.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	public static EventHandlersClient EHC_INSTANCE;
	public static AnimationHandler AH_INSTANCE;

	public static KeyBinding fastEquip = new KeyBinding("key.fastEquip.desc", Keyboard.KEY_X, "key.categories.misc");

	@Override
	public void preInit( FMLPreInitializationEvent event )
	{
		super.preInit(event);
		ClientRegistry.registerKeyBinding(fastEquip);
		PacketHandler.registerMessages(Reference.MOD_ID);
		CapabilityManager.INSTANCE.register(IOffHandAttack.class, new StorageOffHandAttack(), DefaultImplOffHandAttack::new);
		SoundHandler.registerSounds();
	}
	
	@Override
	public void postInit( FMLPostInitializationEvent event )
	{
		super.postInit(event);
		MinecraftForge.EVENT_BUS.register(EHC_INSTANCE = new EventHandlersClient());
		MinecraftForge.EVENT_BUS.register(AH_INSTANCE = new AnimationHandler());
	}
	
	@Override
	public void init( FMLInitializationEvent event )
	{
		super.init(event);
	}
		
	@Override
    public void spawnSweep(EntityPlayer player)
	{
        double x = -MathHelper.sin(player.rotationYaw * 0.017453292F);
        double z = MathHelper.cos(player.rotationYaw * 0.017453292F);
        Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySweepAttack2FX(Minecraft.getMinecraft().getTextureManager(), player.world, player.posX + x, player.posY + player.height * 0.5D, player.posZ + z, 0.0D));
    }
	
	// PARTICLE
//	private static final Map<ResourceLocation, IWizardryParticleFactory> factories = new HashMap<>();
//
//	public static void addParticleFactory(ResourceLocation name, IWizardryParticleFactory factory)
//	{
//		factories.put(name, factory);
//	}
//	
//	@Override
//	public void registerParticles()
//	{
//		ParticleWizardry.registerParticle(ParticleType.SWEEP, ParticleSweep::new);
//	}
//	
//	@Override
//	public ParticleWizardry createParticle(ResourceLocation type, World world, double x, double y, double z)
//	{
//		IWizardryParticleFactory factory = factories.get(type);
//		
//		if ( factory == null )
//		{
//			return null;
//		}
//		
//		return factory.createParticle(world, x, y, z);
//	}
}
