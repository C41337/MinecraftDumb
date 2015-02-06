package com.swordpvp.craftsonic;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.swordlib.SwordLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class API {

	private static CraftSonic craftSonic;
	private static List<Sonic> sonics = new ArrayList<Sonic>();
	private static List<CheckPoint> checkPoints = new ArrayList<CheckPoint>();
	private static HashMap<String, Integer> sugar = new HashMap<>();

	public API(CraftSonic plugin) {
		craftSonic = plugin;
	}


	/**
	 * Return the main Hades Games instance
	 *
	 * @return craftSonic
	 */
	public static CraftSonic getInstance() {
		return craftSonic;
	}


	/**
	 * Broadcast a message to all players
	 *
	 * @param msg Message to say
	 */
	public static void broadcast(String msg) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			say(player, msg);
		}
	}

	/**
	 * Send a message to a player
	 *
	 * @param player Player to say to
	 * @param msg    Message to say
	 */
	public static void say(Player player, String msg) {
		CTBAPI.say(player, SwordLib.getPrefix("CraftSonic") + msg);
	}

	/**
	 * Send a message to a player
	 *
	 * @param player Player to say to
	 * @param msg    Message to say
	 */
	public static void say(CommandSender player, String msg) {
		say((Player) player, msg);
	}

	/**
	 * Get a YML file for an arena
	 *
	 * @param arena Arena name
	 * @return FileConfiguration Config
	 */
	public static FileConfiguration getArenaConfig(String arena) {
		File arenaFile = new File(getInstance().getDataFolder() + "/arenas", arena + ".yml");
		return YamlConfiguration.loadConfiguration(arenaFile);
	}

	/**
	 * Save an arena config.
	 *
	 * @param arena FileConfiguration Config
	 */
	public static void saveArena(FileConfiguration arena) throws IOException {
		arena.save(new File(getInstance().getDataFolder() + "/arenas", arena.getString("Name") + ".yml"));
	}

	public static void addCheckPoint(CheckPoint checkPoint) {
		checkPoints.add(checkPoint);
	}

	public static void clearCheckPoints() {
		checkPoints.clear();
	}

	public static List<CheckPoint> getCheckPoints() {
		return checkPoints;
	}

	public static CheckPoint getCheckPoint(int id) {
		for (CheckPoint check : getCheckPoints()) {
			if (check.getId() == id)
				return check;
		}
		return null;
	}

	public static Sonic getSonic(String name) {
		for (Sonic player : getSonics()) {
			if (player.getPlayer().getName().equals(name))
				return player;
		}
		return null;
	}

	public static Sonic getSonic(Player player) {
		return getSonic(player.getName());
	}

	public static List<Sonic> getSonics() {
		return sonics;
	}

	public static void clearSonics() {
		sonics.clear();
	}

	public static void addSonic(Sonic sonic) {
		sonics.add(sonic);
	}

	public static void removeSonic(Sonic sonic) {
		sonics.remove(sonic);
	}

	public static ItemStack getBook() {

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta bookMeta = (BookMeta) book.getItemMeta();

		bookMeta.setTitle(ChatColor.RED + "CraftSonic Instructions");
		bookMeta.setAuthor("SwordPVP");

		bookMeta.setPages(Arrays.asList("       " + ChatColor.UNDERLINE + "Welcome to\n" + ChatColor.BLACK + "     " + ChatColor.RED + ChatColor.BOLD + "CraftSonic!\n\n" + ChatColor.BLACK + "Ready to race with your friends? Once the race has started, run to the checkpoints (marked with beacons)! Lost? Follow the signs and/or use the respawner (nether star).", "Your XP level is the time left you have to reach the next checkpoint. If you don't reach it, you will return to the lobby. If you are lost left/right click the nether star.", "The compass will point toward the next checkpoint. Follow the signs for more help navigating!", ChatColor.BOLD + "Supporters rewards:" + ChatColor.BLACK + "\nSupporters (Premium(+) & VIP(+)) get boosts which gives them a boost of speed for 10 seconds! They also get to join full matches and much more!"));

		book.setItemMeta(bookMeta);

		return book;

	}

	public static String getStringFromCheckPoint(CheckPoint checkPoint) {
		// <ID>,<LOC1>,<LOC2>

		return checkPoint.getId() + "," + CTBAPI.getStringFromLocation(checkPoint.getLoc1()) + "," + CTBAPI.getStringFromLocation(checkPoint.getLoc2());
	}

	public static CheckPoint getCheckPointFromString(String checkPoint) {
		// <ID>,<LOC1>,<LOC2>
		String[] stuff = checkPoint.split(",");
		return new CheckPoint(Integer.parseInt(stuff[0]), CTBAPI.getLocationFromString(stuff[1]), CTBAPI.getLocationFromString(stuff[2]));
	}

	public static HashMap<String, Integer> getSugar() {
		return sugar;
	}

	static public class ZoneVector {
		public int x;
		public int y;
		public int z;

		public ZoneVector(int x, int y, int z) {
			this.x = x;
			this.z = z;
			this.y = y;
		}

		public ZoneVector(Location loc) {
			this.x = loc.getBlockX();
			this.y = loc.getBlockY();
			this.z = loc.getBlockZ();
		}

		public boolean isInAABB(ZoneVector min, ZoneVector max) { //Idk why i use this name for the method, i just do.
			return ((this.x <= max.x) && (this.x >= min.x) && (this.z <= max.z) && (this.z >= min.z) /* Optional code*/ && (this.y <= max.y) && (this.y >= min.y));

		}

		public boolean isInCheckpoint(CheckPoint checkPoint) { //Idk why i use this name for the method, i just do.
			ZoneVector min = new ZoneVector(checkPoint.getLoc1());
			ZoneVector max = new ZoneVector(checkPoint.getLoc2());
			return ((this.x <= max.x) && (this.x >= min.x) && (this.z <= max.z) && (this.z >= min.z) /* Optional code*/ && (this.y <= max.y) && (this.y >= min.y));

		}
	}
}
