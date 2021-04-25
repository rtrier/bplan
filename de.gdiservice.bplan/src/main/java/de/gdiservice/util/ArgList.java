package de.gdiservice.util;

import java.util.HashMap;
import java.util.Map;

public class ArgList {
	
	Map<String, String> argMap = new HashMap<>();
	
	public ArgList(String[] args) {
		if (args!=null) {
			for (int i=0; i<args.length; i++) {
				int idx = args[i].indexOf("=");
				if (idx>1) {
				  argMap.put(args[i].substring(0, idx), args[i].substring(idx+1));
				}
			}
		}
	}
	
	public String get(String argName) {
		return argMap.get(argName);
	}
	
}