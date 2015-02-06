package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TNTRun implements MiniGame {
	/*

	spawn = spawn location

	 */
	int y;
	boolean breakblocks;
	int time;
	int timeout;
	Location spawn;
	Location spectate;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();

	int size = 24;

	String world;

	@Override
	public String getName() {
		return "TNTRun";
	}

	@Override
	public void setup() {
		FileConfiguration config = API.getGamemodeConfig(getName());
		time = 5 * 10;
		breakblocks = false;
		winners.clear();


		String spawnString = config.getString("spawn");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		spawn = CTBAPI.getLocationFromString(spawnString);

		y = spawn.getBlockY() - 10;
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
		API.broadcast("Don't fall off! Blocks will break under you!");
	}

	@Override
	public String getWorld() {
		return world;
	}

	@Override
	public void finish() {
		// Remove arena
		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.AIR);
			}
		}
		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY() - 7, spawn.getBlockZ() + z).getBlock();
				block.setType(Material.AIR);
			}
		}
	}

	@Override
	public void onGameTick() {
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			breakblocks = true;
		}
		if (time > 0) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.setMessage(p.getPlayer(), ChatColor.YELLOW + "Time", ((float) time / (float) (5 * 10)) * 100);
				//setupArena();
			}
		} else {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.removeBar(p.getPlayer());
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
		if (breakblocks) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				if (!API.isSpectator(p)) {
					Location loc = p.getPlayer().getLocation();
					loc.add(0, -1, 0);
					breakBlock(loc);
				}
			}
		}
		API.updateInvisible();
	}

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent event) {
		// move, wait 4 tick, break block
		// if y is < 0, remove from alive players, make spectate
		if (breakblocks && !API.isSpectator(API.getMiniGamePlayer(event.getPlayer()))) {
			Location loc = event.getFrom();
			loc.add(0, -1, 0);
			breakBlock(loc);
		}
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
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player)
			event.setCancelled(true);
	}

	public void breakBlock(final Location loc) {

		Bukkit.getScheduler().runTaskLater(API.getInstance(), new Runnable() {
			@Override
			public void run() {

				for (int x = loc.getBlockX() - 2; x < loc.getBlockX() + 2; x++) {
					for (int z = loc.getBlockZ() - 2; z < loc.getBlockZ() + 2; z++) {
						Location loc_ = new Location(loc.getWorld(), x, loc.getY(), z);
						if (loc_.distance(loc) <= 1.6) {
							loc_.getBlock().setType(Material.AIR);
							loc_.getWorld().playEffect(loc_, Effect.STEP_SOUND, loc_.getBlock().getType());
						}
					}
				}
			}
		}, 12);
	}

	public void setupArena() {
		// Make arena
		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY() - 7, spawn.getBlockZ() + z).getBlock();
				block.setType(Material.STAINED_CLAY);
				block.setData((byte) ((new Random()).nextInt(15) + 1));
			}
		}
		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.STAINED_CLAY);
				block.setData((byte) ((new Random()).nextInt(15) + 1));
			}
		}
	}

	public Location getSpawn() {
		Location spawn = this.spawn.clone();
		spawn.add(0, 2, 0);
		return spawn;
	}
}


