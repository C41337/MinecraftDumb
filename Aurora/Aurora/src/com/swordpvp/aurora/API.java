package com.swordpvp.aurora;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class API {

	private static Aurora aurora;

	public API(Aurora plugin) {
		aurora = plugin;
	}

	public static Aurora getInstance() {
		return aurora;
	}

	public static ItemStack[] getArmor(String team) {
		if (!getInstance().colorArmor || getInstance().gamemode.equals("FFA"))
			return getInstance().getArmorArray(getInstance().armor);
		List<ItemStack> armor_temp = new ArrayList<ItemStack>();
		for (ItemStack item : getInstance().armor)
			if (item != null && item.getType() != Material.AIR) {
				if (item.getType().equals(Material.LEATHER_BOOTS) || item.getType().equals(Material.LEATHER_LEGGINGS)
						|| item.getType().equals(Material.LEATHER_CHESTPLATE) || item.getType().equals(Material.LEATHER_HELMET)) {
					LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
					meta.setColor(getInstance().getAuroraTeam(team).getColor());
					item.setItemMeta(meta);
				}
				armor_temp.add(item);
			}

		return getInstance().getArmorArray(armor_temp);

	}


	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

}
