package com.swordpvp.minigames;

import com.swordpvp.swordlib.SwordLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;

public class Events implements Listener {


	public Events() {
		API.getInstance().getServer().getPluginManager().registerEvents(this, API.getInstance());
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		API.addMiniGamePlayer(new MiniGamePlayer(event.getPlayer().getName()));
		event.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + " joined the game!");
		Bukkit.getScheduler().runTaskLater(API.getInstance(), new Runnable() {
			@Override
			public void run() {
				API.resetPlayer(API.getMiniGamePlayer(event.getPlayer()));
				API.spectate(API.getMiniGamePlayer(event.getPlayer()));
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if (Bukkit.getOnlinePlayers().length >= 32) {
			event.setKickMessage(SwordLib.getPrefix("MiniGames") + "Server Full!");
			event.setResult(PlayerLoginEvent.Result.KICK_FULL);
		}
	}

	@EventHandler
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
			event.setCancelled(true);
	}


	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockIgnite(final BlockIgniteEvent event) {
		if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		event.setQuitMessage(ChatColor.RED + "- " + ChatColor.WHITE + event.getPlayer().getName() + ChatColor.GRAY + " left the game!");
		API.removeMiniGamePlayer(API.getMiniGamePlayer(event.getPlayer()));
		API.getCurrentMiniGame().update();
	}


	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getWhoClicked().getName())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
	}

	@EventHandler
	public void onInteract(final PlayerInteractEvent event) {
		if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageEntity(final EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && API.isSpectator(API.getMiniGamePlayer((Player) event.getDamager())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
	}

	@EventHandler
	public void onServerPing(final ServerListPingEvent event) {
		event.setMotd(API.getCurrentMiniGame().getName());
	}

}