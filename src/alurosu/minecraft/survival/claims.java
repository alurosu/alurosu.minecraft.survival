package alurosu.minecraft.survival;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class claims {
	public static Map<Player, Integer> playerID = new HashMap<Player, Integer>();
	static Map<String, String> chunk = new HashMap<String, String>();
	
	public static boolean isClaimed(String c, Player p) {
		String access = chunk.get(c);
		
		if (access == null)
			return false;

		int myID = playerID.get(p);
		
		boolean isMine = false;
		for (String s : access.split(",")) {
			if (Integer.parseInt(s) == myID)
				isMine = true;
		}
		return !isMine;
	}
	
	public static String addTrustedUser(Player p, String user, int id) {
    	String c = p.getLocation().getChunk().getX() + " " + p.getLocation().getChunk().getZ();
    	String access = chunk.get(c);
    	
    	if (access == null)
			return "You do not own this chunk.";
    	
    	int myID = playerID.get(p);
    	
    	boolean isMine = false;
		for (String s : access.split(",")) {
			if (Integer.parseInt(s) == myID)
				isMine = true;
		}
		
		if (!isMine)
			return "You do not own this chunk.";
    
		access += id+",";
		chunk.put(c, access);
		plugin.updateClaimInDB(c, access);
		
    	return "You trust §6"+user+"§f on chunk: §6"+c;
	}
	
	public static String removeTrustedUser(Player p, String user, int id) {
    	String c = p.getLocation().getChunk().getX() + " " + p.getLocation().getChunk().getZ();
    	String access = chunk.get(c);
    	String new_access = "";
    	
    	if (access == null)
			return "You do not own this chunk.";
    	
    	int myID = playerID.get(p);
    	
    	boolean isMine = false;
		for (String s : access.split(",")) {
			int temp = Integer.parseInt(s);
			if (temp == myID)
				isMine = true;
			if (temp != id)
				new_access += temp + ",";
		}
		
		if (!isMine)
			return "You do not own this chunk.";
		
		chunk.put(c, new_access);
		plugin.updateClaimInDB(c, new_access);
		
    	return "You trust §6"+user+"§f on chunk: §6"+c;
	}
	
	public static void removeClaim(String c) {
		chunk.remove(c);
		plugin.removeClaimFromDB(c);
	}

	public static void addClaim(String c, String access) {
		chunk.put(c, access);
	}
	
	public static String debug() {
		String m = "debug: ";
		
		for (String key : chunk.keySet()) {
	        m += key + "=" + chunk.get(key) + ", ";
	    }
		
		return m;
	}
}
