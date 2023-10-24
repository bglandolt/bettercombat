package bettercombat.mod.util;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class ConfigurationHandler
{
	public static Configuration config;

	public static void init( File configFile )
	{
		if ( config == null )
		{
			config = new Configuration(configFile, Integer.toString(11));
			loadConfiguration();
		}
	}
	
	/* ARROWS --------------------------------------------------------------------------------------------------------------------- */

	public static boolean addDamageSourceToTippedArrows = true;
	public static boolean blockFireProjectiles = true;
	public static boolean healingArrowNoDamage = true;
	public static float dragonboneBowWitherDamage = 12.0F;
	public static float harmingArrowAmplifier = 6.0F;
	public static float healingArrowAmplifier = 6.0F;
	public static double rangedSilverDamageMultiplier = 1.5;
	public static boolean tippedArrowFix = true;

	/* ATTRIBUTES --------------------------------------------------------------------------------------------------------------------- */

	public static double addedSwingTickCooldown = 0.5;
	public static double baseAttackDamage = 1.0;
	public static double baseAttackSpeed = 4.0;
	public static double baseKnockback = 1.0;
	public static double baseReachDistance = 4.0;
	public static int minimumAttackSpeedTicks = 8;

	/* --------------------------------------------------------------------------------------------------------------------- */

	public static ArrayList<CustomBow> bows = new ArrayList<CustomBow>();

	private static String[] bowList = {
	    "[Bow=minecraft:bow]	[Damage Multiplier=1.00]	[Velocity=0.90]	[Ticks=0]"
	};

	/* CRITICAL --------------------------------------------------------------------------------------------------------------------- */

	public static boolean autoCritOnSneakAttacks = true;
	public static double baseCritPercentChance = 0.05;
	public static double baseCritPercentDamage = 1.5;
	public static double jumpCrits = 0.1;
	public static double luckCritModifier = 0.1;

	/* DUALWIELD --------------------------------------------------------------------------------------------------------------------- */

	public static double offHandEfficiency = 0.75;
	public static boolean sneakingDisablesOffhandAttack = true;
	public static double versatileFatigueAmount = 0.3;

	/* ENCHANTMENTS --------------------------------------------------------------------------------------------------------------------- */

	public static boolean gourmandEnchantmentEnabled = true;
	public static boolean lightningEnchantmentEnabled = true;
	public static float lightningEnchantmentBaseDamage = 3.0F;
	public static float lightningEnchantmentDamagePerLevel = 2.0F;
	public static float lightningEnchantmentWetModifier = 1.5F;
	public static boolean webbingEnchantmentEnabled = true;
	public static boolean revitalizeEnchantmentEnabled = true;
	public static float revitalizePercentPerLevel = 0.2F;
	public static boolean sorceryEnchantmentEnabled = true;
	public static float sorceryPercentPerLevel = 0.1F;

	/* OTHER --------------------------------------------------------------------------------------------------------------------- */

	public static boolean moreSprint = true;
	public static double inertiaOnAttack = 0.5;
	public static boolean fastEquipHotbarOnly = false;
	public static boolean fastEquipOffhandWeaponsOrShieldsOnly = true;
	public static double extraAttackHeight = 0.75;
	public static double extraAttackWidth = 0.25;
	public static float nauseaAffectsMobs = 2.2F;
	public static double miningFatigueDamageReduction = 0.0;
	public static float silverArmorDamagesUndeadAttackers = 1.5F;
	public static boolean grassPathingRequiresAnimation = true;
	public static boolean tillingRequiresAnimation = true;
	public static boolean strippingBarkRequiresAnimation = false;
	public static boolean cancelSpartanWeaponryFatigue = true;

	/* KNOCKBACK --------------------------------------------------------------------------------------------------------------------- */

	public static boolean betterKnockback = true;
	public static double knockUpStrengthMultiplier = 0.5;
	public static double knockbackStrengthMultiplier = 1.0;
	public static double sprintingKnockbackAmount = 0.5;

	/* BWLISTS --------------------------------------------------------------------------------------------------------------------- */

	@SuppressWarnings("rawtypes")
	private static ArrayList<Class> entityBlackArray = new ArrayList<Class>();
	private static String[] entityBlacklist =
	{
	    "net.minecraft.entity.passive.EntityHorse", "net.minecraft.entity.item.EntityArmorStand", "net.minecraft.entity.passive.EntityVillager", "net.torocraft.toroquest.entities.creatures.ICitizen"
	};

	@SuppressWarnings("rawtypes")
	private static ArrayList<Class> itemClassWhiteArray = new ArrayList<Class>();
	private static String[] itemClassWhitelist =
	{
	    "net.minecraft.item.ItemSword", "net.minecraft.item.ItemTool", "net.minecraft.item.ItemHoe", "com.oblivioussp.spartanweaponry.item.ItemThrowingWeapon"
	};
	
	@SuppressWarnings("rawtypes")
	private static ArrayList<Class> itemClassBlackArray = new ArrayList<Class>();
	private static String[] itemClassBlacklist =
	{
	};
	
	private static String[] itemBlacklist =
	{

	};

	/* POTIONS --------------------------------------------------------------------------------------------------------------------- */

	public static float alchemizedAmplifier = 0.2F;
	public static double critChancePotionAmplifier = 0.1;
	public static double critDamagePotionAmplifier = 0.1;
	public static boolean customPotionEffectsWorkOnSweep = true;
	public static double strengthPotionMultiplier = 0.05;
	public static double weaknessPotionMultiplier = 0.1;
	public static double extraSplashPotionWidth = 0.0D;
	public static double extraSplashPotionHeight = 2.0D;
	public static float bleedingDamagePerTick = 0.5F;
	
	/* SOUND --------------------------------------------------------------------------------------------------------------------- */
	
		public static float bowThudSoundVolume = 1.0F;
		public static float bowStrikeSoundVolume = 1.0F;
		public static boolean playArrowHitSound = true;
		public static float shieldBashSoundVolume = 1.0F;
		public static float shieldBlockSoundVolume = 1.0F;
		public static float weaponHitSoundVolume = 1.0F;
		public static float weaponSwingSoundVolume = 1.0F;
		public static float weaponEquipAndSheatheSoundVolume = 1.0F;
		
		public static String[] nonMetalList =
		{
			"wood", "stone", "bone", "staff", "club", "caestus"
		};
		
	/* TOOLTIPS --------------------------------------------------------------------------------------------------------------------- */

		public static boolean showCritChanceTooltip = true;
		public static boolean showCritChanceTooltipAsTotal = true;
		public static boolean showCritDamageTooltip = true;
		public static boolean showCritDamageTooltipAsTotal = false;
		public static boolean showKnockbackTooltip = true;
		public static boolean showKnockbackTooltipAsTotal = true;
		public static boolean showPotionEffectTooltip = true;
		public static boolean showReachTooltip = true;
		public static boolean showReachTooltipAsTotal = false;
		public static boolean showSweepTooltip = true;
//		public static boolean removeRedundantSpartanWeaponryTooltips = true;
		
	/* UNARMED --------------------------------------------------------------------------------------------------------------------- */

		public static double fistAndNonWeaponDamageReduction = 0.5;
		public static double fistAndNonWeaponKnockbackReduction = 0.0;
		public static double fistAndNonWeaponReachReduction = 0.5;
		
	/* VISUAL --------------------------------------------------------------------------------------------------------------------- */

		public static boolean aetherealizedDamageParticles = true;
		public static float breathingAnimationIntensity = 0.03F;
		public static float breathingAnimationSpeed = 0.04F;
		public static float cameraPitchSwing = 0.09F;
		public static float rotationYawSwing = 0.18F;
		public static boolean damageParticles = true;
		public static boolean moreSweep = true;
		public static boolean showShieldCooldownCrosshair = true;

	/* AXE --------------------------------------------------------------------------------------------------------------------- */
	
		public static ArrayList<CustomAxe> axes = new ArrayList<CustomAxe>();
		private static String[] axeList =
		{
				"[Axe Substring=battleaxe_]		[Disable Duration=90]",
				"[Axe Substring=halberd_]		[Disable Duration=60]",
				"[Axe Substring=_axe]			[Disable Duration=30]"
		};
	
	/* SHIELD --------------------------------------------------------------------------------------------------------------------- */

		public static double critsDisableShield = 3.0;
		public static double parryChanceEffectivness = 3.0;
		public static boolean enableParrying = true;
		public static float parryKnockbackAmount = 0.5F;
		public static boolean disableBlockingWhileAttacking = true;
		public static boolean disableBlockingWhileShieldBashing = false;
		public static float shieldSilverDamageMultiplier = 1.5F;
		public static boolean shieldBashingTaunts = true;
		
		public static ArrayList<CustomShield> shields = new ArrayList<CustomShield>();
	
		private static String[] shieldList = {
		    "[Shield=minecraft:shield]						[Damage=3.0]	[Knockback=1.0]	[Cooldown=30]",
		    "[Shield=spartanshields:shield_basic_diamond]	[Damage=7.0]	[Knockback=2.0]	[Cooldown=25]",
		    "[Shield=spartanshields:shield_basic_gold]		[Damage=3.0]	[Knockback=1.0]	[Cooldown=10]",
		    "[Shield=spartanshields:shield_basic_iron]		[Damage=5.0]	[Knockback=1.0]	[Cooldown=20]",
		    "[Shield=spartanshields:shield_basic_obsidian]	[Damage=5.0]	[Knockback=1.5]	[Cooldown=50]",
		    "[Shield=spartanshields:shield_basic_silver]		[Damage=5.0]	[Knockback=1.0]	[Cooldown=20]",
		    "[Shield=spartanshields:shield_basic_stone]		[Damage=2.0]	[Knockback=0.5]	[Cooldown=40]",
		    "[Shield=spartanshields:shield_basic_wood]		[Damage=1.0]	[Knockback=0.5]	[Cooldown=40]",
		    "[Shield=spartanshields:shield_tower_diamond]	[Damage=7.0]	[Knockback=2.5]	[Cooldown=35]",
		    "[Shield=spartanshields:shield_tower_gold]		[Damage=3.0]	[Knockback=1.5]	[Cooldown=20]",
		    "[Shield=spartanshields:shield_tower_iron]		[Damage=5.0]	[Knockback=1.5]	[Cooldown=30]",
		    "[Shield=spartanshields:shield_tower_obsidian]	[Damage=5.0]	[Knockback=2.0]	[Cooldown=60]",
		    "[Shield=spartanshields:shield_tower_silver]		[Damage=5.0]	[Knockback=1.5]	[Cooldown=30]",
		    "[Shield=spartanshields:shield_tower_stone]		[Damage=2.0]	[Knockback=1.0]	[Cooldown=50]",
		    "[Shield=spartanshields:shield_tower_wood]		[Damage=1.0]	[Knockback=1.0]	[Cooldown=50]"
		};
		
		private static String[] twoDimensionalShieldList =
		{
		    "shield_basic_"
		};
	
	/* SWORD --------------------------------------------------------------------------------------------------------------------- */

		public static ArrayList<CustomSword> swords = new ArrayList<CustomSword>();
		private static String[] swordList =
		{
				"[Sword Substring=golden_sword]		[Additional Attack Speed=0.2]",
				"[Sword Substring=wooden_sword]		[Additional Attack Speed=0.1]"
		};
		
	/* WEAPONS --------------------------------------------------------------------------------------------------------------------- */

		public static ArrayList<CustomWeapon> weapons = new ArrayList<CustomWeapon>();
		private static String[] weaponList =
		{
			 "[Weapon Substring=pike_] [Sound Type=POLEARM] [Animation=STAB] [Property=TWOHAND] [Sweep=0] [Additional Reach=2.0] [Knockback=0.2] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=glaive_] [Sound Type=POLEARM] [Animation=SWEEP] [Property=TWOHAND] [Sweep=2] [Additional Reach=1.0] [Knockback=0.5] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=spear_] [Sound Type=POLEARM] [Animation=STAB] [Property=MAINHAND] [Sweep=0] [Additional Reach=1.0] [Knockback=0.2] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=lance_] [Sound Type=POLEARM] [Animation=STAB] [Property=MAINHAND] [Sweep=0] [Additional Reach=1.0] [Knockback=1.0] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=halberd_] [Sound Type=POLEARM] [Animation=CHOP] [Property=TWOHAND] [Sweep=0] [Additional Reach=1.0] [Knockback=0.8] [Crit Chance=0.25] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=staff] [Sound Type=POLEARM] [Animation=SWEEP] [Property=TWOHAND] [Sweep=2] [Additional Reach=1.0] [Knockback=1.0] [Crit Chance=0.1] [Additional Crit Damage=0.5] [Parry=true] [Potion Effect=CRIT,RECEIVE,minecraft:speed,2,160]",
			 "[Weapon Substring=warhammer_] [Sound Type=BLUNT] [Animation=CHOP] [Property=VERSATILE] [Sweep=0] [Additional Reach=0.0] [Knockback=1.0][Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=hammer_] [Sound Type=BLUNT] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=1.5] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true] [Potion Effect=CRIT,AFFLICT,minecraft:nausea,1,100]",
			 "[Weapon Substring=mace_] [Sound Type=BLUNT] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.8] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=club_] [Sound Type=BLUNT] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.8] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=caestus] [Sound Type=BLUNT] [Animation=STAB] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.2] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=greatsword_] [Sound Type=BLADE] [Animation=SWEEP] [Property=TWOHAND] [Sweep=3] [Additional Reach=1.0] [Knockback=0.5][Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=katana_] [Sound Type=BLADE] [Animation=CHOP] [Property=VERSATILE] [Sweep=0] [Additional Reach=0.0] [Knockback=0.5] [Crit Chance=0.1] [Additional Crit Damage=0.5] [Parry=true]",
			 "[Weapon Substring=longsword_] [Sound Type=BLADE] [Animation=SWEEP] [Property=VERSATILE] [Sweep=1] [Additional Reach=0.0] [Knockback=0.5] Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=saber_] [Sound Type=BLADE] [Animation=SWEEP] [Property=ONEHAND] [Sweep=1] [Additional Reach=0.0] [Knockback=0.2] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=rapier_] [Sound Type=BLADE] [Animation=STAB] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.0] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true] [Potion Effect=CRIT,AFFLICT,bettercombat:bleeding,1,100]",
			 "[Weapon Substring=battleaxe_] [Sound Type=AXE] [Animation=CHOP] [Property=VERSATILE] [Sweep=0] [Additional Reach=0.0] [Knockback=0.8] [Crit Chance=0.25] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=_shovel] [Sound Type=AXE] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.5] [Crit Chance=0.05] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=_sword] [Sound Type=BLADE] [Animation=SWEEP] [Property=ONEHAND] [Sweep=1] [Additional Reach=0.0] [Knockback=0.2] [Crit Chance=0.1] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=_pickaxe] [Sound Type=AXE] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.5] [Crit Chance=0.05] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=_axe] [Sound Type=AXE] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.8] [Crit Chance=0.25] [Additional Crit Damage=0.0] [Parry=true]",
			 "[Weapon Substring=_hoe] [Sound Type=AXE] [Animation=CHOP] [Property=ONEHAND] [Sweep=0] [Additional Reach=0.0] [Knockback=0.2] [Crit Chance=0.05] [Additional Crit Damage=0.0] [Parry=true]"
		};
	
	public static class CustomAxe
	{
		public String name;
		public int disableDuration;
	}
	
	public static class CustomSword
	{
		public String name;
		public float attackSpeed;
	}
	
	public enum WeaponProperty
	{
		ONEHAND, VERSATILE, MAINHAND, TWOHAND;
	}
	
	public enum Animation
	{
		NONE,
		SWEEP,
		CHOP,
		STAB,
		BLOCK;
	}
	
	public enum SoundType
	{
		NONE,
		BLADE,
		BLUNT,
		AXE,
		POLEARM;
	}

	public static class CustomWeapon
	{
		public String name;
		public Animation animation;
		public SoundType soundType;
		public WeaponProperty property;
		public int sweepMod;
		public double additionalReachMod;
		public double knockbackMod;
		public double critChanceMod;
		public double additionalCritDamageMod;
		public boolean parry;
		
		public CustomWeaponPotionEffect customWeaponPotionEffect = null;
	}
	
	public static class CustomWeaponPotionEffect
	{
		public float potionChance;
		public boolean afflict;
		private Potion potionEffect;
		public int potionPower;
		public int potionDuration;
		
//		public Potion getPotion()
//		{
//			return Potion.getPotionFromResourceLocation(this.potionEffect);
//		}
		
		public Potion getPotion()
		{
			return potionEffect;
		}
	}
	
	/* Whitelist weapons that have no config string match */
	public static CustomWeapon DEFAULT_CUSTOM_WEAPON = new CustomWeapon();
	
	public static class CustomShield
	{
		public Item shield;
		public double damage;
		public double knockback;
		public int cooldown;
	}
	
	public static class CustomBow
	{
		public Item bow;
		public double damage;
		public double velocity;
		public int additionalDrawSpeed;
	}

	private static void loadConfiguration()
	{
		/* --------------------------------------------------------------------------------------------------------------------- */
		String ARROWS = "Arrows";
		
		addDamageSourceToTippedArrows = config.getBoolean("Add Damage Source To Tipped Arrows", ARROWS, true, "If set to true, this setting fixes the bug where Tipped Arrows do not have a damage source. This fix allows Tipped Arrows to have their damage increased through player enchantments that increase Magic Damage, for example.");
		blockFireProjectiles = config.getBoolean("Block Flaming Projectiles", ARROWS, true, "If set to true, flaming projectiles & arrows not set the blocker on fire when blocked (blocking flaming projectiles normally ignites the blocker).");
		healingArrowNoDamage = config.getBoolean("Healing Arrows Deals No Damage", ARROWS, true, "Enable to have Tipped Arrows that cause 'Healing' (including the healing done from 'Harming' arrows against undead) to deal no damage.");
		dragonboneBowWitherDamage = config.getFloat("Dragonbone Wither", ARROWS, 12.0F, 0.0F, 256.0F, "The Dragonbone Arrows do the same damage as a normal arrows. This setting adds magic damage in the form of Wither to the arrows to compensate. Set to 0.0F to disable.");
		harmingArrowAmplifier = config.getFloat("Harming Arrow Amplifier", ARROWS, 6.0F, 0.0F, 256.0F, "How much damage arrows of 'Harming' deal, per level. ");
		healingArrowAmplifier = config.getFloat("Harming Arrow Amplifier", ARROWS, 6.0F, 0.0F, 256.0F, "How much health arrows of 'Healing' restore, per level. ");
		rangedSilverDamageMultiplier = config.getFloat("Ranged Silver Damage", ARROWS, 1.5F, 0.0F, 256.0F, "Damage multiplier for ranged silver weapons and arrows against undead. Set to 1.0F to disable.");
		tippedArrowFix = config.getBoolean("Tipped Arrow Fix", ARROWS, true, "Set to true to remove the following vanilla feature that makes 'Harming' & 'Healing' tipped arrows essentially useless: 'Arrows of Harming' (and 'Arrows of Healing' when used against undead mobs) do not add any amount of damage to the arrow. Instead, the arrow damage is first calculated, then checked to see if it is below 12 damage. If the arrow's damage is less than 12, the 'Harming' effect of the arrow makes up the difference to ensure the arrow does exactly 12 damage. Therefore, an unenchanted bow cannot deal more than 12 damage using 'Harming' (or 'Healing' to undead) arrows. However, if the arrow would deal more than 12 damage, the harming effect is entirely neutralized. This means that bows enchanted with Power I through Power III very rarely utilize the arrow at full charge, and any Power level above III never utilizes 'Arrows of Harming' effectively at full charge when against unarmored mobs/players.");
		/* --------------------------------------------------------------------------------------------------------------------- */
		String ATTRIBUTES = "(Advanced settings) Attributes";

		addedSwingTickCooldown = config.get(ATTRIBUTES, "Added Attack Speed Tick Cooldown", 0.5, "Add partial ticks to all attack swing cooldown. This defaults to 0.5, which rounds up all swing cooldowns. A swing cooldown of 12.5 ticks would instead become 13 ticks.").getDouble();
		baseAttackDamage = config.get(ATTRIBUTES, "Attribute Base Attack Damage", 1.0, "Base ATTACK_DAMAGE attribute for the player. Vanilla is 1.0F. If you want to nerf unarmed & non-weapon damage, instead adjust the setting 'Unarmed & Non-Weapon Damage'.").getDouble();
		baseAttackSpeed = config.get(ATTRIBUTES, "Attribute Base Attack Speed", 4.0, "Base ATTACK_SPEED attribute for the player. Vanilla is 4.0F. This value only affects non-weapons, such as fists. An attack speed of 4.0 means 4 attacks per second. Higher values mean faster attack swing recovery. Attack speed formula    -->    20 / attackSpeed. 1.6 attack speed takes 12.5 ticks to recover after an attack swing, which means 1.6 attacks per second. 0.8 means 25 ticks to recover after an attack swing. 4.0 means 5 ticks to recover after an attack. It is best if you do not adjust this value!").getDouble();
		baseCritPercentChance = config.get(ATTRIBUTES, "Base Critical Percent Chance", 0.05, "Base chance of landing a critical strike with an attack. Default 0.05, meaining 5% critical strike chance. Set to a negative number to disable random crit chance and return to the vanilla ways.").getDouble();
		baseCritPercentDamage = config.get(ATTRIBUTES, "Base Critical Percent Damage", 1.5, "How much damage crits do (multiplier). Default 1.5F, meaning crits do 50% extra damage (150% of total damage).").getDouble();
		baseReachDistance = config.get(ATTRIBUTES, "Attribute Base Reach Distance", 4.0, "Base REACH_DISTANCE attribute for the player. Vanilla is 4.0F in singleplayer, and increased to 5.0F in multiplayer to account for latency. This reach distance config is only for attacking distance, not mining distance. Set to 0.0 to disable this setting completely and instead use the vanilla reach distance value instead.").getDouble();
		minimumAttackSpeedTicks = config.getInt("Minimum Attack Speed Tick Cooldown", ATTRIBUTES, 8, 5, 20, "The minimum cooldown (in ticks) for an attack swing cooldown. The default for this setting is 8, which means the minimum attack speed cooldown is 8 ticks. This translates to the maximum attack speed of a weapon being 2.5 and any attack speed value higher than 2.5 will not affect how fast the weapon attacks (unless the player attack speed is slowed or affected by mining fatigue, then higher attack speed values counteract the debuff).");
		/* --------------------------------------------------------------------------------------------------------------------- */
		String CRITICAL = "Critical Strikes";
		
		autoCritOnSneakAttacks = config.getBoolean("Auto Critical On Sneak Attacks", CRITICAL, true, "Automatically critical strike a mob when you are not their attack target or revenge target.");
		critsDisableShield = config.get(CRITICAL, "Critical Strikes Disable Shields", 3.0, "Critical strikes with any weapon disable shields, similar to how axes disable shields for 100 ticks. The shield disable duration is based off of damage, for example, having this setting at 3.0, an attack dealing 7.0 damage to a blocker would disable a shield for 21 ticks. Set to 0.0 to disable this feature.").getDouble();
		jumpCrits = config.get(CRITICAL, "Add Critical Chance On Falling", 0.1, "Adds additional critical strike chance when the player is falling, like in vanilla. Set to 0.0F to disable.").getDouble();
		luckCritModifier = config.get(CRITICAL, "Luck Affects Critical Chance", 0.1, "This setting allows the LUCK attribute to affect critical strike chance. Formula = ( LUCK * luckCritModifier ). If luckCritModifier is 0.1 then 2 LUCK would give 20% crit chance. Set to 0.0 to disable.").getDouble();
		/* --------------------------------------------------------------------------------------------------------------------- */
		String DUALWIELD = "Dual-Wielding";
		
		offHandEfficiency = config.get(DUALWIELD, "Offhand Efficiency", 0.75, "The percent efficiency of an attack with offhanded weapon    -->   damage * efficiency.").getDouble();
		sneakingDisablesOffhandAttack = config.getBoolean("Sneaking Disables Offhand Attack", DUALWIELD, true, "If set to true, attacking with your offhand is disabled while sneaking. The purpose of this is to add compatibility to mods such as 'CarryOn' or 'Effortless Building' where you need to use certian right-click functionality.");
		versatileFatigueAmount = config.get(DUALWIELD, "Versatile Fatigue Amount", 0.3, "How much of a slow to attack speed is applied when dual-wielding with one VERSATILE weapon. Default 0.3, which translates to a 30% slower attack speed. Dual-weilding with two VERSATILE weapons will slow attack speed by 45%.").getDouble();
		miningFatigueDamageReduction = config.get(DUALWIELD, "Mining Fatigue Damage Reduction", 0.0, "Reduces the damage dealt of an attack per level of Mining Fatigue. For example, if this setting is set to 0.25, then Mining Fatigue II reduces player damage by 50%. Set 0.0 to disable.").getDouble();
		/* --------------------------------------------------------------------------------------------------------------------- */
		String ENCHANTMENTS = "Enchantments";
		
		gourmandEnchantmentEnabled = config.getBoolean("Gourmand Enchantment Enabled", ENCHANTMENTS, true, "Set to true to enable the Gourmand enchantment.");
		lightningEnchantmentEnabled = config.getBoolean("Lightning Enchantment Enabled", ENCHANTMENTS, true, "Set to true to enable the Lightning enchantment.");
		lightningEnchantmentBaseDamage = config.getFloat("Lightning Enchantment Base Damage", ENCHANTMENTS, 3.0F, 0.0F, 256.0F, "Lightning enchantment base damage. A base of 3 and 2 per level means Lightning I deals 5 damage.");
		lightningEnchantmentDamagePerLevel = config.getFloat("Lightning Enchantment Damage Per Level", ENCHANTMENTS, 2.0F, 0.0F, 256.0F, "Lightning enchantment damage per level. A base of 3 and 2 per level means Lightning V deals 13 damage.");
		lightningEnchantmentWetModifier = config.getFloat("Lightning Enchantment Wet Modifier", ENCHANTMENTS, 1.5F, 0.0F, 256.0F, "This number is multiplied by the total lightning damage when targets are wet. 1.5F means 50% more damage.");
		revitalizeEnchantmentEnabled = config.getBoolean("Revitalize Enchantment Enabled", ENCHANTMENTS, true, "Set to true to enable the Revitalize enchantment.");
		revitalizePercentPerLevel = config.getFloat("Revitalize Healing Percentage Per Level", ENCHANTMENTS, 0.2F, 0.0F, 16.0F, "The damage multipler for the Revitalize enchantment, per level. 0.2F means a 20% increase in healing received per level.");
		sorceryEnchantmentEnabled = config.getBoolean("Sorcery Enchantment Enabled", ENCHANTMENTS, true, "Set to true to enable the Sorcery enchantment.");
		sorceryPercentPerLevel = config.getFloat("Sorcery Damage Percentage Per Level", ENCHANTMENTS, 0.1F, 0.0F, 16.0F, "The damage multipler for the Sorcery enchantment, per level. 0.1F means a 10% increase in magic damage per level.");
		webbingEnchantmentEnabled = config.getBoolean("Webbing Enchantment Enabled", ENCHANTMENTS, true, "Set to true to enable the Webbing enchantment.");
		/* --------------------------------------------------------------------------------------------------------------------- */
		String OTHER = "Other";
		
		moreSprint = config.getBoolean("Attack & Sprint", OTHER, true, "Attacking an enemy while sprinting will no longer interrupt your sprint.");
		inertiaOnAttack = config.get(OTHER, "Intertia on Attack", 0.5, "Multiplies the player speed by this amount when they swing a weapon. Set to 1.0 to disable.").getDouble();
		fastEquipHotbarOnly = config.getBoolean("Fast Equip Hotbar Only", OTHER, false, "Set to true to have Fast Equip use only items in the hotbar instead of the entire inventory. The Fast Equip keybind is set to X, check the minecraft keybind settings to change this.");
		fastEquipOffhandWeaponsOrShieldsOnly = config.getBoolean("Fast Equip Offhand Weapons Or Shields Only", OTHER, false, "Set to true to have Fast Equip only find weapons or shields, and not tools, for the offhand. Tools will still be found and equipped in the mainhand if no weapons are found. The Fast Equip keybind is set to X, check the minecraft keybind settings to change this.");
		extraAttackWidth = config.get(OTHER, "Extra Attack Width", 0.25, "How wide, on all directions, the hitbox will be extended for melee attacks. Increase the value of this setting to make the XZ-accuracy of attacks more forgiving. 1.0 is equal to an additional block width on XZ sides.").getDouble();
		extraAttackHeight = config.get(OTHER, "Extra Attack Height", 0.75, "How high the hitbox will be extended for melee attacks. Increase the value of this setting to make the Y-accuracy of attacks more forgiving. 1.0 is equal to an additional block height above the entity.").getDouble();
		nauseaAffectsMobs = config.getFloat("Nausea Affects Mobs", OTHER, 2.2F, 0.0F, 256.0F, "Setting to have Nausea & Blindness affect mobs. If a mob has a height equal to or less than this value, it will be affected by Blindess and Nausea potion effects, which causes them to often lose their target and stumble around. Set to 0.0F to disable.");
		silverArmorDamagesUndeadAttackers = config.getFloat("Silver Armor Damage", OTHER, 1.5F, 0.0F, 256.0F, "Thorns, but for undead. How much damage undead attackers will take against an entity wearing Silver armor per piece of Silver armor. Setting this to 1.5F and wearing 2 pieces of Silver armor deals 3.0F damage to undead attackers. Silver weapons get a damage bonus, so why not add a special interaction with Silver armor? Set to 0.0F to disable.");
		grassPathingRequiresAnimation = config.getBoolean("Digging Grass Paths Requires Animation", OTHER, true, "If set to true, shoveling grass paths requires an animation. Shovels with a faster attack speed will path land faster.");
		tillingRequiresAnimation = config.getBoolean("Tilling Land Requires Animation", OTHER, true, "If set to true, tilling land requires an animation. Hoes with a faster attack speed till land faster.");
		strippingBarkRequiresAnimation = config.getBoolean("Stripping Bark Requires Animation", OTHER, false, "CURRENTLY NOT WORKING. If set to true, stripping bark requires an animation. Axes with a faster attack speed strip bark faster. Enable this setting if you have future MC installed which allows stripping of bark.");
		cancelSpartanWeaponryFatigue = config.getBoolean("Cancel Spartan Weaponry Fatigue", OTHER, true, "Set to true to disable weapon fatigue from Spartan Weaponry, this mod instead handles two-handed and versatile weapons.");

		/* --------------------------------------------------------------------------------------------------------------------- */
		String KNOCKBACK = "Knockback";
		
		betterKnockback = config.getBoolean("Better Knockback", KNOCKBACK, true, "Enable have the KNOCKBACK_RESISTANCE attribute reduce the strength of knockback effects, rather than reducing the chance to not be knocked back. (For example: by default, a knockback_resistance of 0.5 means a 50% chance to not be knocked back from an attack. However, if this setting is true, a knockback_resistance of 0.5 means the distance or effects of being knocked back are 50% less far or powerful.");
		knockUpStrengthMultiplier = config.get(KNOCKBACK, "knock Up Strength Multiplier", 0.5, "Multiply the motionY amount of knockback by this amount. Set to 0.5 by default to reduce the motionY by 50%. Does nothing if betterKnockback is disabled.").getDouble();
		knockbackStrengthMultiplier = config.get(KNOCKBACK, "knockback Strength Multiplier", 1.0, "Multiply the motionXZ amount of knockback by this amount. This setting is pretty sensitive, and values below 0.5 have almost no visible knockback.").getDouble();
		sprintingKnockbackAmount = config.get(KNOCKBACK, "Sprinting Knockback Amount", 0.5, "Increases the knockback of an attack when sprinting. For example, setting this to 2.0F is the same knockback amount as Knockback II.").getDouble();
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String PARRYING = "Parrying";
		
		parryChanceEffectivness = config.get(PARRYING, "Parry Chance Effectivness", 3.0, "The attack damage of a weapon is used to calculate the chance of parrying an attack. This setting counts as a multiplier to attack damage for determining the chance to parry an attack. The higher the attack damage of the parry weapon, the higher chances of parrying. This increases the effectivness of parrying with two-handed weapons, which often have higher attack damage. This method also lets weapon material tiers scale to difficulty. Higher damage incoming attacks have a lower the chance of parried. For example, if this setting is set at 3 and the parrying weapon has 5 attack damage, an incoming attack of 9 damage has a 9/(3*5) or 60% chance of NOT being parried.").getDouble();
		parryKnockbackAmount = config.getFloat("Parry Knockback Amount", PARRYING, 0.5F, 0.0F, 1.0F, "How far both the attacker and victim are knockbacked after a parry.");
		enableParrying = config.getBoolean("Enable Parrying", PARRYING, true, "Set to false to disable all parrying mechanics from this mod. If enabled, you must specify which weapons can and can not parry in the Custom Weapon Tweaker config.");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String POTIONS = "Potions";
		
		alchemizedAmplifier = config.getFloat("Aetherealized Potion Damage", POTIONS, 0.2F, 0.0F, 1.0F, "The percentage of your physical damage that is converted to magical by the Aetherealized potion, per level. 0.2 means 20% of your physical damage (per level) is converted to magic damage.");
		critChancePotionAmplifier = config.get(POTIONS, "Percision Potion Chance", 0.1, "The additive percent increase to your critical strike chance that the Percision potion adds, per level. 0.1 means add 10% critical strike chance (per level) to your base critical strike chance.").getDouble();
		critDamagePotionAmplifier = config.get(POTIONS, "Brutality Potion Damage", 0.1, "The additive percent increase to your critical strike damage that the Brutality potion adds, per level. 0.1 means add 10% critical strike damage (per level) to your base critical strike damage.").getDouble();
		customPotionEffectsWorkOnSweep = config.getBoolean("Weapon Potion Effects Work With Sweep", POTIONS, true, "Enable have custom Potion Effects from custom weapons work with Sweep and apply on swept targets.");
		strengthPotionMultiplier = config.get(POTIONS, "Strength Potion Damage", 0.05, "Changes the Strength potion to be a percentage damage increase instead of a flat increase. If set to 0.05, then a Potion of Strength (+3 Attack Damage) will instead increase damage dealt by 15%. Set to 0.0 to disable.").getDouble();
		weaknessPotionMultiplier = config.get(POTIONS, "Weakness Potion Damage", 0.1, "Changes the Weakness potion to be a percentage damage decrease instead of a flat decrease. If set to 0.1, then a Potion of Weakness (-4 Attack Damage) will instead decrease damage dealt by 40%. Set to 0.0 to disable.").getDouble();
		extraSplashPotionWidth = config.get(POTIONS, "Extra Splash Potion Width", 0.0, "Additional splash potion width.").getDouble();
		extraSplashPotionHeight = config.get(POTIONS, "Extra Splash Potion Height", 0.2, "Additional splash potion height.").getDouble();
		bleedingDamagePerTick = config.getFloat("Bleeding Damage Per Tick", POTIONS, 0.5F, 0.0F, 256.0F, "The amount of damage bleeding does, per level, per second.");

		/* --------------------------------------------------------------------------------------------------------------------- */
		String SOUND = "Sound";
		
		bowThudSoundVolume = config.getFloat("Bow Thud Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the 'thud' sound that plays when you land a fully-charged hit.");
		bowStrikeSoundVolume = config.getFloat("Bow Strike Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the 'strike' sound that plays when you land a hit.");
		weaponSwingSoundVolume = config.getFloat("Weapon Swing Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the sound when you swing your weapon.");
		weaponHitSoundVolume = config.getFloat("Weapon Hit Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the sound that plays when you land a weapon hit.");
		weaponEquipAndSheatheSoundVolume = config.getFloat("Weapon Equip and Sheate Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the sound that plays when you equip or sheathe a weapon.");
		shieldBashSoundVolume = config.getFloat("Shield Bash Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the sound that plays when you shield bash.");
		shieldBlockSoundVolume = config.getFloat("Shield Block Sound Volume", SOUND, 1.0F, 0.0F, 2.0F, "The volume of the sound that plays when block with a shield.");
		playArrowHitSound = config.getBoolean("Arrow Impact Sound", SOUND, true, "Arrows will make an impact sound when they hit an entity, regardless of range.");
		nonMetalList = config.getStringList("Non Metal List", SOUND, nonMetalList, "Weapons that are considered non-metal for swinging and hitting sound purposes. If the weapon contains the string, such as 'wood' or 'stone' it will not make a metal sound.");
		/* --------------------------------------------------------------------------------------------------------------------- */
		String TOOLTIPS = "Tooltips";
		
		showCritChanceTooltip = config.getBoolean("Show Crit Chance Tooltip", TOOLTIPS, true, "If set to true, display Critical Chance on weapon tooltips.");
		showCritChanceTooltipAsTotal = config.getBoolean("Show Crit Chance Tooltip As Total", TOOLTIPS, true, "If set to true, display Critical Chance as the total weapon Critical Chance (25% Critical Chance), instead of additional Critical Chance (+15% Critical Chance).");
		showCritDamageTooltip = config.getBoolean("Show Crit Damage Tooltip", TOOLTIPS, true, "If set to true, display Critical Damage on weapon tooltips.");
		showCritDamageTooltipAsTotal = config.getBoolean("Show Crit Damage Tooltip As Total", TOOLTIPS, false, "If set to true, display Critical Chance as the total weapon Critical Damage (200% Critical Damage), instead of additional Critical Damage (+50% Critical Damage).");
		showPotionEffectTooltip = config.getBoolean("Show Potion Effect Tooltip", TOOLTIPS, true, "If set to true, display custom Potion Effects on weapon tooltips.");
		showKnockbackTooltip = config.getBoolean("Show Knockback Tooltip", TOOLTIPS, true, "If set to true, display Knockback amount on weapon tooltips.");
		showKnockbackTooltipAsTotal = config.getBoolean("Show Knockback Tooltip As Total", TOOLTIPS, true, "If set to true, display Knockback as the total weapon Knockback (1.5 Knockback), instead of additional Reach Distance (+0.5 Knockback).");
		showReachTooltip = config.getBoolean("Show Reach Tooltip", TOOLTIPS, true, "If set to true, display Reach Distance on weapon tooltips. For advanced modpack makers, if another mod is adding Reach Distance on a weapon, I recommend blanking out the fields you want gone in the lang file of that mod using Resource Loader.");
		showReachTooltipAsTotal = config.getBoolean("Show Reach Tooltip As Total", TOOLTIPS, false, "If set to true, display Reach Distance as the total weapon Reach Distance (5.0 Reach Distance), instead of additional Reach Distance (+1.0 Reach Distance).");
		showSweepTooltip = config.getBoolean("Show Sweep Tooltip", TOOLTIPS, true, "If set to true, display the Sweep tooltip. For advanced modpack makers, if another mod is adding Reach Distance on a weapon, I recommend blanking out the fields you want gone in the lang file of that mod using Resource Loader.");
//		removeRedundantSpartanWeaponryTooltips = config.getBoolean("Remove Redundant Spartan Weaponry Tooltips", TOOLTIPS, true, "If set to true, tooltips from Spartan Weaponry (such as Two-Handed, Reach, and Sweep) will be removed. These Spartan Weaponry tooltips are instead added through this mod and the Custom Weapon config setting.");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String UNARMED = "Unarmed";
		
		fistAndNonWeaponDamageReduction = config.get(UNARMED, "Unarmed & Non-Weapon Damage Reduction", 0.5, "Reduce damage from unarmed and non-weapon attacks. The damage of unarmed and non-weapons is reduced by this amount. The reason for this, is that unarmed and non-weapon attacks technically have a higher DPS than wooden tools when 'Hurt Resistant Time' is removed, so unarmed and non-weapon damage should be lowered.", 0.0, baseAttackDamage).getDouble();
		fistAndNonWeaponKnockbackReduction = config.get(UNARMED, "Unarmed & Non-Weapon Knockback Reduction", 0.0, "Reduce the knockback from unarmed and non-weapon attacks. The knockback of unarmed and non-weapons is reduced by this amount.", 0.0, baseKnockback).getDouble();
		fistAndNonWeaponReachReduction = config.get(UNARMED, "Unarmed & Non-Weapon Reach Reduction", 0.5, "Reduce the attack range from fists and non-weapon attacks. The range of unarmed and non-weapons is reduced by this amount.", 0.0, 4.0).getDouble();
		/* --------------------------------------------------------------------------------------------------------------------- */
		String VISUAL = "Visual";

		aetherealizedDamageParticles = config.getBoolean("Aetherealized Damage Particles", VISUAL, true, "Enable to have the Aetherealized potion create a ring of particles around the target when struck.");
		breathingAnimationIntensity = config.getFloat("Breathing Animation Intensity", VISUAL, 0.025F, 0.0F, 1.0F, "How fast items move up and down for the breathing animation.");
		breathingAnimationSpeed = config.getFloat("Breathing Animation Speed", VISUAL, 0.036F, 0.0F, 1.0F, "How far items move up and down for the breathing animation.");
		showShieldCooldownCrosshair = config.getBoolean("Show Shield Cooldown Crosshair", VISUAL, true, "Show the shield cooldown crosshair, similar to the dual-wielding crosshairs.");
		cameraPitchSwing = config.getFloat("Camera Pitch Swing", VISUAL, 0.09F, 0.0F, 1.0F, "How much your camera pitch moves when you swing a weapon. May cause slight motion sickness if set too high. Set to 0.0F to disable.");
		rotationYawSwing = config.getFloat("Rotation Yaw Swing", VISUAL, 0.18F, 0.0F, 1.0F, "How much your camera yaw moves when you swing a weapon. May cause slight motion sickness if set too high. Set to 0.0F to disable.");
		damageParticles = config.getBoolean("Damage Particles", VISUAL, true, "Enable to show heart damage particles (this is a vanilla feature, this option is here for those who wish to disable it).");
		moreSweep = config.getBoolean("More Attack Hit Particles", VISUAL, true, "Add sweep particles on attack (looks good, reccommend you keep this as true).");

		/* --------------------------------------------------------------------------------------------------------------------- */
		String AXE = "Axes";

		axeList = config.getStringList("Axe Tweaker", AXE, axeList, "Adjust the axes/weapons that have are able to disable shields."
				+ "[Axe Substring=battleaxe_]		[Disable Duration=90]\n\n"
				
				+ "The [Axe Substring] field can be simply 'battleaxe_' so that all weapons containing the word 'battleaxe_' will be affected, such as 'spartanweaponry:battleaxe_wooden' or 'spartanweaponry:battleaxe_'\n"
				
				+ "The [Disable Duration] field is how long a shield will be disabled for if struck by this axe/weapon.'\n");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String BOW = "Bows";

		bowList = config.getStringList("Bow Tweaker", BOW, bowList,
			
			  "Adjust the base damage and velocity of all arrows shot from specific bows, or adjust the draw speed ticks of bows. Example below:\n\n"

			+ "[Bow=minecraft:bow]    [Damage Multiplier=1.00]    [Velocity Multiplier=0.90]    [Ticks=0]\n\n"
			
			+ "[Bow] must include both the resource domain and path for the bow, such as 'minecraft:bow' or 'spartanweaponry:longbow_iron'\n"

			+ "[Damage Multiplier] multiplies the damage of arrows.\n"
			+ "The base damage of an arrow is 2, and a fully charged shot deals between 8 and 10 damage.\n"
			+ "The formula for the final damage of a bow (using a vanilla flint arrow with 2 baseDamage) is    -->    (baseVelocity * baseDamage]) + random.nextInt(baseVelocity * baseDamage / 2) + 2    -->    (~3 * 2) + random.nextInt(~3 * 2 / 2) + 2\n"
			+ "For example, if you set [Damage Multiplier] to 1.5 (meaning the damage of a vanilla flint arrow would now be 3 instead of 2) the final damage of that fully-charged shot would be 11 to 14.5 damage.\n"

			+ "[Velocity Multiplier] multiplies the velocity of arrows.\n"
			+ "The velocity of a fully-charged arrow (meaning a critical, with a trail of particles) averages 3.\n"
			+ "For example, if you set [Velocity] to 1.2 (meaning the velocity of all arrows shot from this bow is now 3.6 instead of 3) the final damage of that fully-charged shot (with a base damage of 2) would be 9.2 to 11.8 damage. Velocity affects damage!\n"
			+ "The following entry    -->    [Bow=spartanweaponry:crossbow_wood]    [Damage Multiplier=0.80]    [Velocity=1.25]    [Ticks=0]    -->    would cut the base damage of crossbow by 20%, but increase the velocity of the arrows/bolts shot by it by 25%.\n"
			+ "This entry would keep the damage of a fully-charged shot roughly the same, however, the velocity would be greatly increased.\n"
			+ "The maximum velocity multiplier is 1.5, anything over this value will do nothing! The reason being is that the arrow travels too fast, leading to visual anomalies.\n"
			+ "If the arrows look like they are swerving off to the side, it means the velocity is too high and you'll have to lower it.\n"
			+ "Longbows from SpartanWeaponry already have a velocity multiplier of 1.2, try not to increase velocity much more.\n"
			+ "Crossbows from SpartanWeaponry already have a velocity multiplier of 1.5, do not increase it any further!\n"
			+ "SpartanWeaponry now has a config that fuctions very similar to this one, so use this bow tweaker config for other mods that add bows or for editing the vanilla bow.\n"

			+ "[Ticks] adds or subtracts ticks from the draw speed of bows. The draw speed in ticks of a vanilla bow is 20.\n"
			+ "Setting [Ticks] to 20 would Float the amount of time it takes to fully draw back a vanilla bow.\n"
			+ "Setting [Ticks] to -10 would reduce the amount of time it takes to fully draw back a vanilla bow by half.\n\n"

			+ "More info on bows can be found here   ->   https://minecraft.fandom.com/wiki/Bow#Weapon");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String WEAPON = "Custom Weapons";

		weaponList = config.getStringList("Custom Weapon Tweaker", WEAPON, weaponList,
			  "Adjust the properties of melee weapons, such as animation, property, sweep, reach, critical strike chance, critical strike damage, and on-hit potion effects. The [Potion Effect] field is optional. Example below:\n\n"
				
			+ "[Weapon Substring=pike_]    [Sound Type=POLEARM]    [Animation=STAB]    [Property=TWOHAND]    [Sweep=0]    [Additional Reach=1.0]    [Crit Chance=10]    [Additional Additional Crit Damage=0]    [Potion Effect=minecraft:poison,1,60]\n\n"

			+ "The [Weapon Substring] field can be simply 'pike_' so that all weapons containing the word 'pike_' will be affected, such as 'spartanweaponry:pike_wooden' or 'spartanweaponry:pike_iron'\n"
			+ "If you would rather adjust properties of a specific weapon, you may put the resource path such as 'glaive_diamond' (do not include the resource domain such as 'spartanweaponry:')\n"
			+ "The order in this list is important, as it is greedy and will find the first string match! The instance [Weapon Substring=glaive_diamond] should be above the instance [Weapon Substring=pike_] for example.\n\n"

			+ "The [Sound Type] field takes the values BLADE, BLUNT, AXE, POLEARM and changes how the weapon sounds.\n"
			+ "BLADE should be assigned to bladed weapons such as swords, sabers, and rapiers.\n"
			+ "BLUNT should be assigned to heavy blunt weapons such as maces, warhammers, and hammers.\n"
			+ "AXE should be assigned to weapons such as axes, battleaxes, and pickaxes, and shovels, and hoes.\n"
			+ "POLEARM should be assigned to all polearms, such as spears, pikes, glaives, and staves.\n\n"
			
			+ " The [Animation] field takes the values SWEEP, CHOP, or STAB and changes the attack animations of weapons.\n"
			+ "SWEEP is an animation that sweeps across the lower screen. Additionally, it increases the width of the attack's hitbox based off the weapon's sweep level. This animation is best on weapons with [Sweep].\n"
			+ "CHOP is very similar to minecraft's default attack animation, and works well with weapons such as axes, hammers, or pickaxes.\n"
			+ "STAB is an animation that rotates the weapon and then extends it forwards. This animation is best suited for weapons such as spears, pikes, rapiers, and lances.\n\n"

			+ "The [Sweep] field adds sweep to a weapon.\n"
			+ "Set [Sweep] to 0 to disable sweep on a weapon.\n"
			+ "Set [Sweep] to 1 to have that weapon deal 1.0 damage to sweep targets.\n"
			+ "Set [Sweep] to 2 to have that weapon deal 25% damage to sweep targets.\n"
			+ "Set [Sweep] to 3 to have that weapon deal 50% damage to sweep targets.\n"
			+ "Set [Sweep] to 4 to have that weapon deal 75% damage to sweep targets.\n"
			+ "Set [Sweep] to 5 to have that weapon deal 100% of the damage dealt to the main target to the other sweep targets.\n"
			+ "For each level of sweep above 1, the weapon deals 25% of the main target damage to sweep targets, with a minimum of 1.0 damage.\n"
			+ "A sweep level higher than 5 (through this config or through the Sweeping enchantmet) does not do any sweep damage, however, it still increases the sweep radius.\n\n"
			
			+ "The [Property] field takes the values ONEHAND, VERSATILE, MAINHAND, or TWOHAND.\n"
			+ "ONEHAND can attack in either hand and incur no fatigue penalties.\n"
			+ "VERSATILE can attack in either hand, however, incurs a fatigue penalty if both hands are occupied.\n"
			+ "MAINHAND can only attack in the main hand. This is used for weapons such as spears, where a shield/ spear combination makes sense but dual-weilding spears does not.\n"
			+ "TWOHAND disables the other hand when it is equipped.\n\n"

			+ "The [Additional Reach] field is the reach distance for weapons.\n"
			+ "Setting [Additional Reach] to 1.0 for a weapon would mean a reach distance of 5.0 (1.0 + base reach distance) blocks for that weapon.\n\n"

			+ "The [Crit Chance] field is the critical chance for weapons.\n"
			+ "Setting [Crit Chance] to 0.1 for a weapon would mean critical strike chance is 10%.\n\n"
			
			+ "The [Additional Crit Damage] field is the critical damage multiplier for weapons.\n"
			+ "Setting [Additional Crit Damage] to 50 for a weapon would mean critical strike damage deals an additional 50% damage.\n\n"

			+ "The [Parry] field enables or disables parrying.\n"
			+ "The setting 'Disable Parrying' must be set to false in the config to allow weapons to parry.\n"
			+ "Setting [Parry] to true allows the weapon to parry, and setting [Parry] to false disables parrying for that weapon.\n\n"

			+ "The [Potion Effect] field is optional! It gives weapons a chance to apply a potion effect when you attack an entity with a weapon.\n"
			+ "Only one potion effect is supported. The order goes for the potion effect  ->  chance, afflict/receive, resource location, power, duration.\n"
			+ "'Chance on hit:' can be instead be changed to on 'Critical Strike:' if chance is set to CRIT or 0.0. This setting allows for some interesting crit builds.\n"
			+ "Setting afflict/receive to AFFLICT causes the victim to gain the potion effect. Setting afflict/receive to RECEIVE causes the attacker gain the potion effect.\n"
			+ "Duration is in ticks. There are 20 ticks per second. A duration of 100 would apply the potion effect for 5 seconds.\n\n"
			
			+ "[Potion Effect=1.0,AFFLICT,minecraft:poison,2,600] -> 100% Chance on hit: Afflict Poison II for 30 seconds.\n"
			+ "[Potion Effect=0.5,AFFLICT,minecraft:instant_damage,3,0] -> 50% Chance on hit: Afflict Harming III.\n"
			+ "[Potion Effect=0.1,AFFLICT,minecraft:slowness,1,100] -> 10% Chance on hit: Afflict Slowness I for 5 seconds.\n"
			+ "[Potion Effect=0.2,RECEIVE,minecraft:swiftness,2,160] -> 20% Chance on hit: Receive Swiftness II for 8 seconds.\n"
			+ "[Potion Effect=0.01,RECEIVE,minecraft:instant_health,2,0] -> 1% Chance on hit: Receive Instant Health II.\n"
			+ "[Potion Effect=CRIT,AFFLICT,bettercombat:bleeding,1,100] -> Critical strike: Afflict Bleeding I for 5 seconds.\n"
			+ "[Potion Effect=CRIT,RECEIVE,bettercombat:brutality,1,200] -> Critical strike: Receive Brutality I for 10 seconds.\n");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String SHIELD = "Custom Shields";
		
		shieldBashingTaunts = config.getBoolean("Shield Bashing Taunts", SHIELD, true, "Shield bashing an enemy taunts them, setting the attacker as the attack target.");
		
		disableBlockingWhileAttacking = config.getBoolean("Disable Blocking While Attacking", SHIELD, true, "Disable blocking while attacking with the mainhand.");

		disableBlockingWhileShieldBashing = config.getBoolean("Disable Blocking While Shield Bashing", SHIELD, false, "Disable blocking while shield bash is on cooldown.");

		shieldSilverDamageMultiplier = config.getFloat("Shield Silver Damage", SHIELD, 1.5F, 0.0F, 256.0F, "Multiplier for silver shield bash against undead. Set to 1.0F to disable.");

		shieldList = config.getStringList("Shield Tweaker", SHIELD, shieldList,
		
			"Adjust the damage, knockback amount, and cooldown on shield bashing. Example below:\n\n"
					
			+ "\n\n[Shield=minecraft:shield]    [Damage=1.0]    [Knockback=1.0]    [Cooldown=30]\n\n"
			
			+ "The [Shield] field must include both the resource domain and path for the shield, such as 'minecraft:shield' or 'spartanshields:shield_basic_wood'\n"
			+ "The [Damage] field is clamped be between 0.0 and 326.0, and is the amount of damage bashing with a shield does to a target.\n"
			+ "The [Knockback] field is clamped between 0.0 and 326.0 and determines how far back the enemy is thrown when shield bashed.\n"
			+ "The [Cooldown] field is clamped between 0 and 255, and is how long the shield is put on cooldown after a shield bash.");
		
		twoDimensionalShieldList = config.getStringList("2D Shield String List", SHIELD, twoDimensionalShieldList, "2D shields list. This setting helps position 2D shields from other mods correctly.");

		/* --------------------------------------------------------------------------------------------------------------------- */
		String SWORD = "Swords";

		swordList = config.getStringList("Sword Tweaker", SWORD, swordList,
		
		  "Adjust the Attack Speed of vanilla swords.\n\n"
		+ "This is setting only for vanilla swords! The mod 'Material Changer' allows you change the Attack Speed on all weapons (except swords).\n"
		+ "If a mod does not allow you to adjust the Attack Speed on their custom modded swords, you may use this config to do so.\n"
		
		+ "The [Sword Substring] field can be simply '_sword' so that all weapons containing the word '_sword' will be affected, such as 'minecraft:diamond_sword' or 'minecraft:iron_sword'\n"
		+ "If you would rather adjust properties of a specific sword, you may put the resource path such as 'diamond_sword' (do not include the resource domain such as 'minecraft:')\n"
		+ "The order in this list is important, as it is greedy and will find the first string match! The instance [Sword Substring=diamond_sword] should be above the instance [Sword Substring=_sword] for example.\n\n"
		
		+ "The [Additional Attack Speed=0.2] field is a value added to the sword Attack Speed.\n"
		+ "For example, if this field is set to 0.2, a matched sword would have the Attack Speed value increased by 0.2.\n"
		+ "If that sword had an Attack Speed of 1.6, it would become 1.8. This field can also accept negative values, such as -0.6.");
		
		/* --------------------------------------------------------------------------------------------------------------------- */
		String BWLISTS = "White/Black Lists";

		itemClassWhitelist = config.getStringList("Item Class Whitelist", BWLISTS, itemClassWhitelist, "Whitelisted item classes for attacking. If an item is added to this list, it will function as an Immersive Combat weapon. The Custom Weapons config is for editing the values and attributes of weapons. The class  net.minecraft.item.ItemSword  and anything that extends it is added by default.");
		itemClassBlacklist = config.getStringList("Item Class Blacklist", BWLISTS, itemClassBlacklist, "Blacklisted item classes (Advanced setting; requires you to look through the source code of the mod that you are trying to add the class from). If an item is added to this list, it will have the default left-click and right-click behavior. This setting is useful for gun mods, or items that need to have their default left-click and right-click functionality. Example config value:    com.flansmod.common.guns.ItemGun    com.mrcrayfish.guns.item.ItemGun    techguns.items.guns.GenericGun");
		itemBlacklist = config.getStringList("Item Blacklist", BWLISTS, itemBlacklist, "Blacklisted items (Simple setting; use CraftTweaker to get the syntax of the item in your hand. The command is: /ct hand). This is similar to Item Class Blacklist, however, it instead uses a resource location for specific items, such as:    mrcrayfish:gun");
		entityBlacklist = config.getStringList("Entity Blacklist", BWLISTS, entityBlacklist, "Blacklisted entity classes for attacking with offhand or sweep. You will not be able to attack any entity that extends this class with your offhand, and they will not be hit by sweep! This is to prevent you from accidentally attacking or killing certain entities. Please note that entities extending IEntityOwnable are by default blacklisted, when the entity is owned by the attacker.");

		/* --------------------------------------------------------------------------------------------------------------------- */

		setDefaultCustomWeaponVariables();

		if ( config.hasChanged() )
		{
			config.save();
		}
	}
	
	public static void postConfig() throws Exception
	{
		String CONFIG_REGEX = "([=]|(\\]\\s*\\[)|[\\]])";

		for ( String s : shieldList )
		{
			try
			{
				String[] list = s.split(CONFIG_REGEX);
				CustomShield spartanShield = new CustomShield();
				
				spartanShield.shield = Item.getByNameOrId(list[1]);
				spartanShield.damage = MathHelper.clamp(Double.parseDouble(list[3]), 0.0, 326.0);
				spartanShield.knockback = MathHelper.clamp(Double.parseDouble(list[5]), 0.0, 326.0);
				spartanShield.cooldown = MathHelper.clamp(Integer.parseInt(list[7]), 0, 255);
				shields.add(spartanShield);
			}
			catch ( Exception e )
			{
				throw new Exception(
			    	"\n\n================================================================"
					+ "\n|                        Immersive Combat                      |"
					+ "\n================================================================"
					
					+ "\n\nError:     Invalid bettercombat.cfg entry"
					
					+ "\n\nLocation:  Custom Shield Tweaker"
					
					+ "\n\nCause:     " + e.getCause()
					
					+ "\n\nDetails:   " + s
					
					+ "\n\nFix:       Remove the line listed for 'Custom Shield Tweaker' or reset your config by deleting bettercombat.cfg"
					
					+ "\n\n================================================================"
					+ "\n================================================================\n\n");
			}
		}
		
		for ( String s : axeList )
		{
			try
			{
				String[] list = s.split(CONFIG_REGEX);
				CustomAxe axe = new CustomAxe();
				
				axe.name = list[1];
				axe.disableDuration = Integer.parseInt(list[3]);
				
				axes.add(axe);
			}
			catch ( Exception e )
			{
				throw new Exception(
			    	"\n\n================================================================"
					+ "\n|                        Immersive Combat                      |"
					+ "\n================================================================"
					
					+ "\n\nError:     Invalid bettercombat.cfg entry"
					
					+ "\n\nLocation:  Custom Axe Tweaker"
					
					+ "\n\nCause:     " + e.getCause()
					
					+ "\n\nDetails:   " + s
					
					+ "\n\nFix:       Remove the line listed for 'Custom Axe Tweaker' or reset your config by deleting bettercombat.cfg"
					
					+ "\n\n================================================================"
					+ "\n================================================================\n\n");
			}
		}
		
		for ( String s : swordList )
		{
			try
			{
				String[] list = s.split(CONFIG_REGEX);
				CustomSword sword = new CustomSword();
				
				sword.name = list[1];
				sword.attackSpeed = MathHelper.clamp(Float.parseFloat(list[3]), -4.0F, 4.0F);
				swords.add(sword);
			}
			catch ( Exception e )
			{
				throw new Exception(
			    	"\n\n================================================================"
					+ "\n|                        Immersive Combat                      |"
					+ "\n================================================================"
					
					+ "\n\nError:     Invalid bettercombat.cfg entry"
					
					+ "\n\nLocation:  Custom Sword Tweaker"
					
					+ "\n\nCause:     " + e.getCause()
					
					+ "\n\nDetails:   " + s
					
					+ "\n\nFix:       Remove the line listed for 'Custom Sword Tweaker' or reset your config by deleting bettercombat.cfg"
					
					+ "\n\n================================================================"
					+ "\n================================================================\n\n");
			}
		}
		
		for ( String s : weaponList )
		{
			String[] list = s.split(CONFIG_REGEX);
			CustomWeapon customWeapon = new CustomWeapon();
			
			try
			{
				customWeapon.name = list[1];
				customWeapon.soundType = SoundType.valueOf(list[3]);
				customWeapon.animation = Animation.valueOf(list[5]);
				customWeapon.property = WeaponProperty.valueOf(list[7]);
				customWeapon.sweepMod = Integer.parseInt(list[9]);
				customWeapon.additionalReachMod = Double.parseDouble(list[11]);
				customWeapon.knockbackMod = Double.parseDouble(list[13]);
				customWeapon.critChanceMod = Double.parseDouble(list[15]);
				customWeapon.additionalCritDamageMod = Double.parseDouble(list[17]);

			}
			catch ( Exception e )
			{
				throw new Exception(
				    "\n\n================================================================"
					+ "\n|                        Immersive Combat                      |"
					+ "\n================================================================"
					
					+ "\n\nError:     Invalid bettercombat.cfg entry"
					
					+ "\n\nLocation:  Custom Weapon Tweaker"
					
					+ "\n\nCause:     " + e.getCause()
					
					+ "\n\nDetails:   " + s
					
					+ "\n\nFix:       There was an error for the line in the 'Custom Weapon Tweaker' config. Remove the line or reset your config by deleting bettercombat.cfg"
					
					+ "\n\n================================================================"
					+ "\n================================================================\n\n");
			}
			
			int potionEffectSplit;
			
			try
			{
				customWeapon.parry = Boolean.parseBoolean(list[19]);
				
				if ( !enableParrying )
				{
					customWeapon.parry = false;
				}
				
				potionEffectSplit = 21;
			}
			catch ( Exception e )
			{
				customWeapon.parry = enableParrying;
				potionEffectSplit = 19;
			}

			try
			{
				String[] potionEffectList = list[potionEffectSplit].split(",");

				try
				{
					if ( potionEffectList != null )
					{
						CustomWeaponPotionEffect customPotionEffect = new CustomWeaponPotionEffect();
						
						customPotionEffect.potionEffect = Potion.getPotionFromResourceLocation(potionEffectList[2]);
						
						if ( customPotionEffect.potionEffect == null )
						{
							throw new NullPointerException(
							    "\n\n================================================================"
								+ "\n|                        Immersive Combat                      |"
								+ "\n================================================================"
								
								+ "\n\nError:     Invalid bettercombat.cfg entry"
								
								+ "\n\nLocation:  Custom Weapon Tweaker"
								
								+ "\n\nCause:     NullPointerException"
								
								+ "\n\nDetails:   " + s
								
								+ "\n\nFix:       The [Potion Effect] resource location listed in 'Details' for 'Custom Weapon Tweaker' does not exist, make sure it is a valid resource location!"
								
								+ "\n\n================================================================"
								+ "\n================================================================\n\n");
						}
						
						customPotionEffect.potionChance = getPotionChance(potionEffectList[0]);
						customPotionEffect.afflict = potionEffectList[1].toLowerCase().equals("afflict");
						customPotionEffect.potionPower = Integer.parseInt(potionEffectList[3]);
						customPotionEffect.potionDuration = Integer.parseInt(potionEffectList[4]);
						
						customWeapon.customWeaponPotionEffect = customPotionEffect;
					}
				}
				catch ( Exception e )
				{
					throw new Exception(
					    "\n\n================================================================"
						+ "\n|                        Immersive Combat                      |"
						+ "\n================================================================"
						
						+ "\n\nError:     Invalid bettercombat.cfg entry"
						
						+ "\n\nLocation:  Custom Weapon Tweaker"
						
						+ "\n\nCause:     " + e.getCause()
						
						+ "\n\nDetails:   " + s
						
						+ "\n\nThe [Potion Effect] field for the line in 'Custom Weapon Tweaker' is incorrectly set up. Remove the [Potion Effect] field, or review the [Potion Effect] information in the 'Custom Weapon Tweaker' config to set it up correctly."
						
						+ "\n\n================================================================"
						+ "\n================================================================\n\n");
				}
			}
			catch ( Exception e )
			{
				/* No potion effect added, continue! */
			}
			
			weapons.add(customWeapon);
		}
		
		for ( String s : bowList )
		{
			try
			{
				String[] list = s.split(CONFIG_REGEX);
				CustomBow spartanBow = new CustomBow();
				
				spartanBow.bow = Item.getByNameOrId(list[1]);
				spartanBow.damage = Double.parseDouble(list[3]);
				spartanBow.velocity = MathHelper.clamp(Double.parseDouble(list[5]), 0.15, 1.5);
				spartanBow.additionalDrawSpeed = Integer.parseInt(list[7]);
				bows.add(spartanBow);
			}
			catch ( Exception e )
			{
				throw new Exception(
				    "\n\n================================================================"
					+ "\n|                        Immersive Combat                      |"
					+ "\n================================================================"
					
					+ "\n\nError:     Invalid bettercombat.cfg entry"
					
					+ "\n\nLocation:  Custom Bow Tweaker"
					
					+ "\n\nCause:     " + e.getCause()
					
					+ "\n\nDetails:   " + s
					
					+ "\n\nFix:       Remove the line listed in 'Custom Bow Tweaker' or reset your config by deleting bettercombat.cfg"
					
					+ "\n\n================================================================"
					+ "\n================================================================\n\n");
			}
		}
	}

	private static float getPotionChance( String potionEffectList )
	{
		try
		{
			return Float.parseFloat(potionEffectList);
		}
		catch ( Exception e )
		{
			return 0.0F;
		}
	}

	private static void setDefaultCustomWeaponVariables()
	{
		DEFAULT_CUSTOM_WEAPON.name = "";
		DEFAULT_CUSTOM_WEAPON.animation = Animation.CHOP;
		DEFAULT_CUSTOM_WEAPON.soundType = SoundType.AXE;
		DEFAULT_CUSTOM_WEAPON.property = WeaponProperty.ONEHAND;
		DEFAULT_CUSTOM_WEAPON.sweepMod = 0;
		DEFAULT_CUSTOM_WEAPON.additionalReachMod = 0.0F;
		DEFAULT_CUSTOM_WEAPON.knockbackMod = ConfigurationHandler.baseKnockback;
		DEFAULT_CUSTOM_WEAPON.critChanceMod = ConfigurationHandler.baseCritPercentChance;
		DEFAULT_CUSTOM_WEAPON.additionalCritDamageMod = 0;
		DEFAULT_CUSTOM_WEAPON.customWeaponPotionEffect = null;
		DEFAULT_CUSTOM_WEAPON.parry = enableParrying;
	}

	public static void createInstLists()
	{		
		for ( String className : itemClassWhitelist )
		{
			try
			{
				itemClassWhiteArray.add(Class.forName(className));
			}
			catch ( ClassNotFoundException classNotFoundException )
			{
				
			}
		}
		
		for ( String className : itemClassBlacklist )
		{
			try
			{
				itemClassBlackArray.add(Class.forName(className));
			}
			catch ( ClassNotFoundException classNotFoundException )
			{
				
			}
		}
		
		for ( String className : entityBlacklist )
		{
			try
			{
				entityBlackArray.add(Class.forName(className));
			}
			catch ( ClassNotFoundException classNotFoundException )
			{
				
			}
		}
	}
	
	/* REMOVED */
	// warhammerArmorPiercingAdjustments = config.getBoolean("Warhammer Armor Piercing", SPARTAN, true, "Enable to have armor the piercing effect of warhammers actually work against mobs who have added armor attribute values. The armor attribute can be added to mobs through RoughMobsRevamped. The armor peircing amount has also been slightly increased to make the damage difference more noticable.");
	// randomCrits = config.getFloat("Random Critical Chance", CRITICAL, 0.1F, 0.0F, 1.0F, "Adds random crit chance and replaces vanilla critical strikes.");

	/* config weapon */
	// https://github.com/SanAndreaP/BetterCombatRebirth/blob/f7b2de78a7bc3f93b0cb09775b7e7e13278da6a4/src/main/java/bettercombat/mod/util/ConfigurationHandler.java
	
//	@SuppressWarnings( "rawtypes" )
//	public static boolean isShield2D( Item item )
//	{		
//		for ( Class clazz : twoDimensionalShieldArray )
//		{			
//			if ( clazz.isInstance(item) )
//			{
//				return true;
//			}
//		}
//		
//		return false;
//	}
	
	public static boolean isShield2D( String name )
	{		
		for ( String s : twoDimensionalShieldList )
		{			
			if ( name.contains(s) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings( "rawtypes" )
	public static boolean isItemClassWhiteList( Item item )
	{		
		for ( Class clazz : itemClassWhiteArray )
		{			
			if ( clazz.isInstance(item) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings( "rawtypes" )
	private static boolean isItemClassBlackList( Item item )
	{		
		for ( Class clazz : itemClassBlackArray )
		{			
			if ( clazz.isInstance(item) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isItemBlackList(Item item)
	{
		for ( String blackListed : itemBlacklist )
		{			
			if ( blackListed.equals(item.getRegistryName().toString()) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isBlacklisted(Item item)
	{
		return ( isItemClassBlackList(item) || isItemBlackList(item) );
	}
	
//	public static boolean isItemBlackList( Item item )
//	{
//		return Arrays.stream(itemClassBlackArray).anyMatch(wlClass -> wlClass.isInstance(item));
//	}

	/* EventHandlersClient rightClick */
//	public static boolean rightClickAttackable( Entity entity )
//	{
//		return Arrays.stream(entityBlackArray).noneMatch(eClass -> eClass.isInstance(entity));
//	}
	
	@SuppressWarnings( "rawtypes" )
	public static boolean rightClickAttackable( EntityPlayer player, Entity entity )
	{
		/* If the player is under attack from the entity, return true */
		if ( entity instanceof EntityLiving )
		{
			EntityLiving el = (EntityLiving)entity;
			
			if ( el.getAttackTarget() != null && el.getAttackTarget().equals(player) )
			{
				return true;
			}
		}
		
		/* If the player owns the entity, return false */
		if ( entity instanceof IEntityOwnable && ((IEntityOwnable)entity).getOwner() != null && ((IEntityOwnable)entity).getOwner().equals(player) )
		{
			return false;
		}
		
		/* If the entity is blacklisted, return false */
		for ( Class clazz : entityBlackArray )
		{			
			if ( clazz.isInstance(entity) )
			{
				return false;
			}
		}
		
		/* It is attackable, return true */
		return true;
	}
	
//	public static boolean canAttackWithOffHand( EntityPlayer player, Entity entHit )
//	{
//		return ((entHit instanceof EntityLiving) && ((EntityLiving)entHit).getAttackTarget() == player) || (ConfigurationHandler.rightClickAttackable(entHit) && !(entHit instanceof IEntityOwnable && ((IEntityOwnable) entHit).getOwner() == player));
//	}
}