/*
 * *****************************************************************************
 * *************************************
 * Authors: SanAndreasP
 * Copyright: SanAndreasP
 * License: Creative Commons Attribution-NonCommercial-ShareAlike 4.0
 * International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package bettercombat.mod.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;

public class Reflections
{
	private static Method unpressKey;
	private static final String UPK_SRG = "func_74505_d";
	private static final String UPK_MCP = "unpressKey";
	
	private static Field ticksSinceLastSwing;
	private static final String TSLS_SRG = "field_184617_aD";
	private static final String TSLS_MCP = "ticksSinceLastSwing";
	
	private static Field activeItemStackUseCount;
	private static final String AISUC_SRG = "field_71072_f";
	private static final String AISUC_MCP = "activeItemStackUseCount";
	
	private static Method canBlockDamageSource;
	private static final String CBDS_SRG = "func_184583_d";
	private static final String CBDS_MCP = "canBlockDamageSource";
	
	private static Method damageShield;
	private static final String DMGS_SRG = "func_184590_k";
	private static final String DMGS_MCP = "damageShield";
	
	private static Method damageEntity;
	private static final String DMGE_SRG = "func_70665_d";
	private static final String DMGE_MCP = "damageEntity";
	
	private static Method markVelocityChanged;
	private static final String MVCG_SRG = "func_70018_K";
	private static final String MVCG_MCP = "markVelocityChanged";
	
	private static Method getDeathSound;
	private static final String GDSD_SRG = "func_184615_bR";
	private static final String GDSD_MCP = "getDeathSound";
	
	private static Method getSoundVolume;
	private static final String GSVL_SRG = "func_70599_aP";
	private static final String GSVL_MCP = "getSoundVolume";
	
	private static Field rightClickDelayTimer;
	private static final String RCDT_SRG = "field_71467_ac";
	private static final String RCDT_MCP = "rightClickDelayTimer";
	
	private static Field leftClickCounter;
	private static final String LCC_SRG = "field_71429_W";
	private static final String LCC_MCP = "leftClickCounter";
	
	private static Method getSoundPitch;
	private static final String GSPT_SRG = "func_70647_i";
	private static final String GSPT_MCP = "getSoundPitch";
	
	private static Method playHurtSound;
	private static final String PHSD_SRG = "func_184581_c";
	private static final String PHSD_MCP = "playHurtSound";
	
	public static void unpressKey( KeyBinding keyBinding )
	{
		try
		{
			if ( unpressKey == null )
			{
				unpressKey = KeyBinding.class.getDeclaredMethod(getName(UPK_SRG, UPK_MCP));
				unpressKey.setAccessible(true);
			}

			unpressKey.invoke(keyBinding);
		}
		catch (Exception ex)
		{
			throw new RuntimeException("unpressKey exception: " + ex);
		}
	}
	
	public static boolean canBlockDamageSource( EntityLivingBase living, DamageSource dmgSrc )
	{

		try
		{

			if ( canBlockDamageSource == null )
			{
				canBlockDamageSource = EntityLivingBase.class.getDeclaredMethod(getName(CBDS_SRG, CBDS_MCP), DamageSource.class);
				canBlockDamageSource.setAccessible(true);
			}

			return (Boolean) canBlockDamageSource.invoke(living, dmgSrc);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("canBlockDamageSource exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
			return false;
		}

	}


	public static void damageShield( EntityLivingBase living, float amount )
	{

		try
		{

			if ( damageShield == null )
			{
				damageShield = EntityLivingBase.class.getDeclaredMethod(getName(DMGS_SRG, DMGS_MCP), float.class);
				damageShield.setAccessible(true);
			}

			damageShield.invoke(living, amount);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("damageShield exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
		}

	}


	public static void damageEntity( EntityLivingBase living, DamageSource dmgSrc, float amount )
	{

		try
		{

			if ( damageEntity == null )
			{
				damageEntity = EntityLivingBase.class.getDeclaredMethod(getName(DMGE_SRG, DMGE_MCP), DamageSource.class, float.class);
				damageEntity.setAccessible(true);
			}

			damageEntity.invoke(living, dmgSrc, amount);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("damageEntity exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
		}

	}
	
	public static void setTicksSinceLastSwing( EntityLivingBase living, int ticks )
	{

		try
		{

			if ( ticksSinceLastSwing == null )
			{
				ticksSinceLastSwing = EntityLivingBase.class.getDeclaredField(getName(TSLS_SRG, TSLS_MCP));
				ticksSinceLastSwing.setAccessible(true);
			}

			ticksSinceLastSwing.setInt(living, ticks);
		}
		catch (Exception ex)
		{
			throw new RuntimeException("setTicksSinceLastSwing exception: " + ex);
		}

	}

	public static void activeItemStackUseCount( EntityLivingBase living, int ticks )
	{
		try
		{
			if ( activeItemStackUseCount == null )
			{
				activeItemStackUseCount = EntityLivingBase.class.getDeclaredField(getName(AISUC_SRG, AISUC_MCP));
				activeItemStackUseCount.setAccessible(true);
			}

			activeItemStackUseCount.setInt(living, ticks);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static void markVelocityChanged( Entity entity )
	{

		try
		{

			if ( markVelocityChanged == null )
			{
				markVelocityChanged = Entity.class.getDeclaredMethod(getName(MVCG_SRG, MVCG_MCP));
				markVelocityChanged.setAccessible(true);
			}

			markVelocityChanged.invoke(entity);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("markVelocityChanged exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
		}

	}


	public static SoundEvent getDeathSound( EntityLivingBase living )
	{

		try
		{

			if ( getDeathSound == null )
			{
				getDeathSound = EntityLivingBase.class.getDeclaredMethod(getName(GDSD_SRG, GDSD_MCP));
				getDeathSound.setAccessible(true);
			}

			return (SoundEvent) getDeathSound.invoke(living);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("getDeathSound exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}

	}


	public static float getSoundVolume( EntityLivingBase living )
	{

		try
		{

			if ( getSoundVolume == null )
			{
				getSoundVolume = EntityLivingBase.class.getDeclaredMethod(getName(GSVL_SRG, GSVL_MCP));
				getSoundVolume.setAccessible(true);
			}

			return (float) getSoundVolume.invoke(living);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("getSoundVolume exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
			return 0.0F;
		}

	}
	
	
	public static int getRightClickDelayTimer( Minecraft minecraft )
	{
		try
		{
			if ( rightClickDelayTimer == null )
			{
				rightClickDelayTimer = Minecraft.class.getDeclaredField(getName(RCDT_SRG, RCDT_MCP));
				rightClickDelayTimer.setAccessible(true);
			}

			return rightClickDelayTimer.getInt(minecraft);
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static int getLeftClickCounter( Minecraft minecraft )
	{
		try
		{
			if ( leftClickCounter == null )
			{
				leftClickCounter = Minecraft.class.getDeclaredField(getName(LCC_SRG, LCC_MCP));
				leftClickCounter.setAccessible(true);
			}

			return leftClickCounter.getInt(minecraft);
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}


	public static float getSoundPitch( EntityLivingBase living )
	{

		try
		{

			if ( getSoundPitch == null )
			{
				getSoundPitch = EntityLivingBase.class.getDeclaredMethod(getName(GSPT_SRG, GSPT_MCP));
				getSoundPitch.setAccessible(true);
			}

			return (float) getSoundPitch.invoke(living);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("getSoundPitch exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
			return 0.0F;
		}

	}


	public static void playHurtSound( EntityLivingBase living, DamageSource dmgSrc )
	{

		try
		{

			if ( playHurtSound == null )
			{
				playHurtSound = EntityLivingBase.class.getDeclaredMethod(getName(PHSD_SRG, PHSD_MCP), DamageSource.class);
				playHurtSound.setAccessible(true);
			}

			playHurtSound.invoke(living, dmgSrc);
		}
		catch (NoSuchMethodException ex)
		{
			throw new RuntimeException("playHurtSound exception: " + ex);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			ex.printStackTrace();
		}

	}


	private static String getName( String srgName, String mcpName )
	{
		return Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment")) ? mcpName : srgName;
	}
}
