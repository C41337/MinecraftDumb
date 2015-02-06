package com.swordpvp.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.swordlib.SwordLib;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class API {

	private static MiniGames instance;
	private static int next = 0;

	@Getter
	private static List<MiniGamePlayer> miniGamePlayers = new ArrayList<MiniGamePlayer>();
	@Getter
	private static List<MiniGamePlayer> spectators = new ArrayList<MiniGamePlayer>();

	@Getter
	@Setter
	private static List<MiniGame> miniGames = new ArrayList<MiniGame>();

	@Getter
	@Setter
	private static MiniGame currentMiniGame = null;

	public API(MiniGames miniGame) {
		instance = miniGame;
	}

	public static MiniGames getInstance() {
		return instance;
	}

	public static void registerMiniGame(MiniGame miniGame) {
		miniGames.add(miniGame);
	}

	public static void ungisterMiniGame(MiniGame miniGame) {
		miniGames.remove(miniGame);
	}

	public static MiniGame getMiniGame(String name) {
		for (MiniGame miniGame : getMiniGames()) {
			if (miniGame.getName().equals(name))
				return miniGame;
		}
		return null;
	}

	public static void startMiniGame(MiniGame miniGame) {
		clearSpectator();
		setCurrentMiniGame(miniGame);
		Bukkit.getPluginManager().registerEvents(miniGame, getInstance());
		miniGame.setup();
		cleanUpWorlds();
	}

	public static void stopMiniGame(MiniGame miniGame) {
		setCurrentMiniGame(null);

		miniGame.finish();
		HandlerList.unregisterAll(miniGame);
		startRandomMiniGame();
	}

	public static void startRandomMiniGame() {
		startMiniGame(getMiniGames().get(next++));
		if (next >= getMiniGames().size())
			next = 0;
	}

	public static void win(MiniGame miniGame, MiniGamePlayer player1, MiniGamePlayer player2, MiniGamePlayer player3) {
		// broadcast that they win
		if (player1 != null && player1.getPlayer().isOnline()) {
			API.broadcast(ChatColor.AQUA + ChatColor.BOLD.toString() + "First Place: " + ChatColor.WHITE + player1.getName());
			//SwordLib.getSwordPlayer(player1.getPlayer()).addPoints(10);
		}
		if (player2 != null && player2.getPlayer().isOnline()) {
			API.broadcast(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Second Place: " + ChatColor.WHITE + player2.getName());
			//SwordLib.getSwordPlayer(player2.getPlayer()).addPoints(7);
		}
		if (player3 != null && player3.getPlayer().isOnline()) {
			API.broadcast(ChatColor.BLUE + ChatColor.BOLD.toString() + "Third Place: " + ChatColor.WHITE + player3.getName());
			//SwordLib.getSwordPlayer(player3.getPlayer()).addPoints(4);
		}

		stopMiniGame(miniGame);

	}

	public static void addMiniGamePlayer(MiniGamePlayer player) {
		miniGamePlayers.add(player);
	}

	public static void removeMiniGamePlayer(MiniGamePlayer player) {
		miniGamePlayers.remove(player);
	}

	public static MiniGamePlayer getMiniGamePlayer(Player player) {
		return getMiniGamePlayer(player.getName());
	}

	public static MiniGamePlayer getMiniGamePlayer(String player) {
		for (MiniGamePlayer miniGamePlayer : getMiniGamePlayers()) {
			if (miniGamePlayer.getName().equals(player)) return miniGamePlayer;
		}
		return null;
	}

	public static void resetPlayer(MiniGamePlayer player) {
		Player p = player.getPlayer();
		CTBAPI.resetPlayer(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFoodLevel(20);
		p.setMaxHealth(20);
		p.setHealth(20);
		p.setSaturation(20);
		//NametagAPI.resetNametag(player.getName());
	}

	public static void addSpectator(MiniGamePlayer player) {
		Player p = player.getPlayer();
		resetPlayer(player);
		p.setGameMode(GameMode.CREATIVE);
		p.setFlying(true);
		spectators.add(player);
		updateInvisible();
	}

	public static void removeSpectator(MiniGamePlayer player) {
		spectators.remove(player);
		updateInvisible();
	}

	public static void clearSpectator() {
		spectators.clear();
		updateInvisible();
	}

	public static boolean isSpectator(MiniGamePlayer player) {
		return getSpectators().contains(player);
	}

	public static void updateInvisible() {
		for (MiniGamePlayer player : getMiniGamePlayers()) {
			if (spectators.contains(player)) {
				for (MiniGamePlayer player_ : getMiniGamePlayers()) {
					player_.getPlayer().hidePlayer(player.getPlayer());
				}
			} else {
				for (MiniGamePlayer player_ : getMiniGamePlayers()) {
					player_.getPlayer().showPlayer(player.getPlayer());
				}
			}
		}
	}

	public static FileConfiguration getGamemodeConfig(String gamemode) {
		File arenaFile = new File(getInstance().getDataFolder() + "/gamemodes", gamemode + ".yml");
		return YamlConfiguration.loadConfiguration(arenaFile);
	}

	public static void saveArena(FileConfiguration arena, String name) {
		try {
			arena.save(new File(getInstance().getDataFolder() + "/gamemodes", name + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		player.sendMessage(SwordLib.getPrefix("MiniGames") + ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Send a message to a player
	 *
	 * @param player Player to say to
	 * @param msg    Message to say
	 */
	public static void say(CommandSender player, String msg) {
		if (player instanceof Player)
			say((Player) player, msg);
		else
			player.sendMessage(SwordLib.getPrefix("MiniGames") + ChatColor.translateAlternateColorCodes('&', msg));
	}

	public static void change() {
		stopMiniGame(getCurrentMiniGame());
	}

	public static void spectate(MiniGamePlayer miniGamePlayer) {
		API.addSpectator(miniGamePlayer);
		getCurrentMiniGame().spectate(miniGamePlayer);
	}

	public static void cleanUpWorlds() {
		List<World> worlds = new ArrayList<World>();
		for (MiniGamePlayer p : getMiniGamePlayers()) {
			if (!worlds.contains(p.getPlayer().getWorld())) {
				worlds.add(p.getPlayer().getWorld());
			}
		}
		for (World world : Bukkit.getWorlds()) {
			if (!worlds.contains(world) && !getCurrentMiniGame().getWorld().equals(world.getName())) {
				Bukkit.unloadWorld(world, true);
			}
		}
	}
}
