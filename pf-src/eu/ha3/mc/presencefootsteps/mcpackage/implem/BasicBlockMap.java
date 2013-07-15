package eu.ha3.mc.presencefootsteps.mcpackage.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import eu.ha3.mc.presencefootsteps.mcpackage.interfaces.BlockMap;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

public class BasicBlockMap implements BlockMap
{
	private Map<String, String> blockMap;
	
	public BasicBlockMap()
	{
		this.blockMap = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getBlockMap(int block, int meta)
	{
		String material = null;
		
		if (this.blockMap.containsKey(block + "_" + meta))
		{
			material = this.blockMap.get(block + "_" + meta);
		}
		else if (this.blockMap.containsKey(Integer.toString(block)))
		{
			material = this.blockMap.get(Integer.toString(block));
		}
		else
		{
			material = null;
		}
		
		return material;
	}
	
	@Override
	public String getBlockMapSubstrate(int block, int meta, String substrate)
	{
		String material = null;
		
		if (this.blockMap.containsKey(block + "_" + meta + "." + substrate))
		{
			material = this.blockMap.get(block + "_" + meta + "." + substrate);
		}
		else if (this.blockMap.containsKey(Integer.toString(block) + "." + substrate))
		{
			material = this.blockMap.get(Integer.toString(block) + "." + substrate);
		}
		else
			return null;
		
		return material;
	}
	
	@Override
	public void register(String key, String value)
	{
		this.blockMap.put(key, value);
	}
}