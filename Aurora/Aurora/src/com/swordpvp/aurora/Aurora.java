package com.swordpvp.aurora;

import ca.wacos.nametagedit.NametagAPI;
import com.craftthatblock.ctbapi.CTBAPI;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Kit;
import com.swordpvp.swordlib.SwordLib;
import lombok.Getter;
import lombok.Setter;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Aurora extends JavaPlugin {
	@Getter
	@Setter
	String currentmap;
	String world;
	HashMap<String, String> team = new HashMap<String, String>();
	@Getter
	@Setter
	Integer matchtime = 0;
	@Getter
	@Setter
	Integer maxtime = 0;
	List<AuroraTeam> teams = new ArrayList<AuroraTeam>();
	List<String> itemsToGive = new ArrayList<String>();
	List<ItemStack> armor = new ArrayList<ItemStack>();
	List<PotionEffect> potions = new ArrayList<PotionEffect>();
	List<Location> spawnsOther = new ArrayList<Location>();
	@Getter
	@Setter
	String gamemode = "";
	boolean colorArmor = true;
	boolean building = true;
	boolean fire = true;
	int spawnProt = 3;

	protected long time = 0;

	public void onEnable() {
		new API(this);
		for (String map : getConfig().getStringList("maps")) {
			try {
				API.delete(new File(this.getServer().getWorldContainer().getAbsolutePath(), map + "_temp"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new Events();
		new Commander();
		new TimeM();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (System.currentTimeMillis() >= time) {
					time = System.currentTimeMillis() + 1000;
					Bukkit.getPluginManager().callEvent(new SecondEvent());
				}
			}
		}, 2L, 2L);
		saveDefaultConfig();

		go();

	}

	public void onDisable() {
		for (String map : getConfig().getStringList("maps")) {
			try {
				API.delete(new File(this.getServer().getWorldContainer().getAbsolutePath(), map + "_temp"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setTeam(String player, String team_) {
		if (gamemode.equals("FFA"))
			team.put(player, "white");
		else
			this.team.put(player, team_);
	}

	public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (String aChildren : children) {
				copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public ItemStack getItemFromString(String item) {
		return new ItemStack(Material.getMaterial(item));
	}

	public ItemStack getItemFromString(int item) {
		return getItemFromString(Material.getMaterial(item).toString());
	}

	public String getTeam(String player) {
		return team.get(player);
	}

	public void removeFromTeams(String player) {
		team.remove(player);
	}

	// Pick a team though all the possible team
	public String pickTeam() {
		HashMap<String, Integer> teamP = new HashMap<String, Integer>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			String team_ = getTeam(player.getName());
			if (team_ != null) {
				Object x_ = teamP.remove(team_);
				int x = 0;
				if (x_ != null)
					x += Integer.parseInt(x_.toString());
				teamP.put(team_, x + 1);
			}
		}
		String Pteam = "";
		for (AuroraTeam team : teams) {
			if (Pteam.equals(""))
				Pteam = team.name;
			else {
				Object team_1_ = teamP.get(Pteam);
				if (team_1_ == null)
					team_1_ = 0;
				Object team_2_ = teamP.get(team.name);
				if (team_2_ == null)
					team_2_ = 0;
				int team_1 = Integer.parseInt(team_1_.toString());
				int team_2 = Integer.parseInt(team_2_.toString());
				if (team_1 > team_2)
					Pteam = team.name;
			}
		}
		return Pteam;
	}

	// Spawn a player
	public void spawn(Player player) {
		player.teleport(getAuroraTeam(getTeam(player.getName())).getRandomSpawn());
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		if (armor.size() == 4)
			player.getInventory().setArmorContents(API.getArmor(getTeam(player.getName())));
		player.setExp(0);
		player.setLevel(0);
		player.setCompassTarget(player.getLocation());
		player.setFoodLevel(20);
		player.setHealth(player.getMaxHealth());
		try {
			Kit.expandItems((Essentials) Bukkit.getPluginManager().getPlugin("Essentials"), ((Essentials) Bukkit.getPluginManager()
					.getPlugin("Essentials")).getUser(player), itemsToGive);
		} catch (Exception e) {
			getLogger().warning("ERROR IN " + getAuroraTeam(getTeam(player.getName())).name + ".yml CONFIG (ITEMS)");
			e.printStackTrace();
		}

		for (PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());
		for (PotionEffect potion : potions)
			player.addPotionEffect(potion);
		NametagAPI.setPrefix(player.getName(), getAuroraTeam(getTeam(player.getName())).getChatColor() + "");
		updatePlayerPoints(player);
		BarAPI.setMessage(player, ChatColor.YELLOW + "Time", ((float) ((float) matchtime / (float) maxtime) * 100));
	}

	public AuroraTeam getAuroraTeam(String team) {
		for (AuroraTeam team_ : teams)
			if (team.equalsIgnoreCase(team_.name))
				return team_;

		return null;
	}

	public ItemStack[] getArmorArray(List<ItemStack> armor_temp) {
		ItemStack[] armors = new ItemStack[4];
		armors[0] = armor_temp.get(0);
		armors[1] = armor_temp.get(1);
		armors[2] = armor_temp.get(2);
		armors[3] = armor_temp.get(3);
		return armors;
	}

	// Returns a String from a Location
	public String getStringFromLocation(Location location) {
		String loc = "";
		loc += location.getX();
		loc += ":";
		loc += location.getY();
		loc += ":";
		loc += location.getZ();
		loc += ":";
		loc += location.getYaw();
		loc += ":";
		loc += location.getPitch();
		return loc;
	}

	// Returns a Location from a String
	public Location getLocationFromString(String location, String map) {
		String[] loc = location.split(":");
		return new Location(Bukkit.getWorld(map + "_temp"), Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),
				Double.parseDouble(loc[2]), Float.parseFloat(loc[3]), Float.parseFloat(loc[4]));
	}

	// Returns a PotionEffect from a String
	public PotionEffect getPotionFromString(String potion) {
		String[] ar = potion.split(" ");
		return new PotionEffect(PotionEffectType.getByName(ar[0]), 72000, Integer.parseInt(ar[1]));
	}

	// Returns all the spawns
	public List<Location> getAllSpawns() {
		List<Location> locs = new ArrayList<Location>();
		for (AuroraTeam team : teams)
			for (Location loc : team.spawns)
				locs.add(loc);
		return locs;
	}


	// Returns an arena's config
	public FileConfiguration getArenaConfig(String arena) {
		File arenaFile = new File(getDataFolder() + "/arenas", arena + ".yml");
		return YamlConfiguration.loadConfiguration(arenaFile);
	}

	// Returns a file to save the config
	public File saveArena(String arena) throws IOException {
		return new File(getDataFolder() + "/arenas", arena + ".yml");
	}

	// New match (change map)
	// Note: this NEEDS 2 worlds at lease.
	public void go() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			SwordLib.getSwordPlayer(player).addPoints(10);
			player.kickPlayer(ChatColor.GREEN + "Match finished! You earned 5 SwordPoints!");
		}

		List<String> maps = getConfig().getStringList("maps");
		String map;
		final String oldMap = currentmap;

		do {
			map = maps.get(CTBAPI.getRandom().nextInt(maps.size()));
			if (maps.size() < 2) {
				break;
			}
		} while (map.equals(oldMap));
		getLogger().info("Loading " + map);

		FileConfiguration config = getArenaConfig(map);
		String tempWorld = map;
		getLogger().info("Loading2 " + tempWorld);

		if (config.contains("World"))
			tempWorld = config.getString("World");
		getLogger().info("Loading3 " + tempWorld);

		// Copy and Load (Map and WG)
		File mapBase = new File(this.getServer().getWorldContainer().getAbsolutePath(), tempWorld + "_base");
		File mapTemp = new File(this.getServer().getWorldContainer().getAbsolutePath(), tempWorld + "_temp");
		File mapBaseWG = new File(this.getServer().getWorldContainer().getAbsolutePath() + "/plugins/WorldGuard/worlds", tempWorld + "_base");
		File mapTempWG = new File(this.getServer().getWorldContainer().getAbsolutePath() + "/plugins/WorldGuard/worlds", tempWorld + "_temp");
		try {
			// Map
			if (mapTemp.exists())
				API.delete(mapTemp);
			copyDirectory(mapBase, mapTemp);

			// WorldGuard
			if (mapBaseWG.exists()) {
				if (mapTempWG.exists())
					API.delete(mapTempWG);
				copyDirectory(mapBaseWG, mapTempWG);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Bukkit.createWorld(new WorldCreator(tempWorld + "_temp"));

		// Clear stuff and get new Config
		itemsToGive.clear();
		armor.clear();
		potions.clear();
		spawnsOther.clear();
		teams.clear();
		final String oldWorld = world;
		this.world = tempWorld;
		gamemode = config.getString("Gamemode.Gamemode");
		List<String> gamemodeTypes = Arrays.asList("TDM", "FAA");

		boolean gamemodeValid = gamemodeTypes.contains(gamemode);

		if (!gamemodeValid) {
			Bukkit.getLogger().info("================================");
			Bukkit.getLogger().info("================================");
			Bukkit.getLogger().info("GAMEMODE " + gamemode + " IS NOT VALID");
			Bukkit.getLogger().info("================================");
			Bukkit.getLogger().info("================================");
			Bukkit.shutdown();
			return;
		}

		// Teams and Spawns
		if (gamemode.equals("FFA")) {
			for (String teamSpawn : config.getStringList("Gamemode.Spawns")) {
				spawnsOther.add(getLocationFromString(teamSpawn, tempWorld));
			}
		} else {
			for (String team_ : config.getStringList("Gamemode.Teams")) {
				String[] ar = team_.split(":");
				String team = ar[0];
				String color = ar[1];
				AuroraTeam auteam = new AuroraTeam(team, color);
				for (String teamSpawn : config.getStringList("Gamemode." + team + "-Spawns")) {
					auteam.addSpawn(getLocationFromString(teamSpawn, tempWorld));
				}
				teams.add(auteam);
			}
		}

		// Armor
		if (config.getList("Inventory.Armor") != null && config.getList("Inventory.Armor").size() == 4) {
			for (String item : config.getStringList("Inventory.Armor")) {
				if (CTBAPI.isInteger(item))
					armor.add(getItemFromString(Integer.parseInt(item)));
				else
					armor.add(getItemFromString(item.toUpperCase()));
			}
		}

		// Inventory items
		for (String itemString : config.getStringList("Inventory.Items"))
			itemsToGive.add(itemString);

		// Potions
		for (String potion : config.getStringList("Inventory.Potions"))
			potions.add(getPotionFromString(potion));


		currentmap = map;
		maxtime = config.getInt("Gamemode.TimeLimit");
		matchtime = maxtime;
		colorArmor = config.getBoolean("Inventory.UseColorArmor");
		building = config.getBoolean("Gamemode.Building");
		fire = config.getBoolean("Gamemode.Fire");
		spawnProt = config.getInt("Gamemode.SpawnProtection");
		for (Player player : Bukkit.getOnlinePlayers()) {
			setTeam(player.getName(), pickTeam());
			spawn(player);
		}
		getLogger().info("Switched map to: " + map);

		// Unload
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.unloadWorld(oldMap + "_temp", false);
				try {
					API.delete(new File(Bukkit.getWorldContainer().getAbsolutePath(), oldWorld + "_temp"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 40);

	}


	public static Color getColorFromString(String color) {
		HashMap<String, Color> colors = new HashMap<String, Color>();
		colors.put("red", Color.RED);
		colors.put("blue", Color.BLUE);
		colors.put("green", Color.GREEN);
		colors.put("white", Color.WHITE);
		colors.put("aqua", Color.AQUA);
		colors.put("black", Color.WHITE);
		return colors.get(color.toLowerCase());
	}

	public static ChatColor getChatColorFromString(String color) {
		HashMap<String, ChatColor> colors = new HashMap<String, ChatColor>();
		colors.put("red", ChatColor.RED);
		colors.put("blue", ChatColor.BLUE);
		colors.put("green", ChatColor.GREEN);
		colors.put("white", ChatColor.WHITE);
		colors.put("aqua", ChatColor.AQUA);
		colors.put("black", ChatColor.BLACK);
		return colors.get(color.toLowerCase());
	}


	public void updatePoints() {
		for (Player player : Bukkit.getOnlinePlayers())
			updatePlayerPoints(player);
	}

	public void updatePlayerPoints(Player player) {
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("score", "dummy");
		objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Score");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		scoreboard.registerNewTeam("Aurora");
		for (AuroraTeam team_ : teams) {
			String name = team_.getChatColor() + team_.name;
			if (getAuroraTeam(getTeam(player.getName())).name.equals(team_.name))
				name = ChatColor.GOLD + "•" + name;
			Score score = objective.getScore(Bukkit.getOfflinePlayer(name));
			score.setScore(team_.points);
		}
		player.setScoreboard(scoreboard);

	}
}
