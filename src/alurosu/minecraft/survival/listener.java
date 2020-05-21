package alurosu.minecraft.survival;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class listener implements Listener{	
	private Map<Player, String> coords = new HashMap<>();
	private Map<Player, Boolean> isLoggedIn = new HashMap<>();
	private Map<Player, String> IPqueue = new HashMap<>();
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Player p = event.getPlayer();
    	String[] parts = p.getAddress().toString().split(":");
    	
    	if (IPqueue.containsValue(parts[0])) {
        	p.kickPlayer("Anti-flood: please wait a few seconds for the other account to login successfully.");
    	} else {
        	IPqueue.put(p, parts[0]);
        	isLoggedIn.put(p, false);
        	coords.put(p, p.getLocation().getX()+"-"+p.getLocation().getY()+"-"+p.getLocation().getZ());
        	p.setInvulnerable(true);
    	}
    }
    
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
    	Player p = event.getPlayer();
    	isLoggedIn.remove(p);
    	coords.remove(p);
    	IPqueue.remove(p);
    }
    
    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
    	String m = event.getMessage();
    	if (m.contains("admin") || m.contains("owner")) {
        	event.getPlayer().sendMessage("Contact the admin on Discord. You can find the invitation link at �aamongdemons.com");
    	} else if (m.contains("kit")) {
        	event.getPlayer().sendMessage("You can get �6kits �fwith �bsouls �fat �aamongdemons.com");
    	}
    }
    
    
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
    	Player p = (Player) event.getWhoClicked();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
    		loginMessage(p);
	        event.setCancelled(true);
    	} else  {
    		Chunk t = p.getTargetBlock(null, 100).getChunk();
        	String c = t.getX() + " " + t.getZ();
    		if (claims.isClaimed(c, p)) {
        		p.sendMessage("This chunk is claimed by someone.");
    	        event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
    	Player p = event.getPlayer();
    	String c = event.getBlock().getChunk().getX() + " " + event.getBlock().getChunk().getZ();
    	
    	if (claims.isClaimed(c, p)) {
    		p.sendMessage("This chunk is claimed by someone.");
	        event.setCancelled(true);
    	} else {
    		if (event.getBlock().getType().name().contentEquals("EMERALD_BLOCK")) {
        		claims.addClaim(c, claims.playerID.get(p)+",");
        		plugin.addClaimToDB(c, claims.playerID.get(p)+",");
    			p.sendMessage("You have claimed this chunk: �6"+c);
    		}
    	}
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
    	Player p = event.getPlayer();
		String c = event.getBlock().getChunk().getX() + " " + event.getBlock().getChunk().getZ();
		
    	if (claims.isClaimed(c, p)) {
    		p.sendMessage("This chunk is claimed by someone.");
	        event.setCancelled(true);
    	} else {
    		if (event.getBlock().getType().name().contentEquals("EMERALD_BLOCK")) {
        		claims.removeClaim(c);
    			p.sendMessage("You unclaimed this chunk.");
    		}
    	}
    }
    
    
    
    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}

		p.sendMessage(claims.debug());
    }
    
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
    		String s = event.getTo().getX()+"-"+event.getTo().getY()+"-"+event.getTo().getZ();
    		if (!s.equals(coords.get(p))) {
    			loginMessage(p);
    	        event.setCancelled(true);
    		}
    	}
    }
    
    public void doLogin(Player p) {
    	isLoggedIn.put(p, true);
    	IPqueue.remove(p);
    	p.setInvulnerable(false);
    }
    
    public boolean isLoggedIn(Player p) {
    	return isLoggedIn.get(p);
    }
    
    public void loginMessage(Player p) {
    	p.sendMessage("/login [pass] or register at �aamongdemons.com");
    }
}
