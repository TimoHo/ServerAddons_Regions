package me.tmods.serveraddons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.tmods.serverutils.Methods;

public class Region {
	
	private World world;
	private Double maxX;
	private Double maxZ;
	private Double maxY;
	private Double minX;
	private Double minZ;
	private Double minY;
	private OfflinePlayer owner;
	private List<Player> members;
	private List<Flags> flags;
	
	public Region(World world,Location max,Location min,OfflinePlayer rgowner,List<Player> members, List<Flags> flags) {
		this.world = world;
		this.maxX = max.getX();
		this.maxY = max.getY();
		this.maxZ = max.getZ();
		this.minX = min.getX();
		this.minY = min.getY();
		this.minZ = min.getZ();
		this.owner = rgowner;
		this.members = members;
		this.flags = flags;
	}
	
	public Region(Player owner) {
		this.world = owner.getWorld();
		this.maxX = (double) -300000000;
		this.maxY = (double) -100;
		this.maxZ = (double) -300000000;
		this.minX = (double) 300000000;
		this.minY = (double) 300;
		this.minZ = (double) 300000000;
		this.owner = owner;
		this.members = new ArrayList<Player>();
		this.flags = new ArrayList<Flags>();
	}

	
	public static Region calcFromLocation(Location pos1,Location pos2, Player owner) {
		try {
		Double maxx = Math.max(pos1.getX(), pos2.getX());
		Double maxy = Math.max(pos1.getY(), pos2.getY());
		Double maxz = Math.max(pos1.getZ(), pos2.getZ());
		Double minx = Math.min(pos1.getX(), pos2.getX());
		Double miny = Math.min(pos1.getY(), pos2.getY());
		Double minz = Math.min(pos1.getZ(), pos2.getZ());
		Location max = new Location(pos1.getWorld(),maxx,maxy,maxz);
		Location min = new Location(pos1.getWorld(),minx,miny,minz);
		return new Region(pos1.getWorld(),max,min,owner,new ArrayList<Player>(),new ArrayList<Flags>());
		} catch (Exception e) {
			Methods.log(e);
		}
		return null;
	}
	
	public void Display() {
		try {
		for (double x = minX;x <= maxX;x++) {
			for (double y = minY;y <= maxY;y++) {
				for (double z = minZ;z <= maxZ;z++) {
					if (x == maxX || x == minX || y == maxY || y == minY || z == maxZ || z == minZ) {
						Location loc = new Location(world,x,y,z);
						Methods.playEffect(loc, Particle.REDSTONE, 0, 1, true);
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	
	public Region setFlags(List<Flags> flags) {
		this.flags = flags; return this;
	}
	public List<Flags> getFlags() {
		return this.flags;
	}
	public Location getMaxLoc() {
		return new Location(this.world,this.maxX,this.maxY,this.maxZ);
	}
	public Location getMinLoc() {
		return new Location(this.world,this.minX,this.minY,this.minZ);
	}
	public Region setMaxLoc(Location loc) {
		this.maxX = loc.getX();
		this.maxY = loc.getY();
		this.maxZ = loc.getZ();
		return this;
	}
	public Region setMinLoc(Location loc) {
		this.minX = loc.getX();
		this.minY = loc.getY();
		this.minZ = loc.getZ();
		return this;
	}
	public Region setMaxX(Double value) {
		this.maxX = value; return this;
	}
	public Region setMaxY(Double value) {
		this.maxY = value; return this;
	}
	public Region setMaxZ(Double value) {
		this.maxZ = value; return this;
	}
	public Region setMinX(Double value) {
		this.minX = value; return this;
	}
	public Region setMinY(Double value) {
		this.minY = value; return this;
	}
	public Region setMinZ(Double value) {
		this.minZ = value; return this;
	}
	public Region setOwner(Player value) {
		this.owner = value; return this;
	}
	public void setMembers(List<Player> value) {
		this. members = value;
	}
	public double getMaxX() {
		return maxX;
	}
	public double getMaxY() {
		return maxY;
	}
	public double getMaxZ() {
		return maxZ;
	}
	public double getMinX() {
		return minX;
	}
	public double getMinY() {
		return minY;
	}
	public double getMinZ() {
		return minZ;
	}
	public OfflinePlayer getOwner() {
		return owner;
	}
	public List<Player> getMembers() {
		return members;
	}
	public World getWorld() {
		return world;
	}
	
	public boolean isIn(Location loc) {
		try {
		if (loc.getX() > minX && loc.getX() < maxX) {
			if (loc.getY() > minY && loc.getY() < maxY) {
				if (loc.getZ() > minZ && loc.getZ() < maxZ) {					
					return true;
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static void writeToConfig(Region r,FileConfiguration config,String path) {
		try {
		if (config.getConfigurationSection(path) != null) {
			config.set(path, null);
		}
		config.set(path + ".maxx", r.getMaxX());
		config.set(path + ".maxy", r.getMaxY());
		config.set(path + ".maxz", r.getMaxZ());
		config.set(path + ".minx", r.getMinX());
		config.set(path + ".miny", r.getMinY());
		config.set(path + ".minz", r.getMinZ());
		config.set(path + ".owner", r.getOwner().getName());
		config.set(path + ".world", r.getWorld().getName());
		List<String> membernames = new ArrayList<String>();
		if (r.getMembers().size() > 0) {
			for (Player p:r.getMembers()) {
				if (Bukkit.getOfflinePlayer(p.getName()) != null) {
					membernames.add(p.getName());
				}
			}
		}
		if (r.getFlags().size() > 0) {
			for (Flags f:r.getFlags()) {
				config.set(path + ".flags." + f.getName(), true);
			}
		}
		config.set(path + ".members", membernames);
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@SuppressWarnings("deprecation")
	public static Region readFromConfig(String path,FileConfiguration config) {
		try {
		List<Player> memberlist = new ArrayList<Player>();
		for (String s:config.getStringList(path + ".members")) {
			memberlist.add(Bukkit.getPlayer(s));
		}
		OfflinePlayer rgowner = Bukkit.getOfflinePlayer(config.getString(path + ".owner"));
		Region r = new Region(Bukkit.getWorld(config.getString(path + ".world")),new Location(Bukkit.getWorld(config.getString(path + ".world")),config.getDouble(path + ".maxx"),config.getDouble(path + ".maxy"),config.getDouble(path + ".maxz")),new Location(Bukkit.getWorld(config.getString(path + ".world")),config.getDouble(path + ".minx"),config.getDouble(path + ".miny"),config.getDouble(path + ".minz")),rgowner,memberlist,Flags.getFlags(config,path));
		return r;
		} catch (Exception e) {
			Methods.log(e);
		}
		return null;
	}
}
