package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ColorMatch implements MiniGame {
	/*

	spawn = spawn location

	 */
	int y;
	double currentdelay;
	double delay;
	double dietime;
	boolean running;
	int time;
	int timeout;
	int size = 8;
	int tiles = 3;
	Location floor;
	Location spectate;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();
	DyeColor currentColor = getRandomColor();

	Selection selection;

	String world;

	EditSession editSession;
	LocalWorld localWorld;
	CuboidClipboard clipboard;


	@Override
	public String getName() {
		return "ColorMatch";
	}

	@Override
	public void setup() {

		FileConfiguration config = API.getGamemodeConfig(getName());


		String spawnString = config.getString("spawn");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		localWorld = new BukkitWorld(Bukkit.getWorld(world));

		editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(localWorld, -1);
		clipboard = new CuboidClipboard(new Vector(tiles * size, 1, tiles * size));

		floor = CTBAPI.getLocationFromString(spawnString);


		y = floor.getBlockY() - 3;

		time = 5 * 10;
		delay = 5.0;
		currentdelay = 5.0;
		dietime = 5.0;
		timeout = 2 * 60;
		running = false;
		winners.clear();
		spectate = floor.clone();
		spectate.add((size * tiles) / 2, 10, (size * tiles) / 2);


		setupArena();

		// Move all players and add to alive
		for (MiniGamePlayer player : API.getMiniGamePlayers()) {
			// move
			API.resetPlayer(player);
			player.getPlayer().teleport(getSpawn());
		}

		API.broadcast(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Now Playing: " + ChatColor.RESET + getName());
		API.broadcast("Don't fall off! Stand on the same color as the one in your hand!");
	}

	@Override
	public String getWorld() {
		return world;
	}

	@Override
	public void finish() {
		// go though all state blocks, set to air
		// TODO: WorldEdit API

		CuboidRegion region = new CuboidRegion(
				new Vector(floor.getBlockX(), floor.getBlockY(), floor.getBlockZ()),
				new Vector(floor.getBlockX() + (size + tiles), floor.getBlockY(), floor.getBlockZ() + (size + tiles)));
		try {
			editSession.setBlocks(region, new BaseBlock(BlockID.AIR));
		} catch (MaxChangedBlocksException e) {
			// As of the blocks are unlimited this should not be called
		}

		// clear blocks

	}

	@Override
	public void onGameTick() {
		currentdelay -= 0.1;
		for (MiniGamePlayer p : API.getMiniGamePlayers()) {
			BarAPI.setMessage(p.getPlayer(), ChatColor.YELLOW + "Time", ((float) currentdelay / (float) delay) * 100);
			p.getPlayer().setExp(((float) currentdelay / (float) delay));
			p.getPlayer().setLevel(0);
			//setupArena();
		}
		if (currentdelay <= 0) {
			// remove

			if (dietime <= 0) {
				// place block
				placeBlocks(true);
				dietime = 5.0;
				delay = delay * 0.85;
				currentdelay = delay + 1.5;
				if (delay < 0.5) {
					win();
				}
			} else if (dietime > 4.0) {
				// remove
				removeBlocks();

				dietime = 2.5;
			}
			dietime -= 0.1;


		}
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			running = true;
			placeBlocks(false);
			currentdelay = delay;
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

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent event) {
		// move, wait 4 tick, break block
		// if y is < 0, remove from alive players, make spectate
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

	public void setupArena() {

		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				DyeColor color = getRandomColor();
				// TODO: WorldEdit API

				CuboidRegion region = new CuboidRegion(
						new Vector(floor.getBlockX() + (x * tiles), floor.getBlockY(), floor.getBlockZ() + (z * tiles)),
						new Vector(floor.getBlockX() + (x * tiles) + tiles - 1, floor.getBlockY(), floor.getBlockZ() + (z * tiles) + tiles - 1));

				try {
					editSession.setBlocks(region, new BaseBlock(BlockID.CLOTH, color.getData()));
				} catch (MaxChangedBlocksException e) {
					// As of the blocks are unlimited this should not be called
				}

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

	public void placeBlocks(boolean place) {
		try {
			setupArena(); // because paste doesn't work. needs to be fixed.
			// TODO: fix paste
			//clipboard.paste(editSession, new Vector(floor.getBlockX(), floor.getBlockY(), floor.getBlockZ()), true);
		} catch (Exception e) {
			e.printStackTrace();

			// As of the blocks are unlimited this should not be called
		}
		currentColor = getRandomColor();
		for (MiniGamePlayer p : API.getMiniGamePlayers()) {
			ItemStack item = new ItemStack(Material.WOOL, 1, currentColor.getData());
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + currentColor.toString().replace("_", " "));
			item.setItemMeta(meta);
			Inventory inventory = p.getPlayer().getInventory();
			for (int x = 0; x < 9; x++)
				inventory.setItem(x, item);
		}
	}

	public void removeBlocks() {
		CuboidRegion region = new CuboidRegion(
				new Vector(floor.getBlockX(), floor.getBlockY(), floor.getBlockZ()),
				new Vector(floor.getBlockX() + (size * tiles), floor.getBlockY(), floor.getBlockZ() + (size * tiles)));

		Set<BaseBlock> badBlocks = new HashSet<BaseBlock>();
		for (int i = 0; i < 16; i++) {
			if (i != currentColor.getData())
				badBlocks.add(new BaseBlock(BlockID.CLOTH, i));
		}
		try {
			clipboard.copy(editSession, region);

			editSession.replaceBlocks(region, badBlocks, new SingleBlockPattern(new BaseBlock(BlockID.AIR)));
		} catch (Exception e) {
			e.printStackTrace();
			// As of the blocks are unlimited this should not be called
		}

	}


	public Location getSpawn() {
		Location spawn = floor.clone();
		spawn.add((size * tiles) / 2, 10, (size * tiles) / 2);
		return spawn;
	}

	public DyeColor getRandomColor() {
		return DyeColor.values()[(new Random()).nextInt(DyeColor.values().length)];
	}
}


