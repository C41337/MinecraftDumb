package com.craftthatblock.gpmessages;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class GPMessages extends JavaPlugin implements Listener {

	public void onEnable() {
		// Check if plugin exist
		if (getServer().getPluginManager().getPlugin("GriefPrevention") == null) {
			getLogger().warning("Plugin 'GriefPrevention' could not be found. Disabling GPMessages.");
			return;
		}
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);

		// Metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			getLogger().warning("Could not connect to Metric. Error: " + e);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Claim from = GriefPrevention.instance.dataStore.getClaimAt(event.getFrom(), false, null);
		Claim to = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(), false, null);

		if (to != from) {
			// entered/exited new claim

			// Reload format
			reloadConfig();

			if (from != null) {
				// exit
				String message = getConfig().getString("exit");
				message = message.replace("%own%", from.getOwnerName());
				event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			}
			if (to != null) {
				// enter
				String message = getConfig().getString("enter");
				message = message.replace("%own%", to.getOwnerName());
				event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			}
		}
	}


}
