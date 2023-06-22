package bettercombat.mod.util;

import javax.annotation.Nullable;

import bettercombat.mod.client.BetterCombatHand;
import bettercombat.mod.util.ConfigurationHandler.Animation;
import bettercombat.mod.util.ConfigurationHandler.SoundType;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundHandler
{
	/* --------------------------------- */
	/*                BASH               */
	/* --------------------------------- */
	
	/*               METAL               */

	public static SoundEvent BASH_METAL_SHIELD_0;
	public static SoundEvent BASH_METAL_SHIELD_1;
	public static SoundEvent BASH_METAL_SHIELD_2;
	public static SoundEvent BASH_METAL_SHIELD_3;
	
	/*                WOOD               */

	public static SoundEvent BASH_WOOD_SHIELD_0;
	public static SoundEvent BASH_WOOD_SHIELD_1;
	public static SoundEvent BASH_WOOD_SHIELD_2;
	public static SoundEvent BASH_WOOD_SHIELD_3;

	/* --------------------------------- */
	/*               BLOCK               */
	/* --------------------------------- */

	/*               HEAVY               */

	public static SoundEvent BLOCK_METAL_HEAVY_0;
	public static SoundEvent BLOCK_METAL_HEAVY_1;
	public static SoundEvent BLOCK_METAL_HEAVY_2;
	public static SoundEvent BLOCK_METAL_HEAVY_3;

	/*               LIGHT               */

	public static SoundEvent BLOCK_METAL_LIGHT_0;
	public static SoundEvent BLOCK_METAL_LIGHT_1;
	public static SoundEvent BLOCK_METAL_LIGHT_2;
	public static SoundEvent BLOCK_METAL_LIGHT_3;

	/* --------------------------------- */
	/*              IMPACT               */
	/* --------------------------------- */
	
	/*               RANGED              */

	public static SoundEvent IMPACT_RANGED_0;

	/*               ARMOR               */

	public static SoundEvent IMPACT_ARMOR_BLADE_0;
	public static SoundEvent IMPACT_ARMOR_BLADE_1;
	public static SoundEvent IMPACT_ARMOR_METAL_0;
	
	/*                STAB               */

	public static SoundEvent IMPACT_STAB_0;

	/*                AXE                */

	public static SoundEvent IMPACT_METAL_AXE_0;
	public static SoundEvent IMPACT_METAL_AXE_1;
	public static SoundEvent IMPACT_METAL_AXE_2;
	public static SoundEvent IMPACT_METAL_AXE_3;
	public static SoundEvent IMPACT_METAL_AXE_4;
	public static SoundEvent IMPACT_METAL_AXE_5;
	public static SoundEvent IMPACT_METAL_AXE_6;
	public static SoundEvent IMPACT_METAL_AXE_7;
	public static SoundEvent IMPACT_METAL_AXE_8;
	public static SoundEvent IMPACT_METAL_AXE_9;
	public static SoundEvent IMPACT_METAL_AXE_10;

	/*                BLADE              */

	public static SoundEvent IMPACT_METAL_BLADE_0;
	public static SoundEvent IMPACT_METAL_BLADE_1;
	
	/*                BLUNT              */

	public static SoundEvent IMPACT_METAL_BLUNT_0;
	public static SoundEvent IMPACT_METAL_BLUNT_1;
	public static SoundEvent IMPACT_METAL_BLUNT_2;
	public static SoundEvent IMPACT_METAL_BLUNT_3;
	public static SoundEvent IMPACT_METAL_BLUNT_4;
	public static SoundEvent IMPACT_METAL_BLUNT_5;
	public static SoundEvent IMPACT_METAL_BLUNT_6;
	public static SoundEvent IMPACT_METAL_BLUNT_7;
	public static SoundEvent IMPACT_METAL_BLUNT_8;
	
	/*               PUNCH               */

	public static SoundEvent IMPACT_PUNCH_0;
	public static SoundEvent IMPACT_PUNCH_1;
	public static SoundEvent IMPACT_PUNCH_2;
	public static SoundEvent IMPACT_PUNCH_3;
	public static SoundEvent IMPACT_PUNCH_4;
	public static SoundEvent IMPACT_PUNCH_5;
	
	/* --------------------------------- */
	/*                SWING              */
	/* --------------------------------- */
	
	/*                 2H                */

	public static SoundEvent SWING_2H_LEFT_0;
	public static SoundEvent SWING_2H_LEFT_1;
	public static SoundEvent SWING_2H_LEFT_2;
	public static SoundEvent SWING_2H_RIGHT_0;
	public static SoundEvent SWING_2H_RIGHT_1;
	public static SoundEvent SWING_2H_RIGHT_2;
	
	/*                AXE                */

	public static SoundEvent SWING_METAL_AXE_LEFT_0;
	public static SoundEvent SWING_METAL_AXE_LEFT_1;
	public static SoundEvent SWING_METAL_AXE_LEFT_2;
	public static SoundEvent SWING_METAL_AXE_LEFT_3;
	public static SoundEvent SWING_METAL_AXE_RIGHT_0;
	public static SoundEvent SWING_METAL_AXE_RIGHT_1;
	public static SoundEvent SWING_METAL_AXE_RIGHT_2;
	public static SoundEvent SWING_METAL_AXE_RIGHT_3;

	/*               BLADE                */

	public static SoundEvent SWING_METAL_BLADE_LEFT_0;
	public static SoundEvent SWING_METAL_BLADE_LEFT_1;
	public static SoundEvent SWING_METAL_BLADE_LEFT_2;
	public static SoundEvent SWING_METAL_BLADE_LEFT_3;
	public static SoundEvent SWING_METAL_BLADE_LEFT_4;
	public static SoundEvent SWING_METAL_BLADE_LEFT_5;
	public static SoundEvent SWING_METAL_BLADE_LEFT_6;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_0;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_1;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_2;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_3;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_4;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_5;
	public static SoundEvent SWING_METAL_BLADE_RIGHT_6;

	/*               BLUNT                */

	public static SoundEvent SWING_METAL_BLUNT_LEFT_0;
	public static SoundEvent SWING_METAL_BLUNT_LEFT_1;
	public static SoundEvent SWING_METAL_BLUNT_LEFT_2;
	public static SoundEvent SWING_METAL_BLUNT_LEFT_3;
	public static SoundEvent SWING_METAL_BLUNT_RIGHT_0;
	public static SoundEvent SWING_METAL_BLUNT_RIGHT_1;
	public static SoundEvent SWING_METAL_BLUNT_RIGHT_2;
	public static SoundEvent SWING_METAL_BLUNT_RIGHT_3;
	
	/*               GENERIC              */

	public static SoundEvent SWING_NORMAL_LEFT_0;
	public static SoundEvent SWING_NORMAL_LEFT_1;
	public static SoundEvent SWING_NORMAL_LEFT_2;
	public static SoundEvent SWING_NORMAL_LEFT_3;
	public static SoundEvent SWING_NORMAL_RIGHT_0;
	public static SoundEvent SWING_NORMAL_RIGHT_1;
	public static SoundEvent SWING_NORMAL_RIGHT_2;
	public static SoundEvent SWING_NORMAL_RIGHT_3;
	
	public static SoundEvent SWING_QUICK_LEFT_0;
	public static SoundEvent SWING_QUICK_LEFT_1;
	public static SoundEvent SWING_QUICK_RIGHT_0;
	public static SoundEvent SWING_QUICK_RIGHT_1;

	public static SoundEvent SWING_SLOW_LEFT_0;
	public static SoundEvent SWING_SLOW_LEFT_1;
	public static SoundEvent SWING_SLOW_LEFT_2;
	public static SoundEvent SWING_SLOW_RIGHT_0;
	public static SoundEvent SWING_SLOW_RIGHT_1;
	public static SoundEvent SWING_SLOW_RIGHT_2;
	
	/*                EQUIP              */

	public static SoundEvent EQUIP_BLADE_LEFT_0;
	public static SoundEvent EQUIP_BLADE_LEFT_1;
	public static SoundEvent EQUIP_BLADE_LEFT_2;
	public static SoundEvent EQUIP_BLADE_LEFT_3;
	public static SoundEvent EQUIP_BLADE_RIGHT_0;
	public static SoundEvent EQUIP_BLADE_RIGHT_1;
	public static SoundEvent EQUIP_BLADE_RIGHT_2;
	public static SoundEvent EQUIP_BLADE_RIGHT_3;

	public static SoundEvent EQUIP_AXE_LEFT_0;
	public static SoundEvent EQUIP_AXE_LEFT_1;
	public static SoundEvent EQUIP_AXE_LEFT_2;
	public static SoundEvent EQUIP_AXE_RIGHT_0;
	public static SoundEvent EQUIP_AXE_RIGHT_1;
	public static SoundEvent EQUIP_AXE_RIGHT_2;

	public static SoundEvent EQUIP_OTHER_LEFT_0;
	public static SoundEvent EQUIP_OTHER_LEFT_1;
	public static SoundEvent EQUIP_OTHER_LEFT_2;
	public static SoundEvent EQUIP_OTHER_RIGHT_0;
	public static SoundEvent EQUIP_OTHER_RIGHT_1;
	public static SoundEvent EQUIP_OTHER_RIGHT_2;
	
	/*               SHEATHE              */

	public static SoundEvent SHEATHE_BLADE_LEFT_0;
	public static SoundEvent SHEATHE_BLADE_LEFT_1;
	public static SoundEvent SHEATHE_BLADE_LEFT_2;
	public static SoundEvent SHEATHE_BLADE_RIGHT_0;
	public static SoundEvent SHEATHE_BLADE_RIGHT_1;
	public static SoundEvent SHEATHE_BLADE_RIGHT_2;
	
	public static SoundEvent SHEATHE_AXE_LEFT_0;
	public static SoundEvent SHEATHE_AXE_LEFT_1;
	public static SoundEvent SHEATHE_AXE_RIGHT_0;
	public static SoundEvent SHEATHE_AXE_RIGHT_1;
	
	public static SoundEvent SHEATHE_OTHER_LEFT_0;
	public static SoundEvent SHEATHE_OTHER_LEFT_1;
	public static SoundEvent SHEATHE_OTHER_LEFT_2;
	public static SoundEvent SHEATHE_OTHER_RIGHT_0;
	public static SoundEvent SHEATHE_OTHER_RIGHT_1;
	public static SoundEvent SHEATHE_OTHER_RIGHT_2;

	public static SoundEvent registerSound( String name )
	{
		ResourceLocation location = new ResourceLocation(Reference.MOD_ID, name);
		SoundEvent event = new SoundEvent(location).setRegistryName(name);
		ForgeRegistries.SOUND_EVENTS.register(event);
		return event;
	}

	public static void registerSounds()
	{
		/* --------------------------------- */
		/*                BASH               */
		/* --------------------------------- */
		
		/*               METAL               */

		BASH_METAL_SHIELD_0 = registerSound("player.bash_metal_shield_0");
		BASH_METAL_SHIELD_1 = registerSound("player.bash_metal_shield_1");
		BASH_METAL_SHIELD_2 = registerSound("player.bash_metal_shield_2");
		BASH_METAL_SHIELD_3 = registerSound("player.bash_metal_shield_3");
		
		/*                WOOD               */

		BASH_WOOD_SHIELD_0 = registerSound("player.bash_wood_shield_0");
		BASH_WOOD_SHIELD_1 = registerSound("player.bash_wood_shield_1");
		BASH_WOOD_SHIELD_2 = registerSound("player.bash_wood_shield_2");
		BASH_WOOD_SHIELD_3 = registerSound("player.bash_wood_shield_3");

		/* --------------------------------- */
		/*               BLOCK               */
		/* --------------------------------- */

		/*               HEAVY               */

		BLOCK_METAL_HEAVY_0 = registerSound("player.block_metal_heavy_0");
		BLOCK_METAL_HEAVY_1 = registerSound("player.block_metal_heavy_1");
		BLOCK_METAL_HEAVY_2 = registerSound("player.block_metal_heavy_2");
		BLOCK_METAL_HEAVY_3 = registerSound("player.block_metal_heavy_3");

		/*               LIGHT               */

		BLOCK_METAL_LIGHT_0 = registerSound("player.block_metal_light_0");
		BLOCK_METAL_LIGHT_1 = registerSound("player.block_metal_light_1");
		BLOCK_METAL_LIGHT_2 = registerSound("player.block_metal_light_2");
		BLOCK_METAL_LIGHT_3 = registerSound("player.block_metal_light_3");

		/* --------------------------------- */
		/*              IMPACT               */
		/* --------------------------------- */
		
		/*               RANGED              */

		IMPACT_RANGED_0 = registerSound("player.impact_ranged_0");

		/*               ARMOR               */

		IMPACT_ARMOR_BLADE_0 = registerSound("player.impact_armor_blade_0");
		IMPACT_ARMOR_BLADE_1 = registerSound("player.impact_armor_blade_1");
		
		IMPACT_ARMOR_METAL_0 = registerSound("player.impact_armor_metal_0");
		
		/*                STAB               */

		IMPACT_STAB_0 = registerSound("player.impact_stab_0");

		/*                AXE                */

		IMPACT_METAL_AXE_0 = registerSound("player.impact_metal_axe_0");
		IMPACT_METAL_AXE_1 = registerSound("player.impact_metal_axe_1");
		IMPACT_METAL_AXE_2 = registerSound("player.impact_metal_axe_2");
		IMPACT_METAL_AXE_3 = registerSound("player.impact_metal_axe_3");
		IMPACT_METAL_AXE_4 = registerSound("player.impact_metal_axe_4");
		IMPACT_METAL_AXE_5 = registerSound("player.impact_metal_axe_5");
		IMPACT_METAL_AXE_6 = registerSound("player.impact_metal_axe_6");
		IMPACT_METAL_AXE_7 = registerSound("player.impact_metal_axe_7");
		IMPACT_METAL_AXE_8 = registerSound("player.impact_metal_axe_8");
		IMPACT_METAL_AXE_9 = registerSound("player.impact_metal_axe_9");
		IMPACT_METAL_AXE_10 = registerSound("player.impact_metal_axe_10");

		/*                BLADE              */

		IMPACT_METAL_BLADE_0 = registerSound("player.impact_metal_blade_0");
		IMPACT_METAL_BLADE_1 = registerSound("player.impact_metal_blade_1");
		
		/*                BLUNT              */

		IMPACT_METAL_BLUNT_0 = registerSound("player.impact_metal_blunt_0");
		IMPACT_METAL_BLUNT_1 = registerSound("player.impact_metal_blunt_1");
		IMPACT_METAL_BLUNT_2 = registerSound("player.impact_metal_blunt_2");
		IMPACT_METAL_BLUNT_3 = registerSound("player.impact_metal_blunt_3");
		IMPACT_METAL_BLUNT_4 = registerSound("player.impact_metal_blunt_4");
		IMPACT_METAL_BLUNT_5 = registerSound("player.impact_metal_blunt_5");
		IMPACT_METAL_BLUNT_6 = registerSound("player.impact_metal_blunt_6");
		IMPACT_METAL_BLUNT_7 = registerSound("player.impact_metal_blunt_7");
		IMPACT_METAL_BLUNT_8 = registerSound("player.impact_metal_blunt_8");
		
		/*               PUNCH               */

		IMPACT_PUNCH_0 = registerSound("player.impact_punch_0");
		IMPACT_PUNCH_1 = registerSound("player.impact_punch_1");
		IMPACT_PUNCH_2 = registerSound("player.impact_punch_2");
		IMPACT_PUNCH_3 = registerSound("player.impact_punch_3");
		IMPACT_PUNCH_4 = registerSound("player.impact_punch_4");
		IMPACT_PUNCH_5 = registerSound("player.impact_punch_5");
		
		/* --------------------------------- */
		/*                SWING              */
		/* --------------------------------- */
		
		/*                 2H                */

		SWING_2H_LEFT_0 = registerSound("player.swing_2h_left_0");
		SWING_2H_LEFT_1 = registerSound("player.swing_2h_left_1");
		SWING_2H_LEFT_2 = registerSound("player.swing_2h_left_2");
		SWING_2H_RIGHT_0 = registerSound("player.swing_2h_right_0");
		SWING_2H_RIGHT_1 = registerSound("player.swing_2h_right_1");
		SWING_2H_RIGHT_2 = registerSound("player.swing_2h_right_2");
		
		/*                AXE                */

		SWING_METAL_AXE_LEFT_0 = registerSound("player.swing_metal_axe_left_0");
		SWING_METAL_AXE_LEFT_1 = registerSound("player.swing_metal_axe_left_1");
		SWING_METAL_AXE_LEFT_2 = registerSound("player.swing_metal_axe_left_2");
		SWING_METAL_AXE_LEFT_3 = registerSound("player.swing_metal_axe_left_3");
		SWING_METAL_AXE_RIGHT_0 = registerSound("player.swing_metal_axe_right_0");
		SWING_METAL_AXE_RIGHT_1 = registerSound("player.swing_metal_axe_right_1");
		SWING_METAL_AXE_RIGHT_2 = registerSound("player.swing_metal_axe_right_2");
		SWING_METAL_AXE_RIGHT_3 = registerSound("player.swing_metal_axe_right_3");

		/*               BLADE                */

		SWING_METAL_BLADE_LEFT_0 = registerSound("player.swing_metal_blade_left_0");
		SWING_METAL_BLADE_LEFT_1 = registerSound("player.swing_metal_blade_left_1");
		SWING_METAL_BLADE_LEFT_2 = registerSound("player.swing_metal_blade_left_2");
		SWING_METAL_BLADE_LEFT_3 = registerSound("player.swing_metal_blade_left_3");
		SWING_METAL_BLADE_LEFT_4 = registerSound("player.swing_metal_blade_left_4");
		SWING_METAL_BLADE_LEFT_5 = registerSound("player.swing_metal_blade_left_5");
		SWING_METAL_BLADE_LEFT_6 = registerSound("player.swing_metal_blade_left_6");
		SWING_METAL_BLADE_RIGHT_0 = registerSound("player.swing_metal_blade_right_0");
		SWING_METAL_BLADE_RIGHT_1 = registerSound("player.swing_metal_blade_right_1");
		SWING_METAL_BLADE_RIGHT_2 = registerSound("player.swing_metal_blade_right_2");
		SWING_METAL_BLADE_RIGHT_3 = registerSound("player.swing_metal_blade_right_3");
		SWING_METAL_BLADE_RIGHT_4 = registerSound("player.swing_metal_blade_right_4");
		SWING_METAL_BLADE_RIGHT_5 = registerSound("player.swing_metal_blade_right_5");
		SWING_METAL_BLADE_RIGHT_6 = registerSound("player.swing_metal_blade_right_6");

		/*               BLUNT                */

		SWING_METAL_BLUNT_LEFT_0 = registerSound("player.swing_metal_blunt_left_0");
		SWING_METAL_BLUNT_LEFT_1 = registerSound("player.swing_metal_blunt_left_1");
		SWING_METAL_BLUNT_LEFT_2 = registerSound("player.swing_metal_blunt_left_2");
		SWING_METAL_BLUNT_LEFT_3 = registerSound("player.swing_metal_blunt_left_3");
		SWING_METAL_BLUNT_RIGHT_0 = registerSound("player.swing_metal_blunt_right_0");
		SWING_METAL_BLUNT_RIGHT_1 = registerSound("player.swing_metal_blunt_right_1");
		SWING_METAL_BLUNT_RIGHT_2 = registerSound("player.swing_metal_blunt_right_2");
		SWING_METAL_BLUNT_RIGHT_3 = registerSound("player.swing_metal_blunt_right_3");
		
		/*               GENERIC              */

		SWING_NORMAL_LEFT_0 = registerSound("player.swing_normal_left_0");
		SWING_NORMAL_LEFT_1 = registerSound("player.swing_normal_left_1");
		SWING_NORMAL_LEFT_2 = registerSound("player.swing_normal_left_2");
		SWING_NORMAL_LEFT_3 = registerSound("player.swing_normal_left_3");
		SWING_NORMAL_RIGHT_0 = registerSound("player.swing_normal_right_0");
		SWING_NORMAL_RIGHT_1 = registerSound("player.swing_normal_right_1");
		SWING_NORMAL_RIGHT_2 = registerSound("player.swing_normal_right_2");
		SWING_NORMAL_RIGHT_3 = registerSound("player.swing_normal_right_3");
		
		SWING_QUICK_LEFT_0 = registerSound("player.swing_quick_left_0");
		SWING_QUICK_LEFT_1 = registerSound("player.swing_quick_left_1");
		SWING_QUICK_RIGHT_0 = registerSound("player.swing_quick_right_0");
		SWING_QUICK_RIGHT_1 = registerSound("player.swing_quick_right_1");

		SWING_SLOW_LEFT_0 = registerSound("player.swing_slow_left_0");
		SWING_SLOW_LEFT_1 = registerSound("player.swing_slow_left_1");
		SWING_SLOW_LEFT_2 = registerSound("player.swing_slow_left_2");
		SWING_SLOW_RIGHT_0 = registerSound("player.swing_slow_right_0");
		SWING_SLOW_RIGHT_1 = registerSound("player.swing_slow_right_1");
		SWING_SLOW_RIGHT_2 = registerSound("player.swing_slow_right_2");
		
		/* --------------------------------- */
		/*                EQUIP              */
		/* --------------------------------- */
		
		/*               BLADE              */

		EQUIP_BLADE_LEFT_0 = registerSound("player.equip_blade_left_0");
		EQUIP_BLADE_LEFT_1 = registerSound("player.equip_blade_left_1");
		EQUIP_BLADE_LEFT_2 = registerSound("player.equip_blade_left_2");
		EQUIP_BLADE_LEFT_3 = registerSound("player.equip_blade_left_3");
		EQUIP_BLADE_RIGHT_0 = registerSound("player.equip_blade_right_0");
		EQUIP_BLADE_RIGHT_1 = registerSound("player.equip_blade_right_1");
		EQUIP_BLADE_RIGHT_2 = registerSound("player.equip_blade_right_2");
		EQUIP_BLADE_RIGHT_3 = registerSound("player.equip_blade_right_3");

		/*               AXE              */

		EQUIP_AXE_LEFT_0 = registerSound("player.equip_axe_left_0");
		EQUIP_AXE_LEFT_1 = registerSound("player.equip_axe_left_1");
		EQUIP_AXE_LEFT_2 = registerSound("player.equip_axe_left_2");
		EQUIP_AXE_RIGHT_0 = registerSound("player.equip_axe_right_0");
		EQUIP_AXE_RIGHT_1 = registerSound("player.equip_axe_right_1");
		EQUIP_AXE_RIGHT_2 = registerSound("player.equip_axe_right_2");

		/*               OTHER              */

		EQUIP_OTHER_LEFT_0 = registerSound("player.equip_other_left_0");
		EQUIP_OTHER_LEFT_1 = registerSound("player.equip_other_left_1");
		EQUIP_OTHER_LEFT_2 = registerSound("player.equip_other_left_2");
		EQUIP_OTHER_RIGHT_0 = registerSound("player.equip_other_right_0");
		EQUIP_OTHER_RIGHT_1 = registerSound("player.equip_other_right_1");
		EQUIP_OTHER_RIGHT_2 = registerSound("player.equip_other_right_2");
		
		/* --------------------------------- */
		/*               SHEATHE             */
		/* --------------------------------- */
		
		/*               BLADE              */

		SHEATHE_BLADE_LEFT_0 = registerSound("player.sheathe_blade_left_0");
		SHEATHE_BLADE_LEFT_1 = registerSound("player.sheathe_blade_left_1");
		SHEATHE_BLADE_LEFT_2 = registerSound("player.sheathe_blade_left_2");
		SHEATHE_BLADE_RIGHT_0 = registerSound("player.sheathe_blade_right_0");
		SHEATHE_BLADE_RIGHT_1 = registerSound("player.sheathe_blade_right_1");
		SHEATHE_BLADE_RIGHT_2 = registerSound("player.sheathe_blade_right_2");
		
		/*               AXE              */

		SHEATHE_AXE_LEFT_0 = registerSound("player.sheathe_axe_left_0");
		SHEATHE_AXE_LEFT_1 = registerSound("player.sheathe_axe_left_1");
		SHEATHE_AXE_RIGHT_0 = registerSound("player.sheathe_axe_right_0");
		SHEATHE_AXE_RIGHT_1 = registerSound("player.sheathe_axe_right_1");

		/*               OTHER              */

		SHEATHE_OTHER_LEFT_0 = registerSound("player.sheathe_other_left_0");
		SHEATHE_OTHER_LEFT_1 = registerSound("player.sheathe_other_left_1");
		SHEATHE_OTHER_LEFT_2 = registerSound("player.sheathe_other_left_2");
		SHEATHE_OTHER_RIGHT_0 = registerSound("player.sheathe_other_right_0");
		SHEATHE_OTHER_RIGHT_1 = registerSound("player.sheathe_other_right_1");
		SHEATHE_OTHER_RIGHT_2 = registerSound("player.sheathe_other_right_2");
	}
	
	/* -------------------------------------------------------------------------------------------------------------------- */
	/*             											  SWING     						 					        */
	/* -------------------------------------------------------------------------------------------------------------------- */
	
	public static void playSwingSoundLeft( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown )
	{
		float volume = getRandomSwingVolume();
		float pitch = getSwingPitch(cooldown);
		
		if ( !betterCombatHand.hasCustomWeapon() )
		{
			if ( itemStack.getItem() instanceof ItemShield )
			{				
				if ( Helpers.isMetal(itemStack) )
				{
					swingMetalShield(elb, volume, getRandomSwingPitch());
					return;
				}
				else
				{
					swingWoodenShield(elb, volume, getRandomSwingPitch());
					return;
				}
			}
			else
			{
				swingNonMetalLeft(elb, volume, pitch);
				return;
			}
		}
		
		if ( betterCombatHand.getAnimation() == Animation.STAB )
		{
			swingNonMetalLeft(elb, volume, pitch);
			return;
		}
		
		if ( betterCombatHand.getWeaponProperty() == WeaponProperty.TWOHAND )
		{
			swing2HLeft(elb, volume, pitch);
			return;
		}
		
		if ( Helpers.isMetal(itemStack) )
		{
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					swingMetalBladeLeft(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					swingMetalAxeLeft(elb, volume, pitch);
					return;
				}
				case BLUNT:
				{
					swingMetalBluntLeft(elb, volume, pitch);
					return;
				}
				default:
				{
					swingNonMetalLeft(elb, volume, pitch);
					return;
				}
			}
		}
		else
		{
			swingNonMetalLeft(elb, volume, pitch);
			return;
		}
	}
	
	public static void playSwingSoundRight( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown )
	{
		float volume = getRandomSwingVolume();
		float pitch = getSwingPitch(cooldown);

		if ( !betterCombatHand.hasCustomWeapon() )
		{
			swingNonMetalRight(elb, volume, pitch);
			return;
		}
		
		if ( betterCombatHand.getAnimation() == Animation.STAB )
		{
			swingNonMetalRight(elb, volume, pitch);
			return;
		}
		
		if ( betterCombatHand.getWeaponProperty() == WeaponProperty.TWOHAND )
		{
			swing2HRight(elb, volume, pitch);
			return;
		}
		
		if ( Helpers.isMetal(itemStack) )
		{
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					swingMetalBladeRight(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					swingMetalAxeRight(elb, volume, pitch);
					return;
				}
				case BLUNT:
				{
					swingMetalBluntRight(elb, volume, pitch);
					return;
				}
				default:
				{
					swingNonMetalRight(elb, volume, pitch);
					return;
				}
			}
		}
		else
		{
			swingNonMetalRight(elb, volume, pitch);
			return;
		}
	}
	

	/* -------------------------------------------------------------------------------------------------------------------- */
	/*             											  SHIELD     						 					        */
	/* -------------------------------------------------------------------------------------------------------------------- */
	
	private static void swingWoodenShield( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(3) )
		{
			case 0:
			{
				playSound(elb, SWING_2H_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_2H_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_2H_LEFT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_2H_LEFT_0, volume, pitch);
				return;
			}
		}
	}

	private static void swingMetalShield( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_0, volume, pitch);
				return;
			}
		}
	}

	/* -------------------------------------------------------------------------------------------------------------------- */
	/*             											  EQUIP     						 					        */
	/* -------------------------------------------------------------------------------------------------------------------- */
	
	public static void playEquipSoundLeft( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown )
	{
		if ( !betterCombatHand.hasCustomWeapon() )
		{
			return;
		}		
		
		float volume = getRandomSwingVolume();
		float pitch = getSwingPitch(cooldown);
		
		if ( Helpers.isMetal(itemStack) )
		{
			/* AXE BLADE BLUNT NONE POLEARM */
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					playEquipBladeLeftSound(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					playEquipAxeLeftSound(elb, volume, pitch);
					return;
				}
				default:
				{
					playEquipOtherLeftSound(elb, volume, pitch);
					return;
				}
			}
		}
	}
	
	public static void playEquipSoundRight( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown )
	{
		if ( !betterCombatHand.hasCustomWeapon() )
		{
			return;
		}

		float volume = getRandomSwingVolume();
		float pitch = getSwingPitch(cooldown);
		
		if ( Helpers.isMetal(itemStack) )
		{
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					playEquipBladeRightSound(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					playEquipAxeRightSound(elb, volume, pitch);
					return;
				}
				default:
				{
					playEquipOtherRightSound(elb, volume, pitch);
					return;
				}
			}
		}
	}
	
	/* -------------------------------------------------------------------------------------------------------------------- */
	/*             											  SHEATHE     						 					        */
	/* -------------------------------------------------------------------------------------------------------------------- */
	
	public static void playSheatheSoundLeft( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown, boolean isMetal )
	{		
		if ( !betterCombatHand.hasCustomWeapon() )
		{
			return;
		}

		float volume = getRandomEquipAndSheatheVolume();
		float pitch = getEquipAndSheathePitch(cooldown);
		
		if ( isMetal )
		{
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					playSheatheBladeLeftSound(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					playEquipAxeLeftSound(elb, volume, pitch);
					return;
				}
				default:
				{
					playEquipOtherLeftSound(elb, volume, pitch);
					return;
				}
			}
		}
	}
	
	public static void playSheatheSoundRight( EntityLivingBase elb, BetterCombatHand betterCombatHand, ItemStack itemStack, int cooldown, boolean isMetal )
	{
		if ( !betterCombatHand.hasCustomWeapon() )
		{
			return;
		}

		float volume = getRandomEquipAndSheatheVolume();
		float pitch = getEquipAndSheathePitch(cooldown);
		
		if ( isMetal )
		{
			switch ( betterCombatHand.getSoundType() )
			{
				case BLADE:
				{
					playSheatheBladeRightSound(elb, volume, pitch);
					return;
				}
				case AXE:
				{
					playSheatheBladeRightSound(elb, volume, pitch);
					// XXX playEquipAxeLeftSound(elb, volume, pitch);
					return;
				}
				default:
				{
					playSheatheBladeRightSound(elb, volume, pitch);
					// XXX playEquipOthereLeftSound(elb, volume, pitch);
					return;
				}
			}
		}
	}

	/* -------------------------------------------------------------------------------------------------------------------- */
	/*             											  IMPACT     						 					        */
	/* -------------------------------------------------------------------------------------------------------------------- */
	
	public static void playImpactSound( EntityLivingBase elb, ItemStack itemStack, @Nullable SoundType soundType, @Nullable Animation animation, boolean isMetal )
	{
		float volume = getRandomImpactVolume();
		float pitch = getRandomImpactPitch();
		
		if ( soundType == null || animation == null )
		{
			playImpactPunchSound(elb, volume, pitch);
			return;
		}
		else
		{
			if ( animation == Animation.STAB )
			{
				playImpactStabSound(elb, volume, pitch);
				return;
			}
			
			if ( isMetal )
			{
				switch ( soundType )
				{
					case BLADE:
					{
						playImpactMetalBladeSound(elb, volume, pitch);
						return;
					}
					case AXE:
					{
						playImpactMetalAxeSound(elb, volume, pitch);
						return;
					}
					case BLUNT:
					{
						playImpactMetalBluntSound(elb, volume, pitch);
						return;
					}
					default:
					{
						playImpactMetalAxeSound(elb, volume, pitch);
						return;
					}
				}
			}
			
			playImpactDefaultSound(elb, volume, pitch);
			return;
		}
	}
	
	public static void playBashMetalShieldSound( EntityLivingBase elb )
	{
		float volume = getRandomImpactVolume();
		float pitch = getRandomImpactPitch();
		
		switch ( Helpers.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BASH_METAL_SHIELD_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BASH_METAL_SHIELD_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BASH_METAL_SHIELD_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BASH_METAL_SHIELD_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BASH_METAL_SHIELD_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void playBashWoodShieldSound( EntityLivingBase elb )
	{
		float volume = getRandomImpactVolume();
		float pitch = getRandomImpactPitch();
		
		switch ( Helpers.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BASH_WOOD_SHIELD_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BASH_WOOD_SHIELD_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BASH_WOOD_SHIELD_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BASH_WOOD_SHIELD_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BASH_WOOD_SHIELD_0, volume, pitch);
				return;
			}
		}
	}
	
	/* --------------------------------- */
	/*                SWORD              */
	/* --------------------------------- */

	public static void swingMetalBladeLeft( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(7) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_5, volume, pitch);
				return;
			}
			case 6:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_6, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_BLADE_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swingMetalBladeRight( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(7) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_5, volume, pitch);
				return;
			}
			case 6:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_6, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_BLADE_RIGHT_0, volume, pitch);
				return;
			}
		}
	}

	public static void swingMetalAxeLeft( EntityLivingBase elb, float volume, float pitch )
	{
		switch(elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_AXE_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_AXE_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_AXE_LEFT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_AXE_LEFT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_AXE_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swingMetalAxeRight( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_AXE_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_AXE_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_AXE_RIGHT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_AXE_RIGHT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_AXE_RIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swingMetalBluntRight( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_BLUNT_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_BLUNT_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_BLUNT_RIGHT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_BLUNT_RIGHT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_BLUNT_RIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swingMetalBluntLeft( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_METAL_BLUNT_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void playImpactMetalBladeSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(2) )
		{
			case 0:
			{
				playSound(elb, IMPACT_METAL_BLADE_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_METAL_BLADE_1, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, IMPACT_METAL_BLADE_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void playImpactMetalBluntSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(9) )
		{
			case 0:
			{
				playSound(elb, IMPACT_METAL_BLUNT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_METAL_BLUNT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, IMPACT_METAL_BLUNT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, IMPACT_METAL_BLUNT_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, IMPACT_METAL_BLUNT_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, IMPACT_METAL_BLUNT_5, volume, pitch);
				return;
			}
			case 6:
			{
				playSound(elb, IMPACT_METAL_BLUNT_6, volume, pitch);
				return;
			}
			case 7:
			{
				playSound(elb, IMPACT_METAL_BLUNT_7, volume, pitch);
				return;
			}
			case 8:
			{
				playSound(elb, IMPACT_METAL_BLUNT_8, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, IMPACT_METAL_BLUNT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void playImpactMetalAxeSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(11) )
		{
			case 0:
			{
				playSound(elb, IMPACT_METAL_AXE_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_METAL_AXE_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, IMPACT_METAL_AXE_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, IMPACT_METAL_AXE_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, IMPACT_METAL_AXE_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, IMPACT_METAL_AXE_5, volume, pitch);
				return;
			}
			case 6:
			{
				playSound(elb, IMPACT_METAL_AXE_6, volume, pitch);
				return;
			}
			case 7:
			{
				playSound(elb, IMPACT_METAL_AXE_7, volume, pitch);
				return;
			}
			case 8:
			{
				playSound(elb, IMPACT_METAL_AXE_8, volume, pitch);
				return;
			}
			case 9:
			{
				playSound(elb, IMPACT_METAL_AXE_9, volume, pitch);
				return;
			}
			case 10:
			{
				playSound(elb, IMPACT_METAL_AXE_10, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, IMPACT_METAL_AXE_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void playImpactMetalPunchSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(6) )
		{
			case 0:
			{
				playSound(elb, IMPACT_PUNCH_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_PUNCH_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, IMPACT_PUNCH_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, IMPACT_PUNCH_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, IMPACT_PUNCH_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, IMPACT_PUNCH_5, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, IMPACT_PUNCH_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void impactRanged( EntityLivingBase elb, float volume, float pitch )
	{
		playSound(elb, IMPACT_RANGED_0, volume, pitch);
	}
	
	public static void blockMetalHeavy( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BLOCK_METAL_HEAVY_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BLOCK_METAL_HEAVY_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BLOCK_METAL_HEAVY_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BLOCK_METAL_HEAVY_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BLOCK_METAL_HEAVY_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void blockMetalLight( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BLOCK_METAL_LIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BLOCK_METAL_LIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BLOCK_METAL_LIGHT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BLOCK_METAL_LIGHT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BLOCK_METAL_LIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void blockWoodShield( EntityLivingBase elb, float volume, float pitch )
	{
		playSound(elb, SoundEvents.ITEM_SHIELD_BLOCK, volume, pitch);
	}
	
	public static void bashMetalShield( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BASH_METAL_SHIELD_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BASH_METAL_SHIELD_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BASH_METAL_SHIELD_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BASH_METAL_SHIELD_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BASH_METAL_SHIELD_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void bashWoodShield( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, BASH_WOOD_SHIELD_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, BASH_WOOD_SHIELD_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, BASH_WOOD_SHIELD_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, BASH_WOOD_SHIELD_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, BASH_WOOD_SHIELD_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swing2HRight(EntityLivingBase elb, float volume, float pitch)
	{
		switch( elb.world.rand.nextInt(3) )
		{
			case 0:
			{
				playSound(elb, SWING_2H_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_2H_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_2H_RIGHT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_2H_RIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swing2HLeft(EntityLivingBase elb, float volume, float pitch)
	{
		switch( elb.world.rand.nextInt(3) )
		{
			case 0:
			{
				playSound(elb, SWING_2H_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SWING_2H_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SWING_2H_LEFT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SWING_2H_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	public static void swingNonMetalRight( EntityLivingBase elb, float volume, float pitch )
	{
		if ( pitch <= 0.9F )
		{
			pitch += 0.1F;
			
			switch( elb.world.rand.nextInt(3) )
			{
				case 0:
				{
					playSound(elb, SWING_SLOW_RIGHT_0, volume, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_SLOW_RIGHT_1, volume, pitch);
					return;
				}
				case 2:
				{
					playSound(elb, SWING_SLOW_RIGHT_2, volume, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_SLOW_RIGHT_0, volume, pitch);
					return;
				}
			}
		}
		else if ( pitch >= 1.1F )
		{
			pitch -= 0.1F;

			switch( elb.world.rand.nextInt(2) )
			{
				case 0:
				{
					playSound(elb, SWING_QUICK_RIGHT_0, volume*0.6F, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_QUICK_RIGHT_1, volume*0.6F, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_QUICK_RIGHT_0, volume*0.6F, pitch);
					return;
				}
			}
		}
		else
		{
			switch( elb.world.rand.nextInt(4) )
			{
				case 0:
				{
					playSound(elb, SWING_NORMAL_RIGHT_0, volume, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_NORMAL_RIGHT_1, volume, pitch);
					return;
				}
				case 2:
				{
					playSound(elb, SWING_NORMAL_RIGHT_2, volume, pitch);
					return;
				}
				case 3:
				{
					playSound(elb, SWING_NORMAL_RIGHT_3, volume, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_NORMAL_RIGHT_0, volume, pitch);
					return;
				}
			}
		}
	}
	
	public static void swingNonMetalLeft( EntityLivingBase elb, float volume, float pitch )
	{
		if ( pitch <= 0.9F )
		{
			pitch += 0.1F;
			
			switch( elb.world.rand.nextInt(3) )
			{
				case 0:
				{
					playSound(elb, SWING_SLOW_LEFT_0, volume, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_SLOW_LEFT_1, volume, pitch);
					return;
				}
				case 2:
				{
					playSound(elb, SWING_SLOW_LEFT_2, volume, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_SLOW_LEFT_0, volume, pitch);
					return;
				}
			}
		}
		else if ( pitch >= 1.1F )
		{
			pitch -= 0.1F;

			switch( elb.world.rand.nextInt(2) )
			{
				case 0:
				{
					playSound(elb, SWING_QUICK_LEFT_0, volume*0.6F, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_QUICK_LEFT_1, volume*0.6F, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_QUICK_LEFT_0, volume*0.6F, pitch);
					return;
				}
			}
		}
		else
		{
			switch( elb.world.rand.nextInt(4) )
			{
				case 0:
				{
					playSound(elb, SWING_NORMAL_LEFT_0, volume, pitch);
					return;
				}
				case 1:
				{
					playSound(elb, SWING_NORMAL_LEFT_1, volume, pitch);
					return;
				}
				case 2:
				{
					playSound(elb, SWING_NORMAL_LEFT_2, volume, pitch);
					return;
				}
				case 3:
				{
					playSound(elb, SWING_NORMAL_LEFT_3, volume, pitch);
					return;
				}
				default:
				{
					playSound(elb, SWING_NORMAL_LEFT_0, volume, pitch);
					return;
				}
			}
		}
	}

	public static void playImpactPunchSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(6) )
		{
			case 0:
			{
				playSound(elb, IMPACT_PUNCH_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_PUNCH_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, IMPACT_PUNCH_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, IMPACT_PUNCH_3, volume, pitch);
				return;
			}
			case 4:
			{
				playSound(elb, IMPACT_PUNCH_4, volume, pitch);
				return;
			}
			case 5:
			{
				playSound(elb, IMPACT_PUNCH_5, volume, pitch);
				return;
			}
			default:
			{
				return;
			}
		}		
	}
	
	public static void playImpactArmorBladeSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(2) )
		{
			case 0:
			{
				playSound(elb, IMPACT_ARMOR_BLADE_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, IMPACT_ARMOR_BLADE_1, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, IMPACT_ARMOR_BLADE_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playEquipBladeLeftSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_BLADE_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_BLADE_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, EQUIP_BLADE_LEFT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, EQUIP_BLADE_LEFT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_BLADE_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playEquipAxeLeftSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_AXE_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_AXE_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, EQUIP_AXE_LEFT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_AXE_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playEquipOtherLeftSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_OTHER_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_OTHER_LEFT_1, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_OTHER_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playEquipAxeRightSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_AXE_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_AXE_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, EQUIP_AXE_RIGHT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_AXE_RIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playEquipOtherRightSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_OTHER_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_OTHER_RIGHT_1, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_OTHER_RIGHT_0, volume, pitch);
				return;
			}
		}
	}

	private static void playEquipBladeRightSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(4) )
		{
			case 0:
			{
				playSound(elb, EQUIP_BLADE_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, EQUIP_BLADE_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, EQUIP_BLADE_RIGHT_2, volume, pitch);
				return;
			}
			case 3:
			{
				playSound(elb, EQUIP_BLADE_RIGHT_3, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, EQUIP_BLADE_RIGHT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playSheatheBladeLeftSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(3) )
		{
			case 0:
			{
				playSound(elb, SHEATHE_BLADE_LEFT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SHEATHE_BLADE_LEFT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SHEATHE_BLADE_LEFT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SHEATHE_BLADE_LEFT_0, volume, pitch);
				return;
			}
		}
	}
	
	private static void playSheatheBladeRightSound( EntityLivingBase elb, float volume, float pitch )
	{
		switch( elb.world.rand.nextInt(3) )
		{
			case 0:
			{
				playSound(elb, SHEATHE_BLADE_RIGHT_0, volume, pitch);
				return;
			}
			case 1:
			{
				playSound(elb, SHEATHE_BLADE_RIGHT_1, volume, pitch);
				return;
			}
			case 2:
			{
				playSound(elb, SHEATHE_BLADE_RIGHT_2, volume, pitch);
				return;
			}
			default:
			{
				playSound(elb, SHEATHE_BLADE_RIGHT_0, volume, pitch);
				return;
			}
		}
	}

	private static void playImpactDefaultSound(EntityLivingBase elb, float volume, float pitch)
	{
		playSound(elb, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, volume, pitch);
	}
	
	public static void playImpactArmorMetalSound( EntityLivingBase elb, float volume, float pitch )
	{
		playSound(elb, IMPACT_ARMOR_METAL_0, volume, pitch);	
	}
	
	public static void playImpactStabSound( EntityLivingBase elb, float volume, float pitch )
	{
		playSound(elb, IMPACT_STAB_0, volume, pitch);	
	}
	
	public static void playSound( Entity player, SoundEvent soundEvent, float volume, float pitch )
	{
		try
		{
			player.playSound(soundEvent, volume, pitch);
			player.world.playSound(null, player.posX, player.posY, player.posZ, soundEvent, player.getSoundCategory(), volume, pitch);
		}
		catch ( Exception e )
		{
			/* Random crash on world reload. I don't care just wrap it in a try catch */
		}
	}
	
	
//	public float mainhandSwingPitch( EntityLivingBase elb, Animation animation )
//	{
//		float mainhandCooldownPeriod = getCooldownPeriod();
//		
//		/* Default Cooldown = 13 */
//		/* Default Speed 	= 1.6 */
//		if ( animation == Animation.SWEEP )
//		{
//			return (float) MathHelper.clamp(0.5D + 6.5D / getCooldownPeriod(), 0.8D, 1.2D);
//		}
//		
//		/* Default Cooldown = 14 */
//		/* Default Speed 	= 1.4 */
//		if ( animation == Animation.STAB )
//		{
//			return (float) MathHelper.clamp(0.5D + 7.0D / getCooldownPeriod(), 0.8D, 1.2D);
//		}
//		
//		/* Default Cooldown = 16 */
//		/* Default Speed 	= 1.2 */
//		if ( animation == Animation.CHOP )
//		{
//			return (float) MathHelper.clamp(0.5D + 8.0D / getCooldownPeriod(), 0.8D, 1.2D);
//		}
//		
//		return 1.0F;
//	}

	public float offSwingPitch( EntityLivingBase elb, ItemStack oh, ItemStack mainhand, int offcd )
	{
		/* Default CooldownPeriod = 20 */
		/* Default AttackSpeed 	  = 1.0 */
		return (float) MathHelper.clamp(0.6D + 8.D / offcd, 0.8D, 1.2D);
	}
	
	public static float getRandomSwingPitch()
	{
		return 0.95F + Helpers.rand.nextFloat() * 0.1F;
	}
	
	public static float getRandomImpactPitch()
	{
		return (0.9F + Helpers.rand.nextFloat() * 0.2F);
	}
	
	public static float getRandomShieldBlockVolume()
	{
		return ConfigurationHandler.shieldBlockSoundVolume * (0.7F + Helpers.rand.nextFloat() * 0.1F);
	}
	
	public static float getRandomShieldBashVolume()
	{
		return ConfigurationHandler.shieldBashSoundVolume * (0.9F + Helpers.rand.nextFloat() * 0.1F);
	}
	
	public static float getRandomSwingVolume()
	{
		return ConfigurationHandler.weaponSwingSoundVolume * (0.7F + Helpers.rand.nextFloat() * 0.1F);
	}
	
	public static float getSwingPitch( float f )
	{
		return 0.75F + ( (12.5F + (float)ConfigurationHandler.addedSwingTickCooldown) / f ) * 0.25F;
	}
	
	private static float getRandomImpactVolume()
	{
		return ConfigurationHandler.weaponHitSoundVolume * (0.7F + Helpers.rand.nextFloat() * 0.1F);
	}
	
	public static float getRandomEquipAndSheatheVolume()
	{
		return ConfigurationHandler.weapoEquipAndSheatheSoundVolume * (0.7F + Helpers.rand.nextFloat() * 0.1F);
	}
	
	public static float getEquipAndSheathePitch( float f )
	{
		return 0.8F + ( (12.5F + (float)ConfigurationHandler.addedSwingTickCooldown) / f ) * 0.2F;
	}
}