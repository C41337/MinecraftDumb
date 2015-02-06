package com.swordpvp.craftsonic;

import com.craftthatblock.ctbapi.CTBAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commander implements CommandExecutor {
	CraftSonic plugin;

	public Commander() {
		this.plugin = API.getInstance();
		plugin.getCommand("craftsonic").setExecutor(this);
		//plugin.getCommand("cs").setExecutor(this);
		plugin.getCommand("sonic").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("craftsonic") || label.equalsIgnoreCase("cs") || label.equalsIgnoreCase("sonic")) {
			String arg = "";
			if (args.length > 0)
				arg = args[0];
			if (arg.equalsIgnoreCase("addmap") && sender.hasPermission("craftsonic.admin")) {
				if (args.length != 2)
					help(sender);
				else {
					String map = args[1];

					List<String> maps = plugin.getConfig().getStringList("Maps");
					if (!maps.contains(map)) {
						maps.add(map);
						plugin.getConfig().set("Maps", maps);
						plugin.saveConfig();
						plugin.reloadConfig();
					} else {
						API.say(sender, "&cMap '" + map + "' already exists!");
						return true;
					}


					FileConfiguration config = API.getArenaConfig(map);
					config.set("Name", map);
					if (config.get("Time") == null)
						config.set("Time", 300);
					try {
						API.saveArena(config);
					} catch (IOException e) {
						e.printStackTrace();
					}

					API.say(sender, ChatColor.GREEN + "Map '" + map + "' added!");

				}
			} else if (arg.equalsIgnoreCase("addcheckpoint") && sender.hasPermission("craftsonic.admin")) {
				if (args.length != 3)
					help(sender);
				else {
					String map = args[1];
					String Sid = args[2];
					Location loc = ((Player) sender).getLocation();
					if (!plugin.getConfig().getStringList("Maps").contains(map)) {
						API.say(sender, "&cMap '" + map + "' doesn't exists! Use /cs addmap <map>");
						return true;
					}
					if (!CTBAPI.isInteger(Sid)) {
						API.say(sender, "&cID '" + Sid + "' is not a number!");
						return true;
					}

					int id = Integer.parseInt(Sid);

					FileConfiguration config = API.getArenaConfig(map);


					List<String> spawnList = API.getArenaConfig(map).getStringList("CheckPoints");
					if (spawnList.size() < 1)
						spawnList = new ArrayList<String>();

					WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
					Selection selection = worldEdit.getSelection((Player) sender);
					if (selection == null) {
						API.say(sender, "Please select something with WorldEdit first.");
						return true;
					}
					Location min = selection.getMinimumPoint();
					Location max = selection.getMaximumPoint();

					CheckPoint check = new CheckPoint(id, min, max);

					for (String checks : spawnList) {
						if (API.getCheckPointFromString(checks).getId() == id) spawnList.remove(checks);
					}
					spawnList.add(API.getStringFromCheckPoint(check));
					config.set("CheckPoints", spawnList);
					try {
						API.saveArena(config);
					} catch (IOException e) {
						e.printStackTrace();
					}

					API.say(sender, "&aCheckpoint added to map '" + map + "'!");

				}
			} else if (arg.equalsIgnoreCase("setspawn") && sender.hasPermission("craftsonic.admin")) {
				if (args.length != 2)
					help(sender);
				else {
					String map = args[1];
					if (!plugin.getConfig().getStringList("Maps").contains(map)) {
						API.say(sender, "&cMap '" + map + "' doesn't exists! Use /cs addmap <map>");
						return true;
					}

					FileConfiguration config = API.getArenaConfig(map);

					Location loc = ((Player) sender).getLocation();
					config.set("Spawn", CTBAPI.getStringFromLocation(loc));
					try {
						API.saveArena(config);
					} catch (IOException e) {
						e.printStackTrace();
					}

					API.say(sender, "&aSpawn set for map '" + map + "'!");

				}
			} else if (arg.equalsIgnoreCase("tpworld") && sender.hasPermission("craftsonic.admin")) {
				if (args.length != 2)
					help(sender);
				else {
					String map = args[1];
					Player player = (Player) sender;

					Bukkit.createWorld(new WorldCreator(map));
					World world = Bukkit.getWorld(map);
					Location loc = world.getSpawnLocation();
					player.teleport(loc);
					API.say(player, "&aTeleported to map '" + map + "'!");


				}
			} else if (arg.equalsIgnoreCase("setlobby") && sender.hasPermission("craftsonic.admin")) {
				if (args.length != 2)
					help(sender);
				else {
					String map = args[1];
					if (!plugin.getConfig().getStringList("Maps").contains(map)) {
						API.say(sender, "&cMap '" + map + "' doesn't exists! Use /cs addmap <map>");
						return true;
					}

					FileConfiguration config = API.getArenaConfig(map);

					config.set("Lobby", CTBAPI.getStringFromLocation(((Player) sender).getLocation()));
					try {
						API.saveArena(config);
					} catch (IOException e) {
						e.printStackTrace();
					}

					API.say(sender, "&aLobby set for map '" + map + "'!");
				}
			} else if (arg.equalsIgnoreCase("time") && sender.hasPermission("hecatecraft.admin")) {
				if (args.length == 2 && CTBAPI.isInteger(args[1])) {
					API.getInstance().matchtime = Integer.parseInt(args[1]);
				}
			} else if (arg.equalsIgnoreCase("next") && sender.hasPermission("craftsonic.admin")) {
				plugin.changeMap();
				//sender.sendMessage(SwordLib.getPrefix("CraftSonic") + "Command not enabled!");
			} else {
				help(sender);
			}
		}

		return true;
	}

	private void help(CommandSender sender) {
		API.say(sender, " &c»-» &aWelcome to CraftSonic! &c«-« ");
		if (sender.hasPermission("craftsonic.admin")) {
			API.say(sender, "&cAdmin:&f addmap <map>");
			API.say(sender, "&cAdmin:&f addcheckpoint <map> <id>");
			API.say(sender, "&cAdmin:&f setspawn <map>");
			API.say(sender, "&cAdmin:&f setlobby <map>");
			API.say(sender, "&cAdmin:&f tpworld <world>");
			API.say(sender, "&cAdmin:&f time <time>");
			API.say(sender, "&cAdmin:&f next");
		}
	}
}