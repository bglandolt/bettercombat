package bettercombat.mod.network;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.CustomWeapon;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
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

		/* [][][][][][][][()][!] */
		private static void handle( PacketFastEquip message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;

			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_ARMOR_EQUIP_IRON, player.getSoundCategory(), 1.0F, 1.0F);

			int mainhandWeaponIndex = -1;
			
			boolean twoHanded = false;
			
			ItemStack mainhandItem = player.inventory.getCurrentItem();
			ItemStack offhandItem = player.inventory.offHandInventory.get(0);
			
			/* IF MAINHAND HAS A WEAPON, */
			if ( isWeapon(mainhandItem) )
			{
				String mainString = mainhandItem.getItem().getRegistryName().toString();

				for ( CustomWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( mainString.contains(weapon.name) )
					{
						/* If the MAINHAND weapon is TWOHANDED, */
						if ( weapon.property == WeaponProperty.TWOHAND )
						{
							twoHanded = true;
						}
						
						break;
					}
				}
				
				mainhandWeaponIndex = player.inventory.currentItem;
			}
			/* OTHERWISE, FIND A WEAPON */
			else
			{
				/* HOTBAR WEAPON */
				for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
				{
					ItemStack itemStack = player.inventory.mainInventory.get(i);

					/* If a weapon is found, */
					if ( isWeapon(itemStack) )
					{
						/* Place the new weapon into [currentItem slot] */
						player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
						
						/* Replace the new weapon with the old mainhand item */
						player.inventory.mainInventory.set(i, mainhandItem);
						
						String mainString = itemStack.getItem().getRegistryName().toString();

						for ( CustomWeapon weapon : ConfigurationHandler.weapons )
						{
							if ( mainString.contains(weapon.name) )
							{
								/* If the MAINHAND weapon is TWOHANDED, */
								if ( weapon.property == WeaponProperty.TWOHAND )
								{
									twoHanded = true;
								}
								
								break;
							}
						}
						
						mainhandWeaponIndex = player.inventory.currentItem;
						
						break;
					}
				}
				
				/* INVENTORY WEAPON */
				if ( mainhandWeaponIndex == -1 )
				{
					if ( !ConfigurationHandler.fastEquipHotbarOnly )
					{
						for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
						{
							ItemStack itemStack = player.inventory.mainInventory.get(i);
	
							if ( isWeapon(itemStack) )
							{
								/* Place the new weapon into [currentItem slot] */
								player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
								
								/* Replace the new weapon with the old mainhand item */
								player.inventory.mainInventory.set(i, mainhandItem);
								
								String mainString = itemStack.getItem().getRegistryName().toString();
	
								for ( CustomWeapon weapon : ConfigurationHandler.weapons )
								{
									if ( mainString.contains(weapon.name) )
									{
										/* If the MAINHAND weapon is TWOHANDED, */
										if ( weapon.property == WeaponProperty.TWOHAND )
										{
											twoHanded = true;
										}
										
										break;
									}
								}
								
								mainhandWeaponIndex = player.inventory.currentItem;
								
								break;
							}
						}
					}
					
					/* FIND A MAINHAND HOE OR TOOL */
					if ( mainhandWeaponIndex == -1 )
					{
						for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
						{
							ItemStack itemStack = player.inventory.mainInventory.get(i);

							/* If a weapon is found, */
							if ( isToolOrHoe(itemStack) )
							{
								/* Place the new weapon into [currentItem slot] */
								player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
								
								/* Replace the new weapon with the old mainhand item */
								player.inventory.mainInventory.set(i, mainhandItem);
								
								String mainString = itemStack.getItem().getRegistryName().toString();

								for ( CustomWeapon weapon : ConfigurationHandler.weapons )
								{
									if ( mainString.contains(weapon.name) )
									{
										/* If the MAINHAND weapon is TWOHANDED, */
										if ( weapon.property == WeaponProperty.TWOHAND )
										{
											twoHanded = true;
										}
										
										break;
									}
								}
								
								mainhandWeaponIndex = player.inventory.currentItem;
								
								break;
							}
						}
						
						if ( mainhandWeaponIndex == -1 )
						{
							/* Find a tool if no weapons are found */
							for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
							{
								ItemStack itemStack = player.inventory.mainInventory.get(i);
				
								if ( isToolOrHoe(itemStack) )
								{
									/* Place the new weapon into [currentItem slot] */
									player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
									
									/* Replace the new weapon with the old mainhand item */
									player.inventory.mainInventory.set(i, mainhandItem);
									
									String mainString = itemStack.getItem().getRegistryName().toString();
				
									for ( CustomWeapon weapon : ConfigurationHandler.weapons )
									{
										if ( mainString.contains(weapon.name) )
										{
											/* If the MAINHAND weapon is TWOHANDED, */
											if ( weapon.property == WeaponProperty.TWOHAND )
											{
												twoHanded = true;
											}
											
											break;
										}
									}
									
									mainhandWeaponIndex = player.inventory.currentItem;
									
									break;
								}
							}
						}
					}
				}
			}

			if ( offhandItem.isEmpty() )
			{
				/* HOTBAR SHIELD */
				for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
				{
					if ( i != mainhandWeaponIndex )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);

						if ( isShield(itemStack) )
						{
							player.inventory.mainInventory.set(i, offhandItem);
							player.inventory.offHandInventory.set(0, itemStack);
							return;
						}
					}
				}

				/* HOTBAR WEAPON */
				if ( !twoHanded )
				{
					for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
					{
						if ( i != mainhandWeaponIndex )
						{
							ItemStack itemStack = player.inventory.mainInventory.get(i);
	
							if ( isWeapon(itemStack) )
							{
								String offString = itemStack.getItem().getRegistryName().toString();
	
								for ( CustomWeapon weapon : ConfigurationHandler.weapons )
								{
									if ( offString.contains(weapon.name) )
									{
										if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
										{
											player.inventory.mainInventory.set(i, offhandItem);
											player.inventory.offHandInventory.set(0, itemStack);
											return;
										}
									}
								}
							}
						}
					}
					
					/* HOTBAR HOE OR TOOL */
					for ( int i = 0; i < InventoryPlayer.getHotbarSize(); i++ )
					{
						ItemStack itemStack = player.inventory.mainInventory.get(i);
	
						/* If a weapon is found, */
						if ( isToolOrHoe(itemStack) )
						{
							/* Place the new weapon into [currentItem slot] */
							player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
							
							/* Replace the new weapon with the old mainhand item */
							player.inventory.mainInventory.set(i, mainhandItem);
							
							String mainString = itemStack.getItem().getRegistryName().toString();
	
							for ( CustomWeapon weapon : ConfigurationHandler.weapons )
							{
								if ( mainString.contains(weapon.name) )
								{
									/* If the MAINHAND weapon is TWOHANDED, */
									if ( weapon.property == WeaponProperty.TWOHAND )
									{
										twoHanded = true;
									}
									
									break;
								}
							}
							
							mainhandWeaponIndex = player.inventory.currentItem;
							
							break;
						}
					}
				}

				if ( !ConfigurationHandler.fastEquipHotbarOnly )
				{
					/* INTENTORY SHIELD */
					for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
					{
						if ( i != mainhandWeaponIndex )
						{
							ItemStack itemStack = player.inventory.mainInventory.get(i);

							if ( isShield(itemStack) )
							{
								player.inventory.mainInventory.set(i, offhandItem);
								player.inventory.offHandInventory.set(0, itemStack);
								return;
							}
						}
					}

					if ( !twoHanded )
					{
						/* INVENTORY WEAPON */
						for ( int i = InventoryPlayer.getHotbarSize() - 1; i < player.inventory.mainInventory.size(); i++ )
						{
							if ( i != mainhandWeaponIndex )
							{
								ItemStack itemStack = player.inventory.mainInventory.get(i);
	
								if ( isWeapon(itemStack) )
								{
									String offString = itemStack.getItem().getRegistryName().toString();
	
									for ( CustomWeapon weapon : ConfigurationHandler.weapons )
									{
										if ( offString.contains(weapon.name) )
										{
											if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
											{
												player.inventory.mainInventory.set(i, offhandItem);
												player.inventory.offHandInventory.set(0, itemStack);
												return;
											}
										}
									}
								}
							}
						}
						
						/* INVENTORY HOE OR TOOL */
						for ( int i = 0; i < player.inventory.mainInventory.size(); i++ )
						{
							if ( i != mainhandWeaponIndex )
							{
								ItemStack itemStack = player.inventory.mainInventory.get(i);
	
								if ( isToolOrHoe(itemStack) )
								{
									String offString = itemStack.getItem().getRegistryName().toString();
	
									for ( CustomWeapon weapon : ConfigurationHandler.weapons )
									{
										if ( offString.contains(weapon.name) )
										{
											if ( weapon.property != WeaponProperty.TWOHAND && weapon.property != WeaponProperty.MAINHAND )
											{
												player.inventory.mainInventory.set(i, offhandItem);
												player.inventory.offHandInventory.set(0, itemStack);
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			else if ( isShield(offhandItem) )
			{
				/* Do nothing! */
				return;
			}
			/* If the OFFHAND has an invalid TWOHANDED OR MAINHAND weapon, */
			else if ( isWeapon(offhandItem) )
			{
				String offString = offhandItem.getItem().getRegistryName().toString();
				
				for ( CustomWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( offString.contains(weapon.name) )
					{
						/* If the OFFHAND weapon is TWOHANDED OR MAINHAND, */
						if ( weapon.property == WeaponProperty.TWOHAND || weapon.property == WeaponProperty.MAINHAND )
						{
							/* Remove the weapon, as it is invalid */
							removeOffhandItemStack(player, offhandItem);
						}
						
						break;
					}
				}
			}
		}

		private static void removeOffhandItemStack( EntityPlayerMP player, ItemStack itemStack )
		{
			/* If the OFFHAND item can be moved to the inventory, */
			if ( player.inventory.addItemStackToInventory(itemStack) )
			{
				/* Remove the OFFHAND item */
				player.inventory.offHandInventory.remove(0);
			}			
		}

		public static boolean isWeapon( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && ConfigurationHandler.isItemClassWhiteList(itemStack.getItem()) && !(itemStack.getItem() instanceof ItemTool) && !(itemStack.getItem() instanceof ItemHoe);
		}
		
		public static boolean isToolOrHoe( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && (itemStack.getItem() instanceof ItemTool) || itemStack.getItem() instanceof ItemHoe;
		}

		public static boolean isShield( ItemStack itemStack )
		{
			return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemShield;
		}
	}
}