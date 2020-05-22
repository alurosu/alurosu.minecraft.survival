package alurosu.minecraft.survival;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class plugin extends JavaPlugin {	
	private static String db_user;
	private static String db_pass;
	private static String db_url;
	
	static Connection connection;
	static String help;
	static double tax;
	
	private listener l = new listener();
    @Override
    public void onEnable() {
		this.saveDefaultConfig();
    	FileConfiguration config = this.getConfig();
    	
    	db_user = config.getString("mysql.user");
    	db_pass = config.getString("mysql.pass");
		db_url = "jdbc:mysql://"+config.getString("mysql.url")+":3306/" + config.getString("mysql.name")+"?useSSL=false";
		tax = config.getDouble("tax");
    	
        getServer().getPluginManager().registerEvents(l, this);
        
		help = "How to use §bsouls§f:";
		help += "\n/souls §7- shows how many souls you have§f";
		help += "\n/souls §6give§f [player] [quantity] §7- give souls to another player§f";
		help += "\n/souls §6buy§f [quantity] §7- trade levels for souls§f";
		help += "\n/souls §6sell§f [quantity] §7- trade souls for levels; tax: "+tax+"%";
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("jdbc driver unavailable!");
            return;
        }
        try {
            connection = DriverManager.getConnection(db_url,db_user,db_pass);
            loadClaimsFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
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
    	        		String sql = "SELECT pass, active, id FROM users WHERE user='"+p.getName()+"'";
    	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
    	        		
    	        		if (results.first()) {
    	        			if (results.getInt(2) == 0)
    	        				p.sendMessage("Your account is §6inactive§f. Please contact an admin on discord.");
    	        			else if (getMD5Hash(args[0]).equals(results.getString(1))) {
    	        				claims.playerID.put(p, results.getInt(3));
    	                		l.doLogin(p);
    	                		
    	                		// give items
    	                		String items_sql = "SELECT item, count FROM login_items WHERE user='"+p.getName()+"'";
    	    	        		ResultSet items_results = connection.prepareStatement(items_sql).executeQuery();
    	    	        		
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
    	    	        		
    	    					p.sendMessage("§aSuccess! §fWelcome back, §6"+p.getName());
    	    					p.sendMessage(m);
    	        		    } else p.sendMessage("§cInvalid password. §fPlease try again.");
    	        		} else p.sendMessage("You need an account. Please register at §aamongdemons.com");
    				} catch (SQLException e) {
    					e.printStackTrace();
    				} catch (NoSuchAlgorithmException e) {
    					e.printStackTrace();
    				}
            	}	
        	}
        } else if (label.equalsIgnoreCase("claim")) {
        	p.sendMessage("You can §6claim §fa chunk by placing an §aEmerald Block");
        } else if (label.equalsIgnoreCase("trust")) {
        	if (args.length==1) {
				if (!args[0].equals(p.getName())) {
					try {
						connection = getConnection();
						
						String sql = "SELECT id FROM users WHERE user='"+args[0]+"'";
    	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
    	        		
    	        		if (results.first()) {
    	        			p.sendMessage(claims.addTrustedUser(p, args[0], results.getInt(1)));
    	        		} else p.sendMessage("§6"+args[0]+"§f doesn't exist.");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else p.sendMessage("You can't trust yourself ..");
        	} else p.sendMessage("Give your friend access by typing: /trust [user]");
        } else if (label.equalsIgnoreCase("untrust")) {
        	if (args.length==1) {
				if (!args[0].equals(p.getName())) {
					try {
						connection = getConnection();
						
						String sql = "SELECT id FROM users WHERE user='"+args[0]+"'";
    	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
    	        		
    	        		if (results.first()) {
    	        			p.sendMessage(claims.removeTrustedUser(p, args[0], results.getInt(1)));
    	        		} else p.sendMessage("§6"+args[0]+"§f doesn't exist.");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else p.sendMessage("You can always trust yourself!");
        	} else p.sendMessage("Remove user access by typing: /trust [user]");
        } else if (label.equalsIgnoreCase("kits") || label.equalsIgnoreCase("kit")) {
        	p.sendMessage("You can get §6kits §fwith §bsouls §fat §aamongdemons.com");
        } else if (label.equalsIgnoreCase("souls") || label.equalsIgnoreCase("s")) {
        	if (l.isLoggedIn(p)) {
        		if (args.length == 0) {
	    			try {
	    	        	connection = getConnection();
	    	    		String sql = "SELECT souls FROM users WHERE user='"+p.getName()+"'";
	    	    		ResultSet results;
	    				results = connection.prepareStatement(sql).executeQuery();
	    	    		if (results.first()) {
	    	    			p.sendMessage("You have "+displaySouls(results.getInt(1)));
	    	    		}
	    			} catch (SQLException e) {
	    				e.printStackTrace();
	    			}
        		} else {
        			Boolean showError = false;
        			
        			if (args[0].equals("give")) {
        				// give souls
        				if (args.length == 3) {
        					if (!args[1].equals(p.getName())) {
	        					int amount = Integer.parseInt(args[2]);
	        					if (amount > 0) {
	        						try {
		        						connection = getConnection();
		        						
		        						String sql = "SELECT souls FROM users WHERE user='"+p.getName()+"'";
		            	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
		            	        		
		            	        		if (results.first()) {
		            	        			if (results.getInt(1) >= amount) {
				        						String target_sql = "SELECT souls FROM users WHERE user='"+args[1]+"'";
				            	        		ResultSet target_results = connection.prepareStatement(target_sql).executeQuery();
				            	        		if (target_results.first()) {
					        						String update = "UPDATE users SET souls = souls - "+amount+" WHERE user='"+p.getName()+"'";
													connection.prepareStatement(update).execute();
					        						update = "UPDATE users SET souls = souls + "+amount+" WHERE user='"+args[1]+"'";
													connection.prepareStatement(update).execute();
													
					        						p.sendMessage("You gave "+displaySouls(amount)+" to §6"+args[1]+"§f");
				            	        		} else p.sendMessage("The user §6"+args[1]+"§f does not exist");
		            	        			} else p.sendMessage("You don't have enough §bsouls.");
		            	        		}
									} catch (SQLException e) {
										e.printStackTrace();
									}
	        					} else p.sendMessage("[quantity] needs to be over §60");
        					} else p.sendMessage("You can't send §bsouls§f to yourself");
        				} else showError = true;
        			} else if (args[0].equals("buy")) {       				
        				if (args.length == 2) {
	        				if (args[1].matches("\\-?\\d+")) {
	        					int amount = Integer.parseInt(args[1]);
	        					if (amount > 0) {
		        					int level = p.getLevel();
		        					level -= amount;
		        					if (level >= 0) {
		    	    	                try {
			        						connection = getConnection();
			        						
			        						String update = "UPDATE users SET souls = souls + "+amount+" WHERE user='"+p.getName()+"'";
											connection.prepareStatement(update).execute();
										} catch (SQLException e) {
											e.printStackTrace();
										}
		        						
		        						p.setLevel(level);
		        						p.sendMessage("You bought "+displaySouls(amount));
		        					} else p.sendMessage("You need §6"+amount+"§f levels to buy "+displaySouls(amount));
	        					} else p.sendMessage("[quantity] needs to be over §60");
	        				} else showError = true;
        				} else showError = true;
        			} else if (args[0].equals("sell")) {
        				if (args.length == 2) {
	        				if (args[1].matches("\\-?\\d+")) {
	        					int amount = Integer.parseInt(args[1]);
	        					if (amount > 1) {
	    	    	                try {
		        						connection = getConnection();
		        						
		        						String sql = "SELECT souls FROM users WHERE user='"+p.getName()+"'";
		            	        		ResultSet results = connection.prepareStatement(sql).executeQuery();
		            	        		
		            	        		if (results.first()) {
		            	        			if (results.getInt(1) >= amount) {
					        					int level = p.getLevel();
					        					int diff = (int)(amount-amount*tax/100);
					        					level += diff;
					        					p.setLevel(level);

				        						String update = "UPDATE users SET souls = souls - "+amount+" WHERE user='"+p.getName()+"'";
												connection.prepareStatement(update).execute();
				        						p.sendMessage("You sold "+displaySouls(amount)+" for §6"+diff+"§f levels");
		            	        			} else p.sendMessage("You don't have enough §bsouls.");
		            	        		}
									} catch (SQLException e) {
										e.printStackTrace();
									}
	        					} else p.sendMessage("[quantity] needs to be over §61");
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
    
    public static void addClaimToDB(String c, String access) {    	
    	try {
			connection = getConnection();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			connection.prepareStatement("INSERT INTO claims (chunk, access, modified) VALUES ('"+c+"', '"+access+"', '"+timestamp.getTime()+"')").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static void removeClaimFromDB(String c) {    	
    	try {
			connection = getConnection();			
			connection.prepareStatement("DELETE FROM claims WHERE chunk = '"+c+"'").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static void updateClaimInDB(String c, String access) {    	
    	try {
			connection = getConnection();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			connection.prepareStatement("UPDATE claims SET access = '"+access+"', modified = '"+timestamp.getTime()+"' WHERE chunk = '"+c+"'").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static void loadClaimsFromDB() {
    	try {
			connection = getConnection();
			
			String items_sql = "SELECT chunk, access FROM claims";
			ResultSet items_results = connection.prepareStatement(items_sql).executeQuery();
			
			while(items_results.next()){
				claims.addClaim(items_results.getString(1), items_results.getString(2));
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}
