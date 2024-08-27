package bettercombat.mod.network;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.ConfigWeapon;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFastEquip implements IMessage
{

	public PacketFastEquip()
	{
	}

	public PacketFastEquip( int f, int parEntityId )
	{
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
	}

	public static class Handler implements IMessageHandler<PacketFastEquip, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketFastEquip message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		public static class Weapon
		{
			final int index;
			final ItemStack weapon;
			
			Weapon( final int index, final ItemStack weapon )
			{
				this.index = index;
				this.weapon = weapon;
			}
		}
		
		/* [][][][][][][][()][!] */
		private static void handle( PacketFastEquip message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;

			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_ARMOR_EQUIP_IRON, player.getSoundCategory(), 1.0F, 1.0F);
			
			ItemStack currentOffhandItem = player.inventory.offHandInventory.get(0);
			
			/* If the OFFHAND has an invalid TWOHANDED OR MAINHAND weapon, */
			if ( !isShield(currentOffhandItem) && isWeapon(currentOffhandItem) )
			{
				String s = currentOffhandItem.getItem().getRegistryName().toString();
				
				for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( s.contains(weapon.name) )
					{
						/* If the OFFHAND weapon is TWOHANDED OR MAINHAND, */
						if ( weapon.property == WeaponProperty.TWOHAND || weapon.property == WeaponProperty.MAINHAND )
						{
							/* If the OFFHAND item can be moved to the inventory, */
							if ( player.inventory.addItemStackToInventory(currentOffhandItem) )
							{
								/* Remove the OFFHAND item */
								player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
								currentOffhandItem = ItemStack.EMPTY;
							}
						}
						
						break;
					}
				}
			}
			
			Weapon currentMainhandItem = new Weapon(player.inventory.currentItem, player.inventory.getCurrentItem());
			
			boolean twoHanded = false;

			Weapon newMainhandItem = null;
			Weapon newOffhandItem = null;
			
			/* -------------------------------------------------------------------- */
			/* 								MAINHAND								*/
			/* -------------------------------------------------------------------- */
			if ( isWeapon(currentMainhandItem.weapon) )
			{
				String s = currentMainhandItem.weapon.getItem().getRegistryName().toString();

				for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( s.contains(weapon.name) )
					{
						/* If the MAINHAND weapon is TWOHANDED, */
						if ( weapon.property == WeaponProperty.TWOHAND )
						{
							/* No changes */
							return;
						}
						
						break;
					}
				}
			}
			/* OTHERWISE, FIND A MAINHAND WEAPON */
			else
			{
				/* -------------------------------------------------------------------- */
				/* 						MAINHAND | WEAPON | HOTBAR						*/
				/* -------------------------------------------------------------------- */
				inventorySearch: for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
				{
					ItemStack itemStack = player.inventory.mainInventory.get(i);

					if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
					{
						continue;
					}
					
					/* If a weapon is found, */
					if ( isWeapon(itemStack) )
					{
						String s = itemStack.getItem().getRegistryName().toString();

						for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
						{
							if ( s.contains(weapon.name) )
							{
								/* If the MAINHAND weapon is TWOHANDED, */
								if ( weapon.property == WeaponProperty.TWOHAND )
								{
									twoHanded = true;
								}

								newMainhandItem = new Weapon(i, itemStack);
								break inventorySearch;
							}
						}
					}
				}
				
				/* -------------------------------------------------------------------- */
				/* 						MAINHAND | WEAPON | INVENTORY					*/
				/* -------------------------------------------------------------------- */
				if ( newMainhandItem == null && !ConfigurationHandler.fastEquipHotbarOnly )
				{
					inventorySearch: for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( isWeapon(itemStack) )
						{
							String mainString = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( mainString.contains(weapon.name) )
								{
									/* If the MAINHAND weapon is TWOHANDED, */
									if ( weapon.property == WeaponProperty.TWOHAND )
									{
										twoHanded = true;
									}
									
									newMainhandItem = new Weapon(i, itemStack);
									break inventorySearch;
								}
							}
						}
					}
				}
					
				/* -------------------------------------------------------------------- */
				/* 							MAINHAND | TOOL | HOTBAR					*/
				/* -------------------------------------------------------------------- */
				if ( newMainhandItem == null )
				{
					inventorySearch: for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}
						
						/* If a weapon is found, */
						if ( isToolOrHoe(itemStack) )
						{
							String s = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( s.contains(weapon.name) )
								{
									/* If the MAINHAND weapon is TWOHANDED, */
									if ( weapon.property == WeaponProperty.TWOHAND )
									{
										twoHanded = true;
									}
									
									newMainhandItem = new Weapon(i, itemStack);
									break inventorySearch;
								}
							}
						}
					}
						
					/* -------------------------------------------------------------------- */
					/* 							MAINHAND | TOOL | INVENTORY					*/
					/* -------------------------------------------------------------------- */
					if ( newMainhandItem == null && !ConfigurationHandler.fastEquipHotbarOnly )
					{
						/* Find a tool if no weapons are found */
						inventorySearch: for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
						{
							ItemStack itemStack = player.inventory.mainInventory.get(i);
			
							if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
							{
								continue;
							}
							
							if ( isToolOrHoe(itemStack) )
							{
								String s = itemStack.getItem().getRegistryName().toString();
			
								for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
								{
									if ( s.contains(weapon.name) )
									{
										/* If the MAINHAND weapon is TWOHANDED, */
										if ( weapon.property == WeaponProperty.TWOHAND )
										{
											twoHanded = true;
										}
										
										newMainhandItem = new Weapon(i, itemStack);
										break inventorySearch;
									}
								}
							}
						}
					}
				}
			}
			
			if ( newMainhandItem != null )
			{
				player.inventory.setInventorySlotContents(currentMainhandItem.index, newMainhandItem.weapon.copy());
				player.inventory.setInventorySlotContents(newMainhandItem.index, currentMainhandItem.weapon.copy());
				
				currentMainhandItem = new Weapon(player.inventory.currentItem, newMainhandItem.weapon);
			}
			
			/* ------------------------------------------------------------------- */
			/* 								 OFFHAND							   */
			/* ------------------------------------------------------------------- */
			if ( currentOffhandItem == null || currentOffhandItem.isEmpty() )
			{
				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | SHIELD | HOTBAR						*/
				/* -------------------------------------------------------------------- */
				for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
				{
					ItemStack itemStack = player.inventory.mainInventory.get(i);

					if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
					{
						continue;
					}

					if ( isShield(itemStack) )
					{
						newOffhandItem = new Weapon(i, itemStack);
						break;
					}
				}

				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | WEAPON | HOTBAR					*/
				/* -------------------------------------------------------------------- */
				if ( newOffhandItem == null && !twoHanded )
				{
					inventorySearch: for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}
	
						if ( isWeapon(itemStack) )
						{
							String s = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( s.contains(weapon.name) )
								{
									if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
									{
										newOffhandItem = new Weapon(i, itemStack);
										break inventorySearch;
									}
								}
							}
						}
					}
				}
				
				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | TOOL | HOTBAR						*/
				/* -------------------------------------------------------------------- */
				if ( newOffhandItem == null && !twoHanded && ConfigurationHandler.fastEquipOffhandWeaponsOrShieldsOnly )
				{
					/* HOTBAR HOE OR TOOL */
					inventorySearch: for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}
	
						if ( isToolOrHoe(itemStack) )
						{
							String s = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( s.contains(weapon.name) )
								{
									if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
									{
										newOffhandItem = new Weapon(i, itemStack);
										break inventorySearch;
									}
								}
							}
						}
					}
				}

				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | SHIELD | INVENTORY					*/
				/* -------------------------------------------------------------------- */
				if ( newOffhandItem == null && !ConfigurationHandler.fastEquipHotbarOnly )
				{
					/* INTENTORY SHIELD */
					for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}

						if ( isShield(itemStack) )
						{
							newOffhandItem = new Weapon(i, itemStack);
							break;
						}
					}
				}

				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | WEAPON | INVENTORY					*/
				/* -------------------------------------------------------------------- */
				if ( newOffhandItem == null && !twoHanded )
				{
					inventorySearch: for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}
	
						if ( isWeapon(itemStack) )
						{
							String s = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( s.contains(weapon.name) )
								{
									if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
									{
										newOffhandItem = new Weapon(i, itemStack);
										break inventorySearch;
									}
								}
							}
						}
					}
				}
				
				/* -------------------------------------------------------------------- */
				/* 						OFFHAND | TOOL | INVENTORY						*/
				/* -------------------------------------------------------------------- */
				if ( newOffhandItem == null && !twoHanded && ConfigurationHandler.fastEquipOffhandWeaponsOrShieldsOnly )
				{
					/* HOTBAR HOE OR TOOL */
					inventorySearch: for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( sameWeaponOrSlot(player, currentMainhandItem, itemStack, i) )
						{
							continue;
						}
	
						if ( isToolOrHoe(itemStack) )
						{
							String s = itemStack.getItem().getRegistryName().toString();

							for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( s.contains(weapon.name) )
								{
									if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
									{
										newOffhandItem = new Weapon(i, itemStack);
										break inventorySearch;
									}
								}
							}
						}
					}
				}
				
				if ( newOffhandItem != null )
				{
					player.inventory.offHandInventory.set(0, newOffhandItem.weapon);
					player.inventory.setInventorySlotContents(newOffhandItem.index, currentOffhandItem.copy());
				}
			}
		}

		private static boolean sameWeaponOrSlot( EntityPlayer player, Weapon mainhandWeapon, ItemStack itemStack, int i )
		{
			if ( i == mainhandWeapon.index )
			{
				return true;
			}

			if ( mainhandWeapon.weapon == itemStack )
			{
				return true;
			}
			
			return false;
		}

		public static boolean isWeapon( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && ConfigurationHandler.isConfigWeapon(itemStack.getItem()) && !isToolOrHoe(itemStack);
		}
		
		public static boolean isToolOrHoe( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && ( (itemStack.getItem() instanceof ItemTool && !(itemStack.getItem() instanceof ItemAxe)) || itemStack.getItem() instanceof ItemHoe );
		}

		public static boolean isShield( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemShield;
		}
	}
}