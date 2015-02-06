package com.swordpvp.aurora;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.swordlib.SwordLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class Events implements Listener {

	Aurora plugin;

	public Events() {
		this.plugin = API.getInstance();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		plugin.setTeam(event.getPlayer().getName(), plugin.pickTeam());
		plugin.spawn(event.getPlayer());
		event.setJoinMessage(plugin.getAuroraTeam(plugin.getTeam(event.getPlayer().getName())).getChatColor() + event.getPlayer().getName() + ChatColor.GRAY + " joined the game!");
	}

	@EventHandler
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		if (!plugin.building)
			event.setCancelled(true);
		else {
			if (plugin.spawnProt != -1) {
				for (Location loc : plugin.getAllSpawns()) {
					if (loc.distance(event.getBlock().getLocation()) <= plugin.spawnProt) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!plugin.building)
			event.setCancelled(true);
		else {
			if (plugin.spawnProt != -1) {
				for (Location loc : plugin.getAllSpawns()) {
					if (loc.distance(event.getBlock().getLocation()) <= plugin.spawnProt) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		if (!plugin.fire)
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockIgnite(final BlockIgniteEvent event) {
		if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD && !plugin.fire) event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		event.setQuitMessage(plugin.getAuroraTeam(plugin.getTeam(event.getPlayer().getName())).getChatColor() + event.getPlayer().getName() + ChatColor.GRAY + " left the game!");
		plugin.removeFromTeams(event.getPlayer().getName());
	}

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		List<Integer> list = Arrays.asList(36, 37, 38, 39);
		if (list.contains(event.getSlot()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		boolean yes = false;
		for (ItemStack item : event.getPlayer().getInventory().getArmorContents()) {
			if (item.getType() == event.getItem().getItemStack().getType() && CTBAPI.isTool(event.getItem().getItemStack())) {
				yes = true;
				break;
			}
		}
		if (event.getItem() != null && (event.getPlayer().getInventory().contains(event.getItem().getItemStack().getType()) || yes)
				&& CTBAPI.isTool(event.getItem().getItemStack())) {
			event.setCancelled(true);
			event.getItem().remove();
		}
	}

	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 4));
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				plugin.spawn(event.getPlayer());
			}
		}, 4);
	}

	// Friendly damage block
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player && !plugin.gamemode.equals("FFA")) {
				EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
				Player damagee = (Player) event.getEntity();
				Player damager = (Player) edbe.getDamager();
				if (plugin.getTeam(damagee.getName()).equals(plugin.getTeam(damager.getName()))) {
					event.setCancelled(true);
				}

			}
			if (plugin.spawnProt != -1) {
				for (Location loc : plugin.getAllSpawns()) {
					if (loc.distance(event.getEntity().getLocation()) <= plugin.spawnProt) {
						event.setCancelled(true);
					}
				}
			}

		}
	}

	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		event.setDeathMessage(ChatColor.GRAY + event.getDeathMessage().replace(event.getEntity().getName(), plugin.getAuroraTeam(plugin.getTeam(event.getEntity().getName())).getChatColor() + event.getEntity().getName() + ChatColor.GRAY));
		if (event.getEntity().getKiller() != null) {
			if (plugin.gamemode == "FFA" || (!plugin.getTeam(event.getEntity().getKiller().getName()).equals(plugin.getTeam(event.getEntity().getName())))) {
				final String kill = event.getEntity().getKiller().getName();
				final String death = event.getEntity().getName();
				final String map = plugin.currentmap;
				if (kill != null && kill != "")
					event.setDeathMessage(event.getDeathMessage().replace(kill, plugin.getAuroraTeam(plugin.getTeam(kill)).getChatColor() + kill + ChatColor.GRAY));

				if (plugin.gamemode.equals("TDM")) {
					plugin.getAuroraTeam(plugin.getTeam(event.getEntity().getKiller().getName())).addPoints(1);
					plugin.updatePoints();
					plugin.getLogger().info(":" + plugin.getAuroraTeam(plugin.getTeam(event.getEntity().getKiller().getName())).points);
				}
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					public void run() {
					/*	try {
							SwordLib.getSwordPlayer(event.getEntity().getKiller()).addPoints(1);
							PreparedStatement ps = SwordLib.getConn().prepareStatement("INSERT INTO aurora_kills ( `kill`, `death`, `map` ) VALUES ( ?, ?, ? )");
							ps.setString((int) 1, kill);

							ps.setString((int) 2, death);
							ps.setString((int) 3, map);
							ps.execute();
						} catch (Exception e) {
							Bukkit.getLogger().warning("[MySQL] Error adding kill! Error: " + e);
						} */
					}
				});


			}
		}
	}

	@EventHandler
	public void onServerPing(final ServerListPingEvent event) {
		event.setMotd(plugin.gamemode + ":" + plugin.currentmap);
	}

}