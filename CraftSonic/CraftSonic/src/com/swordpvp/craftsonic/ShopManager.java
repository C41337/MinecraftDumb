package com.swordpvp.craftsonic;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

	@Getter
	private static String menuTitle = ChatColor.BLACK + "Shop";
	@Getter
	private static List<ShopItem> shopItems = new ArrayList<ShopItem>();

	public static Inventory getShop(Player player) {
		Inventory inv = Bukkit.createInventory(null, shopItems.size() + 9 - shopItems.size() % 9, menuTitle);

		for (ShopItem item : shopItems) {
			inv.addItem(item.getItem(player));
		}
		return inv;
	}

	public static void openShop(Player sonic) {
		sonic.openInventory(getShop(sonic));
	}

	public static void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getDisplayName() != null && event.getCurrentItem().getItemMeta().getDisplayName() != "") {
			Player sonic = (org.bukkit.entity.Player) event.getWhoClicked();
			for (ShopItem item : shopItems) {
				if (item.getItem(sonic).getType() == event.getCursor().getType()) {
					item.open(sonic);
				}
				break;
			}
			event.setCancelled(true);
			sonic.getPlayer().closeInventory();
		}
	}

	public static interface ShopItem {
		public ItemStack getItem(Player player);

		public void open(Player player);
	}

	public static void addShopItem(ShopItem item) {
		shopItems.add(item);
	}

}
