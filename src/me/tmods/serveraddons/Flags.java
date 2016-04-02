package me.tmods.serveraddons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import me.tmods.serverutils.Methods;

public enum Flags {
	denyEntry("denyEntry"),
	denyExit("denyExit"),
	denyExplosion("denyExplosion"),
	denyInteract("denyInteract"),
	denyBuild("denyBuild"),
	denyMobs("denyMobs"),
	denyAnimals("denyAnimals");
	
	private String name;
	
	private Flags(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static List<Flags> getFlags(FileConfiguration config,String path) {
		try {
		List<Flags> flags = new ArrayList<Flags>();
		if (config.getConfigurationSection(path + ".flags") != null) {
			if (config.getConfigurationSection(path + ".flags").getKeys(false).size() > 0) {
				for (String s:config.getConfigurationSection(path + ".flags").getKeys(false)) {
					flags.add(fromString(s));
				}
			}
		}
		return flags;
		} catch (Exception e) {
			Methods.log(e);
		}
		return null;
	}
	
	public static Flags fromString(String s) {
		try {
		if (s.equals(denyEntry.getName())) {
			return denyEntry;
		}
		if (s.equals(denyExit.getName())) {
			return denyExit;
		}
		if (s.equals(denyExplosion.getName())) {
			return denyExplosion;
		}
		if (s.equals(denyInteract.getName())) {
			return denyInteract;
		}
		if (s.equals(denyBuild.getName())) {
			return denyBuild;
		}
		if (s.equals(denyAnimals.getName())) {
			return denyAnimals;
		}
		if (s.equals(denyMobs.getName())) {
			return denyMobs;
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return null;
	}
	
	public static List<Region> getRegion(Location loc,FileConfiguration rgconfig,String regionpath) {
		try {
		if (rgconfig.getConfigurationSection(regionpath) != null) {
			if (rgconfig.getConfigurationSection(regionpath).getKeys(false).size() > 0) {
				List<Region> regions = new ArrayList<Region>();
				for (String s:rgconfig.getConfigurationSection(regionpath).getKeys(false)) {
					if (Region.readFromConfig(regionpath + "." + s, rgconfig).isIn(loc)) {
						regions.add(Region.readFromConfig(regionpath + "." + s, rgconfig));
					}
				}
				if (regions.size() > 0) {
					return regions;
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return new ArrayList<Region>();
	}
}
