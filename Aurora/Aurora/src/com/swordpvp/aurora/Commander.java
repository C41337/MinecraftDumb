package com.swordpvp.aurora;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.swordlib.SwordLib;
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
	Aurora plugin;

	public Commander() {
		this.plugin = API.getInstance();
		plugin.getCommand("aurora").setExecutor(this);
		plugin.getCommand("au").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("aurora") || label.equalsIgnoreCase("au")) {
			String arg = "";
			if (args.length > 0)
				arg = args[0];
			if (arg.equalsIgnoreCase("team") && sender.hasPermission("aurora.team")) {
				if (plugin.gamemode.equals("FFA")) {
					sender.sendMessage(SwordLib.getPrefix("Aurora") + "Team switching is not available in FFA!");
					return true;
				}
				if (args.length != 2)
					help(sender);
				else {
					String team = args[1];
					if (team.equalsIgnoreCase("list")) {
						String teams = "";
						int x = 1;
						for (AuroraTeam team_ : plugin.teams) {
							teams += team_.getChatColor() + team_.color + ChatColor.WHITE + (plugin.teams.size() == x ? ", " : "");
							x++;
						}
						sender.sendMessage(SwordLib.getPrefix("Aurora") + "Available teams: " + teams);
					} else {
						AuroraTeam auroraTeam = null;
						for (AuroraTeam team_ : plugin.teams) {
							if (team.equalsIgnoreCase(team_.name)) {
								auroraTeam = team_;
								break;
							}
						}
						if (auroraTeam == null) {
							String teams = "";
							int x = 1;
							for (AuroraTeam team_ : plugin.teams) {
								teams += team_.getChatColor() + team_.color + ChatColor.WHITE + (plugin.teams.size() == x ? ", " : "");
								x++;
							}
							sender.sendMessage(SwordLib.getPrefix("Aurora") + "Team not found. Available teams: " + teams);
						} else {
							plugin.setTeam(sender.getName(), plugin.getAuroraTeam(team).name);
							plugin.spawn((Player) sender);
						}

					}

				}
			} else if (arg.equalsIgnoreCase("addspawn") && sender.hasPermission("aurora.admin")) {
				if (args.length != 3)
					help(sender);
				else {
					String map = args[1];
					String team = args[2];
					Location loc = ((Player) sender).getLocation();
					FileConfiguration config = plugin.getArenaConfig(map);
					boolean noTeam = true;
					for (String team_ : config.getStringList("Gamemode.Teams")) {
						if (team_.split(":")[0].equals(team)) {
							noTeam = false;
							break;
						}
					}
					boolean FFA = false;
					if (team.equals("FFA")) {
						FFA = true;
						noTeam = false;
					}
					if (noTeam) {
						sender.sendMessage(ChatColor.RED + "ERROR: Team '" + team + "' doesn't exist! Add a team using /au addteam");
						return true;
					}

					if (!plugin.getConfig().getStringList("maps").contains(map)) {
						sender.sendMessage(ChatColor.RED + "ERROR: Map '" + map + "' doesn't exist! Add a team using /au addteam");
						return true;
					}


					String path = "Gamemode." + team + "-Spawns";
					if (FFA)
						path = "Gamemode.Spawns";

					List<String> spawnList = plugin.getArenaConfig(map).getStringList(path);
					if (spawnList.size() < 1)
						spawnList = new ArrayList<String>();
					spawnList.add(plugin.getStringFromLocation(loc));
					config.set(path, spawnList);
					try {
						config.save(plugin.saveArena(map));
					} catch (IOException e) {
						e.printStackTrace();
					}
					List<String> maps = plugin.getConfig().getStringList("maps");
					if (!maps.contains(map)) {
						maps.add(map);
						plugin.getConfig().set("maps", maps);
						plugin.saveConfig();
					}
					sender.sendMessage(ChatColor.GREEN + "Spawn added to map '" + map + "' for team '" + team + "'");

				}
			} else if (arg.equalsIgnoreCase("addteam") && sender.hasPermission("aurora.admin")) {
				if (args.length != 4)
					help(sender);
				else {
					String map = args[1];
					String team = args[2];
					String color = args[3].toLowerCase();

					FileConfiguration config = plugin.getArenaConfig(map);
					for (String team_ : config.getStringList("Gamemode.Teams")) {
						if (team_.split(":")[0].toLowerCase().equals(team.toLowerCase())) {
							sender.sendMessage(ChatColor.RED + "Team '" + team + "' already exists!");
							return true;
						}
					}
					List<String> teamList = plugin.getArenaConfig(map).getStringList("Gamemode.Teams");
					if (teamList.size() < 1)
						teamList = new ArrayList<String>();
					teamList.add(team + ":" + color);
					config.set("Gamemode.Teams", teamList);

					try {
						config.save(plugin.saveArena(map));
					} catch (IOException e) {
						e.printStackTrace();
					}
					List<String> maps = plugin.getConfig().getStringList("maps");
					if (!maps.contains(map)) {
						maps.add(map);
						plugin.getConfig().set("maps", maps);
						plugin.saveConfig();
					}
					sender.sendMessage(ChatColor.GREEN + "Team '" + team + "' added to map '" + map + "'");

				}
			} else if (arg.equalsIgnoreCase("addobjective") && sender.hasPermission("aurora.admin")) {
				if (true) {
					sender.sendMessage(ChatColor.RED + "Objectives not done!");
					return true;
				}
				if (args.length != 4)
					help(sender);
				else {
					String map = args[1];
					String team = args[2];
					String objective = args[3].toLowerCase();
					Location loc = ((Player) sender).getLocation();

					FileConfiguration config = plugin.getArenaConfig(map);
					for (String team_ : config.getStringList("Gamemode.Teams")) {
						if (team_.split(":")[0].toLowerCase().equals(team.toLowerCase())) {
							sender.sendMessage(ChatColor.RED + "Team '" + team + "' already exists!");
							return true;
						}
					}
					List<String> teamList = plugin.getArenaConfig(map).getStringList("Gamemode.Teams");
					if (teamList.size() < 1)
						teamList = new ArrayList<String>();
					config.set("Gamemode.Teams", teamList);

					try {
						config.save(plugin.saveArena(map));
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.GREEN + "Objective '" + objective + "' added to map '" + map + "' for team '" + team + "'");

				}
			} else if (arg.equalsIgnoreCase("tpworld") && sender.hasPermission("aurora.admin")) {
				if (args.length != 2)
					help(sender);
				else {
					String map = args[1];
					Bukkit.createWorld(new WorldCreator(map + "_base"));
					World world = Bukkit.getWorld(map + "_base");
					Location loc = world.getSpawnLocation();
					((Player) sender).teleport(loc);
					sender.sendMessage(ChatColor.GREEN + "Teleported to world '" + map + "'");
				}
			} else if (arg.equalsIgnoreCase("nextmap") && sender.hasPermission("aurora.admin")) {
				plugin.go();
			} else if (arg.equalsIgnoreCase("time") && sender.hasPermission("aurora.admin")) {
				if (args.length >= 2) {
					if (CTBAPI.isInteger(args[1])) {
						int time = Integer.parseInt(args[1]);
						plugin.setMatchtime(time);
					} else {
						sender.sendMessage("Not an integer");
					}
				}
			} else {
				help(sender);
			}
		}
		return true;
	}

	private void help(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + " »-» Welcome to Aurora! «-« ");
		if (sender.hasPermission("aurora.admin")) {
			sender.sendMessage("Admin: addteam <map> <team> <color>");
			sender.sendMessage("Admin: addspawn <map> <team/FAA>");
			//sender.sendMessage("Admin: addobjective <map> <team> <objective>");
			sender.sendMessage("Admin: tpworld <world>");
			sender.sendMessage("Admin: nextmap");
		}
		if (sender.hasPermission("aurora.team")) {
			sender.sendMessage("Premium(+): team <team/list>");
		}
		if (sender.hasPermission("premium+")) {
			sender.sendMessage("Premium+: SPECIAL!");
			// TODO: Add Premium+ special
		}

	}
}