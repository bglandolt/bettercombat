package bettercombat.mod.client;

import java.util.Random;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.Animation;
import bettercombat.mod.util.ConfigurationHandler.ConfigWeapon;
import bettercombat.mod.util.ConfigurationHandler.SoundType;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BetterCombatHand
{
	public BetterCombatHand()
	{
		this.resetBetterCombatWeapon();
	}
	
	public void setBetterCombatWeapon( ConfigWeapon configWeapon, ItemStack itemStack, int equipSoundCooldownTicks )
	{
		this.configWeapon = configWeapon;
		
		/* Get the total sweep for the weapon */
		int sweepMod = configWeapon.sweepMod;
		
		NBTTagList nbttaglist = itemStack.getEnchantmentTagList();
		
		for ( int i = 0; i < nbttaglist.tagCount(); ++i )
		{
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
			int j = nbttagcompound.getShort("lvl");
			
			if ( enchantment == Enchantments.SWEEPING )
			{
				if ( j > 0 )
				{
					sweepMod += j;
				}
			}
		}
		
		this.canWeaponParry = configWeapon.parry;
		this.sweep = sweepMod;
				
		int cd = MathHelper.clamp(equipSoundCooldownTicks, ConfigurationHandler.minimumAttackSpeedTicks, 15);

		this.equipTimerIncrement = 1.0F / (3.0F * (cd));
		this.equipSoundTimer = cd / 2;
	}
	
	public boolean hasConfigWeapon()
	{
		return this.configWeapon != null;
	}
	
	public void tick()
	{
		this.swingTimer--;
	}
	
	public WeaponProperty getWeaponProperty()
	{
		if ( this.hasConfigWeapon() )
		{
			return this.getConfigWeapon().property;
		}
		
		return WeaponProperty.ONEHAND;
	}
	
	private ConfigWeapon getConfigWeapon()
	{
		return this.configWeapon;
	}

	public Animation getAnimation()
	{
		if ( this.hasConfigWeapon() )
		{
			return this.getConfigWeapon().animation;
		}
		
		return Animation.NONE;
	}
	
	public SoundType getSoundType()
	{
		if ( this.hasConfigWeapon() )
		{
			return this.getConfigWeapon().soundType;
		}
		
		return SoundType.NONE;
	}
	
	public double getFatigue()
	{
		if ( this.hasConfigWeapon() )
		{
			if ( this.getConfigWeapon().property == WeaponProperty.VERSATILE )
			{
				return ConfigurationHandler.versatileFatigueAmount;
			}
		}
		
		return 0.0;
	}
	
	public double getAdditionalReach()
	{
		if ( this.hasConfigWeapon() )
		{
			return this.getConfigWeapon().additionalReachMod;
		}
		
		return -ConfigurationHandler.fistAndNonWeaponReachReduction;
	}
	
	public boolean canWeaponParry()
	{
		return this.canWeaponParry; // !ConfigurationHandler.disablecanWeaponParrying && 
	}
	
	public int getSweep()
	{
		return this.sweep;
	}
	
	public float getEquipTimerIncrement()
	{
		return this.equipTimerIncrement;
	}
	
	public void resetBetterCombatWeapon()
	{
		this.configWeapon = null;
		
		this.canWeaponParry = false;
		this.sweep = 0;
		
		this.resetSwingTimer();
		this.swingTimerCap = 0;
		this.swingTimerIncrement = 0.0F;

		this.equipSoundTimer = 0;
		this.equipTimerIncrement = 0.5F;
		
		this.swingTimestampSound = 0;
		this.swingTimestampDamage = 0;
		
		this.mining = false;
	}
	
	/* The config weapon */
	ConfigWeapon configWeapon = null;
	
	private boolean canWeaponParry = false;
	
	/* The weapons config reach amount */
	private int sweep = 0;

	/* How long the swing timer is in ticks, counting down to 0 */
	private int swingTimer = 0;
	/* The value the swing timer started at, in ticks */
	private int swingTimerCap = 0;
	/* How fast the animation counts down, in partial ticks */
	private float swingTimerIncrement = 0.0F;
	
	/* How long the equip sound timer is in ticks after equipping a weapon, counting down to 0, This is only used for determining equip/sheathe sounds */
	public int equipSoundTimer = 0;
	/* How long the equip animation is, in partial ticks */
	private float equipTimerIncrement = 0.5F;
	
	/* When the swingTimer reaches this number, make a swing sound */
	private int swingTimestampSound = 0;
	/* When the swingTimer reaches this number, send a damage packet */
	private int swingTimestampDamage = 0;

	/* Mouse held down and is mining a block */
	private boolean mining = false;
	
	
	
	public float moveRightVariance = 1.0F;
	public float moveUpVariance = 1.0F;
	public float moveCloseVariance = 1.0F;

	public float rotateUpVariance = 1.0F;
	public float rotateCounterClockwiseVariance = 1.0F;
	public float rotateLeftVariance = 1.0F;
	
	public boolean alternateAnimation = false;

	private final Random rand = new Random();
	
	public void randomizeVariances()
	{		
		this.moveRightVariance = this.randomMoveVariance();
		this.moveUpVariance = this.randomMoveVariance();
		this.moveCloseVariance = this.randomMoveVariance();

		this.rotateUpVariance = this.randomRotationVariance();
		this.rotateCounterClockwiseVariance = this.randomRotationVariance();
		this.rotateLeftVariance = this.randomRotationVariance();
	}
	
	public float randomMoveVariance()
	{
		return 1.06F - this.rand.nextFloat() * 0.12F;
	}

	public float randomRotationVariance()
	{
		return 1.03F - this.rand.nextFloat() * 0.06F;
	}
	
	public float randomPreciseRotationVariance()
	{
		return 1.015F - this.rand.nextFloat() * 0.03F;
	}
	
	public void stopAttack()
	{
		this.resetSwingTimer();
	}
	
	public void resetSwingTimer()
	{
		this.swingTimer = 0;
	}
	
	public boolean isSwinging()
	{
		return this.swingTimer > 0;
	}
	
	public boolean soundReady()
	{
		return this.swingTimer == this.swingTimestampSound;
	}
	
	public boolean damageReady()
	{
		return this.swingTimer == this.getSwingTimestampDamage();
	}
	
	public void initiateAnimation( int i )
	{
		if ( this.hasConfigWeapon() )
		{
			switch( this.getConfigWeapon().animation )
			{
				case SWEEP:
				{
					this.setSweeping(i);
					return;
				}
				case STAB:
				{
					this.setStabbing(i);
					return;
				}
				case CHOP:
				{
					this.setChopping(i);
					return;
				}
				default:
				{
					this.setPunching(i);
					return;
				}
			}
		}
		else
		{
			this.setPunching(i);
			return;
		}
	}
	
	public void setSweeping( int i )
	{
		this.mining = false;

		this.swingTimer = MathHelper.clamp(i, ConfigurationHandler.minimumAttackSpeedTicks, 14)-2;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 1.0F/this.swingTimer;
		
		if ( this.alternateAnimation = this.rand.nextBoolean() )
		{
			this.swingTimestampSound = Math.round(this.swingTimer*0.5F);
			this.swingTimestampDamage = this.swingTimestampSound-1;
		}
		else
		{
			this.swingTimestampSound = Math.round(this.swingTimer*0.8F);
			this.swingTimestampDamage = this.swingTimestampSound-1;
		}
		
		this.moveRightVariance = this.randomMoveVariance();
		this.moveUpVariance = this.randomMoveVariance();
		this.moveCloseVariance = this.randomMoveVariance();

		this.rotateUpVariance = this.randomPreciseRotationVariance();
		this.rotateCounterClockwiseVariance = this.randomRotationVariance();
		this.rotateLeftVariance = this.randomRotationVariance();
	}
	
	public void setStabbing( int i )
	{
		this.mining = false;

		this.swingTimer = MathHelper.clamp(i, ConfigurationHandler.minimumAttackSpeedTicks, 13)-1;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 1.0F/this.swingTimer;
				
		this.swingTimestampSound = Math.round(this.swingTimer*0.8F);
		this.swingTimestampDamage = this.swingTimestampSound-1;
		
		this.randomizeVariances();
	}
	
	public void setChopping( int i )
	{
		this.mining = false;
		
		this.swingTimer = MathHelper.clamp(i, ConfigurationHandler.minimumAttackSpeedTicks, 14)-2;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 1.0F/this.swingTimer;
				
		this.swingTimestampSound = Math.round(this.swingTimer*0.8F);
		this.swingTimestampDamage = this.swingTimestampSound-1;
		
		this.randomizeVariances();
	}

	public void setPunching( int i )
	{
		this.mining = false;
		
		this.swingTimer = MathHelper.clamp(i, ConfigurationHandler.minimumAttackSpeedTicks, 14)-2;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 1.0F/this.swingTimer;
				
		this.swingTimestampSound = Math.round(this.swingTimer*0.8F);
		this.swingTimestampDamage = this.swingTimestampSound-1;
	}
	
	public void startMining( int i )
	{
		this.mining = true;
		
		this.swingTimer = i;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 1.0F/this.swingTimer;
		
		this.swingTimestampSound = 0;
		this.swingTimestampDamage = 0;
		
		this.randomizeVariances();
	}

	public void setShieldBashing()
	{
		this.mining = false;
		
		this.swingTimer = 10;
		this.swingTimerCap = this.swingTimer;
		this.swingTimerIncrement = 0.1F;
		
		this.swingTimestampSound = 5;
		this.swingTimestampDamage = 7;
	}
	
	/* How long the swing is in ticks, counting down to 0 */
	public int getSwingTimer()
	{
		return this.swingTimer;
	}
	
	/* The increment of the swing timer = 1.0F/this.swingTimer */
	public float getSwingTimerIncrement()
	{
		return this.swingTimerIncrement;
	}
	
	public void stopMining()
	{
		this.mining = false;
	}
	
	public boolean isMining()
	{
		return this.mining;
	}

	public int getSwingTimestampDamage()
	{
		return swingTimestampDamage;
	}

	public float getSwingTimerCap()
	{
		return this.swingTimerCap;
	}
}