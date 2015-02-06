package com.swordpvp.craftsonic;

import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TimeM implements Listener {

	CraftSonic plugin;
	int lobbyTimeout;

	public TimeM() {
		this.plugin = API.getInstance();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		lobbyTimeout = 0;
	}

	@EventHandler
	public void onSecond(final SecondEvent event) {
		// Get the time of the match
		int time = API.getInstance().matchtime;

		// If its in the lobby and has under 3 player
		if (API.getInstance().getArenaState() == CraftSonic.ArenaState.LOBBY && Bukkit.getOnlinePlayers().length < 3) {
			lobbyTimeout++;
			if (lobbyTimeout % 20 == 0) {
				lobbyTimeout = 0;
				API.broadcast("Game must have at least 3 players to start!");
			}
		} else {
			// If no, countdown time (in-game or has enough player)
			time = --API.getInstance().matchtime;
		}
		// if running
		if (time >= 0) {
			// lobby stuff
			float percentTime = ((float) plugin.matchtime / 45) * 100;
			String bartext = "Lobby";
			Location compass = API.getInstance().getLobby();

			if (API.getInstance().getArenaState().equals(CraftSonic.ArenaState.INGAME)) {
				// If its in-game
				bartext = "In-Game";

				percentTime = ((float) plugin.matchtime / (float) plugin.maxtime) * 100;

			}

			// set
			for (Player player : Bukkit.getOnlinePlayers()) {
				BarAPI.setMessage(player, ChatColor.YELLOW + bartext, percentTime);
				Location loc = compass;
				// if in-game, point towards next checkpoint
				if (API.getSonic(player) != null) {
					Sonic sonic = API.getSonic(player);
					sonic.setTimeout(sonic.getTimeout() - 1);
					if (sonic.getTimeout() <= 0) {
						API.say(sonic.getPlayer(), "You were lost or idle, and got kicked out of the game.");
						API.getInstance().moveToLobby(sonic.getPlayer());
						API.removeSonic(sonic);

						API.getInstance().updateScoreboard();
						if (API.getSonics().size() < 1) {
							API.getInstance().win();
						}
						continue;
					} else {
						sonic.getPlayer().setLevel(sonic.getTimeout());
					}
					if (sonic.getTimeout() < 10 && sonic.getTimeout() % 2 == 0) {
						API.say(sonic.getPlayer(), "Reach the next checkout quickly!");
					}
					CheckPoint check = API.getCheckPoint(API.getSonic(player).getCheckpoint() + 1);
					if (check != null)
						loc = new Location(loc.getWorld(), (check.getLoc1().getX() + check.getLoc2().getX()) / 2, 0, (check.getLoc1().getZ() + check.getLoc2().getZ()) / 2);
				}
				player.setCompassTarget(loc);
			}

		}

		if (API.getInstance().getArenaState() != CraftSonic.ArenaState.LOBBY) {
			// Broadcast every minute, on 15 and 30, and when under 10 secs.
			if (time % 60 == 0 || time == 15 || time == 30 || time <= 10) {
				API.broadcast((time > 60 ? (time / 60) : (time)) + (time > 60 ? (" minute" + (time / 60 != 1 ? "s" : "")) :
						(" second" + (time != 1 ? "s" : ""))) + " left!");
			}
		} else if (API.getInstance().getArenaState() == CraftSonic.ArenaState.LOBBY) {
			if (time <= 5 && time > 0) {
				API.broadcast("Prepare to run in... " + time);
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.playSound(player.getLocation(), Sound.NOTE_BASS, (-time/3)*4, 10);
				}
			}
		}

		// Times is up!
		if (time <= 0) {
			if (API.getInstance().getArenaState() == CraftSonic.ArenaState.LOBBY) {
				plugin.startGame();
			} else {
				API.getInstance().changeMap();
			}
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setFoodLevel(20);
			player.getWorld().setStorm(false);
		}

		if (API.getSonics().size() < 1 && API.getInstance().getArenaState() == CraftSonic.ArenaState.INGAME) {
			API.getInstance().win();
		}

	}

}
