package com.swordpvp.craftsonic;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.craftsonic.shop.Sugar;
import com.swordpvp.swordlib.SwordLib;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class CraftSonic extends JavaPlugin {

	String currentmap;
	Integer matchtime = 0;
	Integer maxtime = 0;

	ArenaState arenaState = ArenaState.LOBBY;

	Team team;
	Scoreboard scoreboard;

	public List<TimeHolder> winners = new ArrayList<TimeHolder>();

	public enum ArenaState {
		LOBBY, INGAME
	}

	@Data
	static public class TimeHolder {
		final private int time;
		final private String name;


	}

	protected long time = 0;

	public void onEnable() {
		new API(this);

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
		changeMap();

		// load shop
		ShopManager.addShopItem(new Sugar());
	}

	public void onDisable() {
	}


	public ArenaState getArenaState() {
		return arenaState;
	}

	public void win() {
		int x = 0;
		String first = "";
		String second = "";
		String thrid = "";

		int firstT = 0;
		int secondT = 0;
		int thridT = 0;
		String message = "Match finished! " + ChatColor.GOLD + "Results: 1: ";
		for (TimeHolder time_ : winners) {

			if (Bukkit.getOfflinePlayer(time_.getName()).isOnline()) {
				Player player = Bukkit.getPlayer(time_.getName());
				int points = 10 - x;
				if (points <= 0) break;
				SwordLib.getSwordPlayer(player).addPoints(points);
				x++;
				if (x < 3) {
					message += time_.getName() + " (" + time_.getTime() + ")";
					if (x < 2) message += ", ";
				}
			}
		}

		arenaState = ArenaState.LOBBY;
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.kickPlayer(message);
		}
		changeMap();
	}

	public void changeMap() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.kickPlayer(ChatColor.GREEN + "Match finished!");
		}
		List<String> maps = getConfig().getStringList("Maps");
		String map;
		final String oldMap = currentmap;

		do {
			map = maps.get(CTBAPI.getRandom().nextInt(maps.size()));
			if (maps.size() < 2) {
				break;
			}
		} while (map.equals(oldMap));


		// Clear stuff and get new Config
		FileConfiguration config = API.getArenaConfig(map);
		maxtime = config.getInt("Time");
		maxtime = 300;
		matchtime = 45;
		arenaState = ArenaState.LOBBY;
		API.clearSonics();
		API.clearCheckPoints();
		winners.clear();
		API.getSugar().clear();

		for (World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, true);
		}
		if (Bukkit.getWorld(config.getString("Lobby").split(":")[0]) == null)
			Bukkit.createWorld(new WorldCreator(config.getString("Lobby").split(":")[0]));

		for (String point : config.getStringList("CheckPoints")) {
			API.addCheckPoint((API.getCheckPointFromString(point)));
		}

		currentmap = map;

		getLogger().info("Switched map to: " + map);

	}

	public void updateScoreboard() {
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
		if (getArenaState() == ArenaState.INGAME) {
			Objective objective = scoreboard.registerNewObjective("score", "dummy");
			objective.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "Checkpoints");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			Team team = scoreboard.registerNewTeam("CraftSonic");
			//team.setCanSeeFriendlyInvisibles(true);
			//for (Player player : Bukkit.getOnlinePlayers()) {
			//	if (API.getSonic(player) == null)
			//player.removePotionEffect(PotionEffectType.INVISIBILITY);
			//}
			for (Sonic sonic : API.getSonics()) {
				Score score = objective.getScore(Bukkit.getOfflinePlayer(sonic.getName()));
				score.setScore(sonic.getCheckpoint());
				//team.addPlayer(sonic.getPlayer());
				//sonic.getPlayer().addPotionEffect(CTBAPI.getInfinitePotionFromString(PotionEffectType.INVISIBILITY.getName()));

			}
		}
		for (Player player : Bukkit.getOnlinePlayers())
			player.setScoreboard(scoreboard);
	}


	public Location getLobby() {
		return CTBAPI.getLocationFromString(API.getArenaConfig(currentmap).getString("Lobby"));
	}

	public Location getSpawn() {
		return CTBAPI.getLocationFromString(API.getArenaConfig(currentmap).getString("Spawn"));
	}

	public void moveToLobby(Player player) {
		player.teleport(getLobby());
		CTBAPI.resetPlayer(player);
		// TODO: give book
		player.getInventory().addItem(API.getBook());

		player.addPotionEffect(CTBAPI.getInfinitePotionFromString(PotionEffectType.NIGHT_VISION.getName() + " 2"));
	}

	public void startGame() {

		API.clearSonics();
		// spawn everyone but hades
		for (Player sonic : Bukkit.getOnlinePlayers()) {
			sonic.playSound(sonic.getLocation(), Sound.SUCCESSFUL_HIT, 10, 10);
			API.addSonic(new Sonic(sonic.getName()));
			API.getSonic(sonic).spawn();

		}

		matchtime = maxtime;

		arenaState = ArenaState.INGAME;

		// boardcast messages about hades and start
		API.broadcast("Run started!");

		API.getInstance().updateScoreboard();

	}
}
