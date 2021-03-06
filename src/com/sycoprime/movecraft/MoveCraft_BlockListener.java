package com.sycoprime.movecraft;

import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.entity.Player;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class MoveCraft_BlockListener extends BlockListener {

	public MoveCraft_BlockListener() {
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		System.out.println("A BLOCK WAS PLACED");
		Block blockPlaced = event.getBlock();		

		Craft craft = Craft.getCraft(blockPlaced.getX(),
				blockPlaced.getY(), blockPlaced.getZ());
		
		if(craft != null) {
			
		}
	}

	public static void ClickedASign(Player player, Block block) {
		String world = block.getWorld().getName();
		Craft playerCraft = Craft.getCraft(player);

		Sign sign = (Sign) block.getState();

		if(sign.getLine(0) == null || sign.getLine(0).trim().equals("")) return;
		
		String craftTypeName = sign.getLine(0).trim().toLowerCase();;

		//remove colors
		craftTypeName = craftTypeName.replaceAll(ChatColor.BLUE.toString(), "");

		//remove brackets
		if(craftTypeName.startsWith("["))
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);

		//if the first line of the sign is a craft type, get the matching craft type.
		CraftType craftType = CraftType.getCraftType(craftTypeName);

		//it is a registred craft type !
		if(craftType != null){

			if(playerCraft != null && playerCraft.type == craftType) {
				MoveCraft.instance.releaseCraft(player, playerCraft);
				return;
			}

			//All players can use signs...
			/*
				if(!craftType.canUse(player)){
					player.sendMessage(ChatColor.RED + "You are not allowed to use this type of craft");
					return;
				}
			 */

			String restriction = sign.getLine(2).trim();
			if(!restriction.equals("") && restriction != null) {
				if(restriction != "public" && restriction != player.getName()) {
					//if(!PermissionInterface.CheckGroupPermission(player, restriction))
					if(!PermissionInterface.CheckGroupPermission(world, player, restriction))
						return;
				}
			}

			String name = sign.getLine(1);//.replaceAll("§.", "");

			if(name.trim().equals(""))
				name = null;

			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			int direction = block.getData();

			//get the block the sign is attached to (not rly needed lol)
			x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
			z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

			Craft tehCraft = MoveCraft.instance.createCraft(player, craftType, x, y, z, name);
			
			if(sign.getLine(3).equalsIgnoreCase("center")) {
				tehCraft.offX = x;
				tehCraft.offZ = z;
			}

			return;                        
		} else if(craftTypeName.equalsIgnoreCase("engage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
			if(playerCraft == null) {
				player.kickPlayer("Don't.");
				return;
			}
			Craft_Hyperspace.enterHyperSpace(playerCraft);
			sign.setLine(0, "Disengage Hyperdrive");
		} else if(craftTypeName.equalsIgnoreCase("disengage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
			if(playerCraft == null) {
				player.kickPlayer("I am TIRED of these MOTHER____ING noobs on this MOTHER____ING server.");
				return;
			}
			Craft_Hyperspace.exitHyperSpace(playerCraft);
			sign.setLine(0, "Engage Hyperdrive");
		}
	}
	
	public void onSignChange(SignChangeEvent event) {
		MoveCraft.instance.DebugMessage("A SIGN CHANGED!");
		
		Player player = event.getPlayer();
		String craftTypeName = event.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

		//remove brackets
		if(craftTypeName.startsWith("["))
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);

		//if the first line of the sign is a craft type, get the matching craft type.
		CraftType craftType = CraftType.getCraftType(craftTypeName);
		
		if (craftType != null &&
				!PermissionInterface.CheckPermission(player, "movecraft." + craftTypeName + "." + craftType.driveCommand)) {
			player.sendMessage("You don't have permission to do that!");
			event.setCancelled(true);
		}
	}
}
