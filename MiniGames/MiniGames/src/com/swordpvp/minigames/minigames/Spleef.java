package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Spleef implements MiniGame {
	/*

	spawn = spawn location

	 */
	int y;
	boolean breakblocks;
	int time;
	Location spawn;
	Location spectate;
	int timeout;
	String world;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();

	int size = 24;


	@Override
	public String getName() {
		return "Spleef";
	}

	@Override
	public void setup() {

		API.clearSpectator();
		FileConfiguration config = API.getGamemodeConfig(getName());

		time = 5 * 10;
		breakblocks = false;
		winners.clear();
		String spawnString = config.getString("spawn");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		spawn = CTBAPI.getLocationFromString(spawnString);
		y = spawn.getBlockY() - 3;
		spectate = spawn.clone();
		spectate.add(0, 10, 0);

		timeout = 2 * 60;
		setupArena();

		// Move all players and add to alive
		for (MiniGamePlayer player : API.getMiniGamePlayers()) {
			// move
			API.resetPlayer(player);
			player.getPlayer().teleport(getSpawn());

		}
		API.broadcast(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Now Playing: " + ChatColor.RESET + getName());
		API.broadcast("Make other players fall off the platform by breaking blocks under them! Right click to throw snowballs!");

	}

	@Override
	public void onGameTick() {
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			breakblocks = true;
			ItemStack item = new ItemStack(Material.DIAMOND_SPADE);
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.removeBar(p.getPlayer());
				if (!API.isSpectator(p))
					p.getPlayer().getInventory().addItem(item);
			}
		}
		if (time > 0) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.setMessage(p.getPlayer(), ChatColor.YELLOW + "Time", ((float) time / (float) (5 * 10)) * 100);
				//setupArena();
			}
		}
	}

	@Override
	public void onSecond() {
		//API.broadcast(time + " : " + breakblocks);

		update();
		if (timeout-- <= 0) {
			win();
		}

	}

	@Override
	public void spectate(MiniGamePlayer player) {
		player.getPlayer().teleport(spectate);
	}

	@Override
	public void update() {
		//API.broadcast(API.getMiniGamePlayers().size() + " - " + API.getSpectators().size());
		if (API.getMiniGamePlayers().size() - API.getSpectators().size() <= 1 && API.getMiniGamePlayers().size() > 1) {
			win();
		}
		API.updateInvisible();
	}

	@Override
	public String getWorld() {
		return world;
	}

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent event) {
		if (event.getTo().getY() <= y) {
			// dead
			if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer()))) {
				event.getPlayer().teleport(spectate);
			} else {
				MiniGamePlayer player = API.getMiniGamePlayer(event.getPlayer());
				winners.add(player);
				API.broadcast(player.getName() + " &7fell off!");
				API.spectate(player);
				update();
			}
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		if (!breakblocks) event.setCancelled(true);
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())) && breakblocks) {
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			//event.getBlock().getDrops().clear();
		}
	}


	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onInteract(final PlayerInteractEvent event) {
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (event.getItem() != null && event.getItem().getType() == Material.DIAMOND_SPADE) {
				event.getPlayer().launchProjectile(Snowball.class);
			}
		}
	}

	@EventHandler
	public void onHit(final ProjectileHitEvent event) {
		if (event.getEntity().getType() == EntityType.SNOWBALL) {
			Location loc = event.getEntity().getLocation();
			Block b = null;
			for (int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++) {
				for (int y = loc.getBlockY() - 1; y <= loc.getBlockY() + 1; y++) {
					for (int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++) {
						Block near = new Location(spawn.getWorld(), x, y, z).getBlock();
						if (b == null || (near.getLocation().distance(loc) < b.getLocation().distance(loc) && near.getType() == Material.SNOW_BLOCK)) {
							b = near;
						}
					}
				}
			}
			if (b != null) {
				b.setType(Material.AIR);
			}


		}
	}

	@EventHandler
	public void onPickup(final PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player)
			event.setCancelled(true);
	}


	public void setupArena() {
		// Make arena
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.SNOW_BLOCK);
			}
		}
	}

	@Override
	public void finish() {
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.AIR);
			}
		}
	}

	public void win() {
		if (API.getMiniGamePlayers().size() - API.getSpectators().size() >= 1) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				if (!API.isSpectator(p))
					winners.add(p);
			}
		}
		MiniGamePlayer player1 = null;
		MiniGamePlayer player2 = null;
		MiniGamePlayer player3 = null;
		if (winners.size() == 0) {

		} else if (winners.size() == 1) {
			player1 = winners.get(0);
		} else if (winners.size() == 2) {
			player1 = winners.get(1);
			player2 = winners.get(0);
		} else if (winners.size() >= 3) {
			player1 = winners.get(winners.size() - 1);
			player2 = winners.get(winners.size() - 2);
			player3 = winners.get(winners.size() - 3);
		}
		API.win(this, player1, player2, player3);
	}

	public Location getSpawn() {
		Location spawn = this.spawn.clone();
		spawn.add(0, 2, 0);
		return spawn;
	}
}


