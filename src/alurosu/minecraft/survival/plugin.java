package alurosu.minecraft.survival;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import net.milkbowl.vault.economy.Economy;

public class plugin extends JavaPlugin {	
	private static String db_user;
	private static String db_pass;
	private static String db_url;
	private static DynmapAPI dapi;
	private static MarkerSet player_markers;
	
	static Connection connection;
	static String help;
	static int soulsAt10minutes;
	static int markerCost;
	
    public static Economy provider;
    public PluginManager pluginManager;
	
	private listener l = new listener();
	
	
	@Override
    public void onLoad() {
        pluginManager = getServer().getPluginManager();
        
        hookVault();
    }
	
    @Override
    public void onEnable() {
		this.saveDefaultConfig();
    	FileConfiguration config = this.getConfig();
    	
    	db_user = config.getString("mysql.user");
    	db_pass = config.getString("mysql.pass");
		db_url = "jdbc:mysql://"+config.getString("mysql.url")+":3306/" + config.getString("mysql.name")+"?useSSL=false&autoReconnect=true";
		soulsAt10minutes = config.getInt("soulsAt10minutes");
		markerCost = config.getInt("markerCost");
    	
		pluginManager.registerEvents(l, this);
        
		help = "How to use §bsouls§f:";
		help += "\n/souls §7- shows how many souls you have§f";
		help += "\n/souls §6give§f [player] [quantity] §7- give souls to another player§f";
		help += "\n/souls §6convert§f [quantity] §7- trade souls for levels";
		help += "\n§fNeed §bsouls§f? Click the link to learn more: \n§aamongdemons.com/minecraft/server-info/#get-souls";
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("[amongdemons.com] jdbc driver unavailable!");
            return;
        }
        try {
            connection = DriverManager.getConnection(db_url,db_user,db_pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	String in = "(";
            	for(Player p: Bukkit.getOnlinePlayers()){
            		if (l.isLoggedIn.get(p)) {
    					in += "'"+p.getName()+"',";
            			p.sendTitle("", "                                                §6+"+soulsAt10minutes+" §bsouls§f", 10, 70, 10);
            		}
        		}
            	in += "'')";
            	
            	try {
        	    	plugin.connection = plugin.getConnection();

        	    	// update souls
        	    	String sql = "UPDATE users SET souls = souls + "+soulsAt10minutes+" WHERE user IN "+in;
            		plugin.connection.prepareStatement(sql).execute();
                        
        		} catch (SQLException e) {
        			e.printStackTrace();
        		}
            }
        }, 0L, 12000L); // 20L = 1 sec - 12000L = 10 min
        
	    dapi = (DynmapAPI) pluginManager.getPlugin("dynmap");
	    if (dapi!=null) {
	    	player_markers = dapi.getMarkerAPI().getMarkerSet("markers");
	    }
    }
    
    public void hookVault() {
        if (pluginManager.getPlugin("Vault") == null) {
        	System.out.println("[amongdemons.com] Vault isn't installed - this somehow got past the compatibility check - hook task aborted.");
        } else {
            provider = new myeconomy();
            Bukkit.getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
            System.out.println("[amongdemons.com] Hooked to Vault successfuly.");
        }
    }

    public void unhookVault() {
        if (pluginManager.getPlugin("Vault") == null) {
        	System.out.println("[amongdemons.com] Vault isn't installed - unhook task aborted.");
        } else {
            Bukkit.getServicesManager().unregister(Economy.class, provider);
            System.out.println("[amongdemons.com] Unhooked from Vault successfully.");
        }
    }
    
    public static Connection getConnection() {
        try {
			if (connection == null || connection.isClosed()) {
			    connection = DriverManager.getConnection(db_url,db_user,db_pass);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return connection;
    }
    
    @Override
    public void onDisable() {
        try {
            if (connection!=null && !connection.isClosed()){ 
                connection.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        unhookVault();
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Player p = (Player) sender;
    	if (label.equalsIgnoreCase("login") || label.equalsIgnoreCase("l")) {
        	if (l.isLoggedIn(p))
        		p.sendMessage("You are already logged in.");
        	else {
        		if (args.length == 0)
            		p.sendMessage("Please enter your password: /login [pass]");
            	else {
    				try {
    					connection = getConnection();
    	        		String sql = "SELECT pass, active, souls FROM users WHERE user='"+p.getName()+"'";
    	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
    	        		
    	        		if (results.first()) {
    	        			if (results.getInt(2) == 0)
    	        				p.sendMessage("Your account is §6inactive§f. Please contact an admin on discord.");
    	        			else if (getMD5Hash(args[0]).equals(results.getString(1))) {    	        				
    	                		l.doLogin(p);
    	        				p.sendMessage("§aSuccess! §fWelcome back, §6"+p.getName());
    	        				
    	                		giveKitItems(p, true);
    	        		    } else p.sendMessage("§cInvalid password. §fPlease try again.");
    	        		} else p.sendMessage("We didn't find any account with your username: §6"+p.getName()+"§f. Change your username or register at §aamongdemons.com");
    				} catch (SQLException e) {
    					e.printStackTrace();
    				} catch (NoSuchAlgorithmException e) {
    					e.printStackTrace();
    				}
            	}	
        	}
        } else if (label.equalsIgnoreCase("addmarker")) {
        	if (l.isLoggedIn(p)) {
        		if (dapi != null) {
        			if (provider.has(p, markerCost)) {
	        			String id = p.getName() + "-" + (int)p.getLocation().getX() + "-" + (int)p.getLocation().getZ() + "-" + p.getLocation().getWorld().getName();
	        			String msg = "";
	                    for (int i = 0; i < args.length; i++) {
	                        msg = msg + args[i] + " ";
	                    }

	        			MarkerIcon icon = dapi.getMarkerAPI().getMarkerIcon("comment");
	        			player_markers.createMarker(id, msg, p.getLocation().getWorld().getName(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), icon, true);
	        			
	        			provider.withdrawPlayer(p, markerCost);
	        			p.sendMessage("§aSuccess! §fMap marker created.");
        			} else p.sendMessage("A map marker costs §6"+markerCost+" §bsouls§f. You need more.");
        		} else p.sendMessage("Dynmap API not connected.");
        	} else l.loginMessage(p);
        } else if (label.equalsIgnoreCase("delmarker") || label.equalsIgnoreCase("deletemarker") || label.equalsIgnoreCase("removemarker")) {
        	if (l.isLoggedIn(p)) {
        		if (dapi != null) {
        			String id = p.getName() + "-" + (int)p.getLocation().getX() + "-" + (int)p.getLocation().getZ() + "-" + p.getLocation().getWorld().getName();
        			Marker f = player_markers.findMarker(id);
        			
        			if (f != null) {
	        			f.deleteMarker();
	        			provider.depositPlayer(p, markerCost);
	        			p.sendMessage("Map marker deleted. You received §6"+markerCost+" §bsouls");
        			} else p.sendMessage("Can't find a marker at your location.");
        		} else p.sendMessage("Dynmap API not connected.");
        	} else l.loginMessage(p);
        } else if (label.equalsIgnoreCase("claimkit") || label.equalsIgnoreCase("claimkits")) {
        	if (l.isLoggedIn(p)) {
        		giveKitItems(p, false);
        	} else l.loginMessage(p);
        } else if (label.equalsIgnoreCase("kits") || label.equalsIgnoreCase("kit") || label.equalsIgnoreCase("shop")) {
        	p.sendMessage("You can trade §bsouls §ffor §6kits §fat §aamongdemons.com");
        } else if (label.equalsIgnoreCase("help") || label.equalsIgnoreCase("info")) {
        	p.sendMessage("Server §6info §fat §aamongdemons.com/minecraft/server-info");
        } else if (label.equalsIgnoreCase("map")) {
        	p.sendMessage("Need a map? We have one at §aamongdemons.com");
        } else if (label.equalsIgnoreCase("register") || label.equalsIgnoreCase("reg")) {
        	p.sendMessage("Register on our website: §aamongdemons.com");
        } else if (label.equalsIgnoreCase("souls") || label.equalsIgnoreCase("s")) {
        	if (l.isLoggedIn(p)) {
        		if (args.length == 0) {
	    			p.sendMessage("You have "+displaySouls((int)provider.getBalance(p)));
        		} else {
        			Boolean showError = false;
        			
        			if (args[0].equals("give")) {
        				// give souls
        				if (args.length == 3) {
    						try {
	        					if (!args[1].equals(p.getName())) {
		        					int amount = Integer.parseInt(args[2]);
		        					if (amount > 0) {
		        						if (provider.has(p,  amount)) {
				        						connection = getConnection();
				        						
				        						String target_sql = "SELECT souls FROM users WHERE user='"+args[1]+"'";
				            	        		ResultSet target_results = connection.prepareStatement(target_sql).executeQuery();
				            	        		if (target_results.first()) {
				            	        			provider.withdrawPlayer(p, amount);
													String update = "UPDATE users SET souls = souls + "+amount+" WHERE user='"+args[1]+"'";
													connection.prepareStatement(update).execute();
													
					        						p.sendMessage("You gave "+displaySouls(amount)+" to §6"+args[1]+"§f");
					        						
					        						Player receiver = Bukkit.getPlayer(args[1]);
			        								if (receiver != null) {
			        									receiver.sendMessage("§6"+p.getName()+"§f gave you "+displaySouls(amount));
			        								}
				            	        		} else p.sendMessage("The user §6"+args[1]+"§f does not exist");
		        						} else p.sendMessage("You don't have enough §bsouls.");
		        					} else p.sendMessage("[quantity] needs to be over §60");
	        					} else p.sendMessage("You can't send §bsouls§f to yourself");
    						} catch (Exception e) {
								showError = true;
								e.printStackTrace();
							}
        				} else showError = true;
        			} else if (args[0].equals("buy")) {       				
        				p.sendMessage("Feature changed: You now get §6"+soulsAt10minutes+" §bsouls§f every 10 minutes.");
        			} else if (args[0].equals("sell") || args[0].equals("convert")) {
        				if (args.length == 2) {
	        				if (args[1].matches("\\-?\\d+")) {
	        					int amount = Integer.parseInt(args[1]);
	        					if (amount > 0) {
	        						if (provider.has(p, amount)) {
			        					provider.withdrawPlayer(p, amount);
			        					
			        					int level = p.getLevel();
			        					level += amount;
			        					p.setLevel(level);
			        					
			        					p.sendMessage("You converted "+displaySouls(amount)+" for §6"+amount+"§f levels");
	        						} else p.sendMessage("You don't have enough §bsouls.");
	        					} else p.sendMessage("[quantity] needs to be over §60");
	        				} else showError = true;
        				} else showError = true;
        			} else showError = true;
        			
        			if (showError)
        				p.sendMessage(help);
        		}
        	} else l.loginMessage(p);
        }
		return true;
    }
    
    public static String displaySouls(int amount) {
    	if (amount==1)
    		return "§6"+amount+" §bsoul§f";
    	return "§6"+amount+" §bsouls§f";
    }
    
    public static String getMD5Hash(String s) throws NoSuchAlgorithmException {
    	String result = s;
    	if (s != null) {
    	    MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
    	    md.update(s.getBytes());
    	    BigInteger hash = new BigInteger(1, md.digest());
    	    result = hash.toString(16);
    	    while (result.length() < 32) { // 40 for SHA-1
    	        result = "0" + result;
    	    }
    	}
    	return result; 
    }
    
    public static void reloadConnection(Player p) {
    	provider.getBalance(p);
    }
    
    public void giveKitItems(Player p, boolean isLogin) {
    	try {
    		// give items
    		String items_sql = "SELECT item, count FROM login_items WHERE user='"+p.getName()+"'";
    		ResultSet items_results = connection.prepareStatement(items_sql).executeQuery();
    		
    		int total = 0;
    		if(items_results.last()) {
    			total = items_results.getRow();
    			items_results.beforeFirst();
    		}
    		
    		if (total == 0 && !isLogin)
    			p.sendMessage("You have no kits to claim.\nGet some from §aamongdemons.com/minecraft/kits/");
    		
    		if (hasAvaliableSlot(p, total)) {
        		String m = "";
        		while(items_results.next()){    	    	        	        
        			p.getInventory().addItem(new ItemStack(Material.getMaterial(items_results.getString(1)), items_results.getInt(2)));
        			
        			if (m.isEmpty())
        				m = "You received: ";
        			else
        				m += ", ";
        			m += "§6"+items_results.getInt(2)+" §f"+items_results.getString(1);
                }
        		
        		String cleanup = "DELETE FROM login_items WHERE user='"+p.getName()+"'";
                connection.prepareStatement(cleanup).execute();
                
				if (!m.equals(""))
					p.sendMessage(m);
    		
    		} else p.sendMessage("§cNo space for kit items.\n§fClear your inventory and type: /claimkits");
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public boolean hasAvaliableSlot(Player player,int howmanyclear){
    	ItemStack[] inv = player.getInventory().getStorageContents();
    	int check=0;
    	for (ItemStack item: inv) {
	    	if(item == null) {
	    		check++;
	    	}
    	}

    	if (check>=howmanyclear) {
    		return true;
    	} else {
    		return false;
    	}
	}
}
