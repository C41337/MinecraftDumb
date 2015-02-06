package com.swordpvp.craftsonic;

import ca.wacos.nametagedit.NametagAPI;
import com.craftthatblock.ctbapi.CTBAPI;
import com.craftthatblock.ctbapi.ItemBuilder;
import com.swordpvp.swordlib.Messages;
import com.swordpvp.swordlib.SwordLib;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Events implements Listener {

	CraftSonic plugin;

	public Events() {
		this.plugin = API.getInstance();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		API.getInstance().moveToLobby(player);
		event.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + " joined the game!");
		Bukkit.getScheduler().runTaskLater(API.getInstance(), new Runnable() {
			@Override
			public void run() {
				CTBAPI.resetPlayer(player);
				player.setMaxHealth(20);
				API.getInstance().moveToLobby(player);
				//event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				NametagAPI.resetNametag(player.getName());
				player.getInventory().addItem(new ItemBuilder(Material.GOLD_INGOT).withName(ChatColor.YELLOW + "Shop").toItemStack());
			}
		}, 3);
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if (API.getInstance().getArenaState() == CraftSonic.ArenaState.INGAME) {
			event.setKickMessage(SwordLib.getPrefix("CraftSonic") + Messages.FULL_STARTED);
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
		} else if (Bukkit.getOnlinePlayers().length > 25) {
			System.out.println("Checking login for: " + event.getPlayer().getName());
			if (SwordLib.canJoinFull(event.getPlayer().getName())) {
				event.setResult(PlayerLoginEvent.Result.ALLOWED);
			} else {
				event.setKickMessage(SwordLib.getPrefix("CraftSonic") + Messages.FULL_BUYRANK);
				event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			}
		}
	}

	@EventHandler
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
			event.setCancelled(false);
	}


	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent event) {
		if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
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
		if (API.getInstance().getArenaState().equals(CraftSonic.ArenaState.INGAME)) {

			if (API.getSonic(event.getPlayer()) != null) {
				API.removeSonic(API.getSonic(event.getPlayer()));
			}
			if (API.getSonics().size() < 1) {
				API.getInstance().changeMap();
			}

			API.getInstance().updateScoreboard();
		}


	}

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (!event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
		if (event.getInventory() != null && event.getInventory().getTitle() != null && event.getInventory().getTitle().equals(ShopManager.getMenuTitle())) {
			ShopManager.onClick(event);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			event.getItem().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(final PlayerInteractEvent event) {
		if (API.getSonic(event.getPlayer()) != null && event.getItem() != null) {
			if (event.getItem().getType().equals(Material.NETHER_STAR)) {
				Sonic sonic = API.getSonic(event.getPlayer());
				if (sonic.getCheckpoint() == 0) {
					sonic.getPlayer().teleport(API.getInstance().getSpawn());
				} else {
					CheckPoint check = API.getCheckPoint(sonic.getCheckpoint());
					sonic.getPlayer().teleport(new Location(check.getLoc1().getWorld(), (check.getLoc1().getX() + check.getLoc2().getX()) / 2, Math.min(check.getLoc1().getY(), check.getLoc2().getY()), (check.getLoc1().getZ() + check.getLoc2().getZ()) / 2));
				}
			} else if (event.getItem().getType().equals(Material.SUGAR) && !event.getItem().containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
				final Sonic sonic = API.getSonic(event.getPlayer());
				final ItemStack sugar = event.getItem(); // Sugar is in slot #3
				sugar.setAmount(sugar.getAmount() - 1);
				sugar.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
				sonic.getPlayer().getInventory().setItem(2, sugar);
				sonic.getPlayer().setWalkSpeed(0.4f);
				Bukkit.getScheduler().runTaskLater(API.getInstance(), new Runnable() {
					@Override
					public void run() {
						if (sonic.getPlayer() != null) {
							sonic.getPlayer().setWalkSpeed(0.2f);
							sugar.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
							sonic.getPlayer().getInventory().setItem(2, sugar);

						}
					}
				}, 120);
			}
		} else if (event.getItem() != null && event.getItem().getType().equals(Material.GOLD_INGOT)) {

			ShopManager.openShop(event.getPlayer());
		}
	}


	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		if (API.getSonic(event.getPlayer()) != null) {

			if (event.getTo().getBlock().getRelative(BlockFace.SELF).getType() == Material.GOLD_PLATE) {

            /* This wasn't in the tutorial but I figured people would ask in the comments
               wondering how to add sounds + effects to the launchpad. So, here you go! :P
            */
				event.getTo().getBlock().getWorld().playEffect(event.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 3);

				// Speeds up a player's velocity (makes them move somewhere faster).
				//event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(3));

            /* While the player's velocity is sped up, it keeps the same X and Y position at which they were moving to.
               The "1.0D" means that the player will "fly" in the air for the period of time at which the velocity is being
               increased.
            */
				event.getPlayer().setVelocity(new Vector(
						event.getPlayer().getLocation().getDirection().multiply(2.8).getX(),
						3.35,
						event.getPlayer().getLocation().getDirection().multiply(2.8).getZ()
				));

			}


			// is in-game
			API.ZoneVector zone = new API.ZoneVector(event.getPlayer().getLocation());
			for (CheckPoint check : API.getCheckPoints()) {
				if (zone.isInCheckpoint(check)) {
					// player is in checkpoint
					Sonic sonic = API.getSonic(event.getPlayer());
					if (sonic.getCheckpoint() + 1 == check.getId()) {
						// this is the checkpoint
						sonic.setCheckpoint(sonic.getCheckpoint() + 1);
						API.broadcast(sonic.getName() + " passed checkpoint " + check.getId() + "! (" + check.getId() + "/" + API.getCheckPoints().size() + ")");
						sonic.setTimeout(60);
						if (sonic.getCheckpoint() >= API.getCheckPoints().size()) {
							sonic.done();
						}

						API.getInstance().updateScoreboard();
					} else if (sonic.getCheckpoint() == check.getId()) {
						// already passed. do nothing
					} else {
						// shouldn't of passed this checkpoint

					}

					break;
				}
			}
		}

	}

	@EventHandler
	public void onServerPing(final ServerListPingEvent event) {
		event.setMotd(API.getInstance().getArenaState() == CraftSonic.ArenaState.INGAME ? "In-Game" : API.getInstance().currentmap);
	}

}