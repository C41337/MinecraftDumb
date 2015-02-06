package com.swordpvp.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Commander implements CommandExecutor {

	public Commander() {
		API.getInstance().getCommand("minigame").setExecutor(this);
		API.getInstance().getCommand("minigames").setExecutor(this);
		API.getInstance().getCommand("mg").setExecutor(this);
	}

	// set TNTRun worldedit floor value
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("minigame") || label.equalsIgnoreCase("minigames") || label.equalsIgnoreCase("mg")) {
			String arg = "";
			if (args.length > 0)
				arg = args[0];
			if (sender.hasPermission("minigame.admin")) {
				if (arg.equalsIgnoreCase("tpworld")) {
					if (args.length != 2)
						help(sender);
					else {
						String map = args[1];
						if (!(sender instanceof Player)) {
							help(sender);
						} else {
							Player player = (Player) sender;

							Bukkit.createWorld(new WorldCreator(map));
							World world = Bukkit.getWorld(map);
							Location loc = world.getSpawnLocation();
							player.teleport(loc);
							API.say(player, "&aTeleported to map '" + map + "'!");
						}
					}
				} else if (arg.equalsIgnoreCase("set")) {
					if (args.length < 4) {
						help(sender);
					} else {
						String gamemode = args[1];
						String type = args[2];
						String key = args[3];

						FileConfiguration config = API.getGamemodeConfig(gamemode);
						List<String> types = Arrays.asList("string", "double", "int", "location", "worldedit");
						if (type.equalsIgnoreCase("string")) {
							if (args.length != 5) {
								API.say(sender, "No value.");
							} else {
								config.set(key, args[4]);
							}
						} else if (type.equalsIgnoreCase("double")) {
							if (args.length != 5) {
								API.say(sender, "No value.");
							} else {
								try {
									config.set(key, Double.parseDouble(args[4]));
								} catch (NumberFormatException e) {
									API.say(sender, "Value not double.");
								}
							}
						} else if (type.equalsIgnoreCase("int")) {
							if (args.length != 5) {
								API.say(sender, "No value.");
							} else {
								try {
									config.set(key, Integer.parseInt(args[4]));
								} catch (NumberFormatException e) {
									API.say(sender, "Value not int.");
								}
							}
						} else if (type.equalsIgnoreCase("location")) {
							config.set(key, CTBAPI.getStringFromLocation(((Player) sender).getLocation()));
						} else if (type.equalsIgnoreCase("worldedit")) {

							WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
							Selection selection = worldEdit.getSelection((Player) sender);
							if (selection == null) {
								API.say(sender, "Please select something with WorldEdit first.");
							} else {
								Location min = selection.getMinimumPoint();
								Location max = selection.getMaximumPoint();

								config.set(key, CTBAPI.getStringFromLocation(min) + "," + CTBAPI.getStringFromLocation(max));
							}
						} else {
							String typesString = "";
							for (String type_ : types) {
								typesString += type_ + " ";
							}
							API.say(sender, "Type not found. Types supported: " + typesString);
						}

						API.saveArena(config, gamemode);
						API.say(sender, "&aValue set for map '" + gamemode + "'!");
					}
				} else if (arg.equalsIgnoreCase("next")) {
					API.stopMiniGame(API.getCurrentMiniGame());
				} else {
					help(sender);
				}
			} else {
				help(sender);
			}
		}
		return true;
	}

	private void help(CommandSender sender) {
		API.say(sender, " &c»-» &aWelcome to Mini-Games! &c«-« ");
		if (sender.hasPermission("minigame.admin")) {
			API.say(sender, "&cAdmin:&f set <gamemode> <type> <key> [value]");
			API.say(sender, "&cAdmin:&f tpworld <world>");
			API.say(sender, "&cAdmin:&f next");
			API.say(sender, "&cAdmin:&f stop");
		}
	}
}