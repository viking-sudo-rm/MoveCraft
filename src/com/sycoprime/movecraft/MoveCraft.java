package com.sycoprime.movecraft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import java.io.File;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/*
 * MoveCraft plugin for Hey0 mod (hMod) by Yogoda
 * Ported to Bukkit by SycoPrime
 *
 * You are free to modify it for your own server
 * or use part of the code for your own plugins.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */

public class MoveCraft extends JavaPlugin {

	static final String pluginName = "MoveCraft";
	static String version;
	public static MoveCraft instance;

	static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Logger logger = Logger.getLogger("Minecraft");
	boolean DebugMode = false;

	public ConfigFile configFile;

	private final MoveCraft_PlayerListener playerListener = new MoveCraft_PlayerListener();
	private final MoveCraft_BlockListener blockListener = new MoveCraft_BlockListener();

	public static void consoleSay(String msg) {
		System.out.println(getDateTime() + " [INFO] " + pluginName + " " + msg);
	}

	public void loadProperties() {
		configFile = new ConfigFile();

		File dir = getDataFolder();
		if (!dir.exists())
			dir.mkdir();

		CraftType.loadTypes(dir);
		//This setting was removed as of 0.6.9, craft type file creation has been commented out of the whole thing,
			//craft type files are to be distributed with the plugin 
		//if(configFile.ConfigSettings.get("WriteDefaultCraft").equalsIgnoreCase("true"))
			//CraftType.saveTypes(dir);		
	}
	
	public void onLoad() {
		
	}

	public void onEnable() {
		// getServer().getScheduler().scheduleSyncDelayedTask(this, loadSensors, 20*5);
		instance = this;

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);

		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		version = pdfFile.getVersion();

		loadProperties();
		PermissionInterface.setupPermissions();
		BlocksInfo.loadBlocksInfo();

		System.out.println(pdfFile.getName() + " " + version + " plugin enabled");
	}

	public void onDisable() {
		consoleSay(version + " plugin disabled");
	}

	public void releaseCraft(Player player, Craft craft) {
		if (craft != null) {
			player.sendMessage(ChatColor.YELLOW + craft.type.sayOnRelease);
			Craft.removeCraft(craft);
			if(DebugMode)
				craft.Destroy();
		} else
			player.sendMessage(ChatColor.YELLOW + "You don't have anything to release");
	}

	public void ToggleDebug() {
		this.DebugMode = !this.DebugMode;
		System.out.println("Debug mode set to " + this.DebugMode);
	}

	public boolean DebugMessage(String message) {
		if(this.DebugMode == true)
			System.out.println(message);
		return this.DebugMode;
	}

	public Craft createCraft(Player player, CraftType craftType, int x, int y, int z, String name) {
		if (DebugMode == true)
			player.sendMessage("Attempting to create " + craftType.name
					+ "at coordinates " + Integer.toString(x) + ", "
					+ Integer.toString(y) + ", " + Integer.toString(z));

		Craft craft = Craft.getCraft(player);

		// release any old craft the player had
		if (craft != null) {
			releaseCraft(player, craft);
		}

		craft = new Craft(craftType, player, name);

		// auto-detect and create the craft
		if (!CraftBuilder.detect(craft, x, y, z)) {
			return null;
		}

		/* Rotation code */
        craft.offX = Math.round(craft.sizeX / 2);
        craft.offZ = Math.round(craft.sizeZ / 2);

		//auto-detect and create the craft
		//if(!craft.createCraft(x, y, z, dirX, dirZ, name)){
		//   craft = null;
		//   return;
		// }
		/* End Rotation Code */

		if(craft.engineBlocks.size() > 0)
			craft.timer = new MoveCraft_Timer(0, craft, "engineCheck", false);
		else {
			if(craft.type.requiresRails) {
				craft.railMove();
			}
		}

		Craft.addCraft(craft);

		if(craft.type.listenItem == true)
			player.sendMessage(ChatColor.GRAY + "With an item in your hand, right-click in the direction you want to go.");
		if(craft.type.listenAnimation == true)
			player.sendMessage(ChatColor.GRAY + "Swing your arm in the direction you want to go.");
		if(craft.type.listenMovement == true)
			player.sendMessage(ChatColor.GRAY + "Move in the direction you want to go.");
		
		return craft;
	}
	
	public String ConfigSetting(String setting) {
		if(configFile.ConfigSettings.containsKey(setting))
			return configFile.ConfigSettings.get(setting);
		else {
			System.out.println("Sycoprime needs to be notified that a non-existing config setting " +
					"was attempted to be accessed.");
			return "";
		}
	}

	public static String getDateTime() {
		return dateFormat.format(new Date());
	}

	public void dropItem(Block block){

		int itemToDrop = BlocksInfo.getDropItem(block.getTypeId());
		int quantity = BlocksInfo.getDropQuantity(block.getTypeId());

		if(itemToDrop != -1 && quantity != 0){

			for(int i=0; i<quantity; i++){
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(itemToDrop, 1));
			}
		}
	}
}