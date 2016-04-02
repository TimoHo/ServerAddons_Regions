package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.tmods.serverutils.Methods;

public class Regions extends JavaPlugin implements Listener{

	public File dataf = new File("plugins/TModsServerUtils","data.yml");
	public File langf = new File("plugins/TModsServerUtils","lang.yml");
	public File cfgf = new File("plugins/TModsServerUtils","config.yml");
	public FileConfiguration data = YamlConfiguration.loadConfiguration(dataf);
	public FileConfiguration lang = YamlConfiguration.loadConfiguration(langf);
	public FileConfiguration cfg = YamlConfiguration.loadConfiguration(cfgf);
	public HashMap<Player,Location[]> Players = new HashMap<Player,Location[]>();
	public HashMap<Player,Region> displayRegion = new HashMap<Player,Region>();
	public List<EntityType> mobs = new ArrayList<EntityType>();
	public List<EntityType> animals = new ArrayList<EntityType>();

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}

	@Override
	public void onEnable() {
		try {
		loadMobs();
		loadAnimals();
		Bukkit.getPluginManager().registerEvents(this, this);
		if (cfg.getBoolean("EnableRegionDisplay")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					try {
					if (Bukkit.getOnlinePlayers().size() > 0) {
						for (Player p:Bukkit.getOnlinePlayers()) {
							if (displayRegion.containsKey(p)) {
								displayRegion.get(p).Display();
							}
						}
					}
					} catch (Exception e) {
						Methods.log(e);
					}
				}
				
			}, 20, 20);
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				try {
				for (World w:Bukkit.getWorlds()) {
					for (Entity e:w.getEntities()) {
						if (Flags.getRegion(e.getLocation(), data, "Regions").size() > 0) {
							for (Region r:Flags.getRegion(e.getLocation(), data, "Regions")) {
								if (r.getFlags().contains(Flags.denyAnimals) && animals.contains(e.getType())) {
									e.remove();
								} else if (r.getFlags().contains(Flags.denyMobs) && mobs.contains(e.getType())) {
									e.remove();
								}
							}
						}
					}
				}
				} catch (Exception e) {
					Methods.log(e);
				}
			}
		}, 5, 5);
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
		if (cmd.getName().equalsIgnoreCase("displayRg")) {
			if (displayRegion.containsKey((Player)sender)) {
				displayRegion.remove((Player)sender);
				return true;
			}
			if (args.length != 1) {
				return false;
			}
			if (displayRegion.containsKey((Player)sender)) {
				displayRegion.remove((Player)sender);
			} else {
				if (Region.readFromConfig("Regions." + args[0], data) != null) {
					displayRegion.put((Player)sender, Region.readFromConfig("Regions." + args[0], data));
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("region")) {
			if (!sender.hasPermission("ServerAddons.regions")) {
				sender.sendMessage(lang.getString(cfg.getString("language") + ".permdeny"));
				return true;
			}
			if (args.length < 2) {
				return false;
			}
			if (args[0].equalsIgnoreCase("expand")) {
				if (args.length != 4) {
					sender.sendMessage("/region expand [region] [UP/DOWN/NORTH/WEST/EAST/SOUTH] [amount]");
					return true;
				}
				if (Region.readFromConfig("Regions." + args[1], data) != null) {
					Region r = Region.readFromConfig("Regions." + args[1], data);
					if (args[2].equalsIgnoreCase("up")) {
						r = r.setMaxY(r.getMaxY() + Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("down")) {
						r = r.setMinY(r.getMinY() - Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("north")) {
						r = r.setMinZ(r.getMinZ() - Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("south")) {
						r = r.setMaxZ(r.getMaxZ() - Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("east")) {
						r = r.setMaxX(r.getMaxX() + Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("west")) {
						r = r.setMinX(r.getMinX() - Integer.valueOf(args[3]));
					}
					Region.writeToConfig(r, data, "Regions." + args[1]);
					try {
						data.save(dataf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage("Region expanded!");
					if (displayRegion.containsKey((Player)sender)) {
						if (displayRegion.get((Player)sender) == Region.readFromConfig("Regions." + args[1], data)) {
							displayRegion.put((Player)sender, r);
						}
					}
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("contract")) {
				if (args.length != 4) {
					sender.sendMessage("/region contract [region] [UP/DOWN/NORTH/WEST/EAST/SOUTH] [amount]");
					return true;
				}
				if (Region.readFromConfig("Regions." + args[1], data) != null) {
					Region r = Region.readFromConfig("Regions." + args[1], data);
					if (args[2].equalsIgnoreCase("up")) {
						r = r.setMaxY(r.getMaxY() - Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("down")) {
						r = r.setMinY(r.getMinY() + Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("north")) {
						r = r.setMinZ(r.getMinZ() + Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("south")) {
						r = r.setMaxZ(r.getMaxZ() + Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("east")) {
						r = r.setMaxX(r.getMaxX() - Integer.valueOf(args[3]));
					}
					if (args[2].equalsIgnoreCase("west")) {
						r = r.setMinX(r.getMinX() + Integer.valueOf(args[3]));
					}
					Region.writeToConfig(r, data, "Regions." + args[1]);
					try {
						data.save(dataf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage("Region contracted!");
					if (displayRegion.containsKey((Player)sender)) {
						if (displayRegion.get((Player)sender) == Region.readFromConfig("Regions." + args[1], data)) {
							displayRegion.put((Player)sender, r);
						}
					}
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (Players.get((Player)sender) != null) {
					Region.writeToConfig(Region.calcFromLocation(Players.get((Player)sender)[0], Players.get((Player)sender)[1], (Player)sender), data, "Regions." + args[1]);
					try {
						data.save(dataf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage("Your region was created!");
				} else {
					sender.sendMessage("First select a region!");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("remove")) {
				data.set("Regions." + args[1], null);
				try {
					data.save(dataf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.sendMessage("region deleted!");
				return true;
			}
			if (args[0].equalsIgnoreCase("flag")) {
				if (args.length != 3) {
					return false;
				}
				if (Region.readFromConfig("Regions." + args[1], data) == null) {
					sender.sendMessage("unknown region!");
					return true;
				}
				if (Flags.fromString(args[2]) != null) {
					Region r = Region.readFromConfig("Regions." + args[1], data);
					List<Flags> f = r.getFlags();
					if (!f.contains(Flags.fromString(args[2]))) {
						f.add(Flags.fromString(args[2]));
					} else {
						f.remove(Flags.fromString(args[2]));
					}
					Region rg = r.setFlags(f);
					Region.writeToConfig(rg, data, "Regions." + args[1]);
					try {
						data.save(dataf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage("region flagged!");
				} else {
					sender.sendMessage("unknown flag");
					sender.sendMessage("flags:");
					sender.sendMessage("denyExplosion, denyEntry, denyExit, denyBuild, denyInteract");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("owner")) {
				if (args.length != 3) {
					return false;
				}
				if (Region.readFromConfig("Regions." + args[1], data) != null) {
					Region r = Region.readFromConfig("Regions." + args[1], data).setOwner(Bukkit.getPlayer(args[2]));
					Region.writeToConfig(r, data, "Regions." + args[1]);
					try {
						data.save(dataf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sender.sendMessage("owner set.");
					return true;
				} else {
					sender.sendMessage("this region doesn't exist!");
				}
			}
			if (args[0].equalsIgnoreCase("member")) {
				if (args.length != 4) {
					sender.sendMessage("/region member add/remove [player]");
					return true;
				}
				if (Region.readFromConfig("Regions." + args[2], data) == null) {
					sender.sendMessage("unknown region!");
					return true;
				}
				Region r = Region.readFromConfig("Regions." + args[2], data);
				List<Player> members = r.getMembers();
				if (args[1].equalsIgnoreCase("add")) {
					if (!members.contains(Bukkit.getPlayer(args[3]))) {
						members.add(Bukkit.getPlayer(args[3]));
					}
				}
				if (args[1].equalsIgnoreCase("remove")) {
					if (members.contains(Bukkit.getPlayer(args[3]))) {
						members.remove(Bukkit.getPlayer(args[3]));
					}
				}
				r.setMembers(members);
				Region.writeToConfig(r, data, "Regions." + args[2]);
				try {
					data.save(dataf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.sendMessage("members set.");
				return true;
			}
			if (args[0].equalsIgnoreCase("info")) {
				if (Region.readFromConfig("Regions." + args[1], data) != null) {
					Region r = Region.readFromConfig("Regions." + args[1], data);
					sender.sendMessage("owner: " + r.getOwner().getName());
					if (r.getMembers().size() > 0) {
						for (Player p:r.getMembers()) {
							sender.sendMessage("member: " + p.getName());
						}
					}
					sender.sendMessage("Flags: ");
					if (r.getFlags().size() > 0) {
						for (Flags f:r.getFlags()) {
							sender.sendMessage(f.getName());
						}
					}
					return true;
				} else {
					sender.sendMessage("unknown region!");
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("rwand") && sender.hasPermission("ServerAddons.regions")) {
			if (!sender.hasPermission("ServerAddons.regions")) {
				sender.sendMessage(lang.getString(cfg.getString("language") + ".permdeny"));
				return true;
			}
			if (sender instanceof Player) {
				Player p = (Player)sender;
				if (Players.get(p) != null) {
					Players.remove(p);
				} else {
					ItemStack regionwand = new ItemStack(Material.DIAMOND_HOE);
					ItemMeta meta = regionwand.getItemMeta();
					meta.setDisplayName("Region Wand");
					regionwand.setItemMeta(meta);
					p.getInventory().addItem(regionwand);
				}
				return true;
			}			
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return false;
	}
	@EventHandler
	public void onMoveEvent(PlayerMoveEvent e) {
		try {
		if (Flags.getRegion(e.getTo(), data, "Regions") != null) {
			for (Region r:Flags.getRegion(e.getPlayer().getLocation(), data, "Regions")) {
				if (r.getFlags().contains(Flags.denyEntry)) {
					if (r.isIn(e.getTo())) {
						if (e.getPlayer() != r.getOwner() && !r.getMembers().contains(e.getPlayer())) {
							e.getPlayer().setVelocity(e.getTo().toVector().subtract(e.getFrom().toVector()).multiply(-1));
						}
					}
				}
			}
		}
		if (Flags.getRegion(e.getFrom(), data, "Regions") != null) {
			for (Region r:Flags.getRegion(e.getPlayer().getLocation(), data, "Regions")) {
				if (r.getFlags().contains(Flags.denyExit)) {
					if (r.isIn(e.getFrom()) && !r.isIn(e.getTo())) {
						if (e.getPlayer() != r.getOwner() && !r.getMembers().contains(e.getPlayer())) {
							e.getPlayer().setVelocity(e.getTo().toVector().subtract(e.getFrom().toVector()).multiply(-2));
						}
					}
				}
			}
		}
		} catch (Exception e1) {
			Methods.log(e1);
		}
	}
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		try {
		if (Flags.getRegion(e.getEntity().getLocation(), data, "Regions") != null) {
			for (Region r:Flags.getRegion(e.getEntity().getLocation(), data, "Regions")) {
				if (r.getFlags().contains(Flags.denyExplosion)) {
					if (r.isIn(e.getEntity().getLocation())) {
							e.setCancelled(true);
					}
				}
			}
		}
		} catch (Exception e1) {
			Methods.log(e1);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		try {
		if (event.getClickedBlock() != null) {
			if (Flags.getRegion(event.getClickedBlock().getLocation(), data, "Regions").size() > 0) {
				for (Region r:Flags.getRegion(event.getClickedBlock().getLocation(), data, "Regions")) {
					if ((event.getAction() == Action.PHYSICAL || event.getAction() == Action.RIGHT_CLICK_BLOCK) && r.getFlags().contains(Flags.denyInteract) && r.getOwner() != event.getPlayer() && !r.getMembers().contains(event.getPlayer())) {
						event.setCancelled(true);
					}
					if (r.getFlags().contains(Flags.denyBuild) && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && r.getOwner() != event.getPlayer() && !r.getMembers().contains(event.getPlayer())) {
						event.setCancelled(true);
					}
				}
			}
		}
		if (event.getClickedBlock() != null) {
			if (Methods.getItemInHand(event.getPlayer()) != null) {
				if (Methods.getItemInHand(event.getPlayer()).getType() == Material.DIAMOND_HOE) {
					if (Methods.getItemInHand(event.getPlayer()).hasItemMeta()) {
						if (Methods.getItemInHand(event.getPlayer()).getItemMeta().getDisplayName().equalsIgnoreCase("Region Wand")) {
							event.setCancelled(true);
							if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
								Players.put(event.getPlayer(), new Location[]{event.getClickedBlock().getLocation(),(Players.get(event.getPlayer()) != null ? Players.get(event.getPlayer())[1]:event.getClickedBlock().getLocation())});
								event.getPlayer().sendMessage("Pos1 set.");
								if (Players.get(event.getPlayer())[0] != null && Players.get(event.getPlayer())[1] != null) {
									displayRegion.put(event.getPlayer(), Region.calcFromLocation(Players.get(event.getPlayer())[0], Players.get(event.getPlayer())[1], event.getPlayer()));
									event.getPlayer().sendMessage("Your Region is now displayed type /displayrg to disable.");
								}
							}
							if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
								Players.put(event.getPlayer(), new Location[]{(Players.get(event.getPlayer()) != null ? Players.get(event.getPlayer())[0]:event.getClickedBlock().getLocation()),event.getClickedBlock().getLocation()});
								event.getPlayer().sendMessage("Pos1 set.");
								if (Players.get(event.getPlayer())[0] != null && Players.get(event.getPlayer())[1] != null) {
									displayRegion.put(event.getPlayer(), Region.calcFromLocation(Players.get(event.getPlayer())[0], Players.get(event.getPlayer())[1], event.getPlayer()));
									event.getPlayer().sendMessage("Your Region is now displayed type /displayrg to disable.");
								}
							}
						}
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	public void loadAnimals() {
		animals = Methods.getAnimals();
	}
	public void loadMobs() {
		mobs = Methods.getMobs();
	}
}
