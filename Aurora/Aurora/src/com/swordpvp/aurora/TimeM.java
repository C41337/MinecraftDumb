package com.swordpvp.aurora;

import com.swordpvp.swordlib.SwordLib;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TimeM implements Listener {

	Aurora plugin;

	public TimeM() {
		this.plugin = API.getInstance();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onSecond(final SecondEvent event) {
		plugin.matchtime--;
		if (getTime() >= 0) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				BarAPI.setHealth(player, ((float) ((float) plugin.matchtime / (float) plugin.maxtime) * 100));
			}
		}

		if (getTime() <= 0) {
			plugin.go();
		}
		if (getTime() % 60 == 0) {
			say((getTime() / 60) + " minute(s) left!");
		}
		if (getTime() == 15 || getTime() == 30 || getTime() <= 10) {
			say(getTime() + " second(s) left!");
		}
	}

	private Integer getTime() {
		return plugin.matchtime;
	}

	private void say(String msg) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(SwordLib.getPrefix("Aurora") + ChatColor.translateAlternateColorCodes('&', msg));
		}
	}
}
