package Goldsack.DiscoSheep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class DiscoSettings { 
	
	private DiscoSheep plugin;
	private Properties configFile;
	private File file = new File("plugins/DiscoSheep.properties");
	
	public boolean dropItems = true;
	public int sheepNumber = 15;
	public int maxSheepNumber = 25;
	public int defaultPartyTime = 10;
	public int maxPartyTime = 120;
	public int spawnDistance = 5;
	public int bigPartySheepNumber = 50;
	public int bigPartyMaxSheepNumber = 100;
	public int bigPartyDefaultPartyTime = 20;
	public int bigPartyMaxPartyTime = 900;
	public int bigPartySpawnDistance = 10;
	
	private String defaultConfigFile =
		"#Discosheep config file\n" +
		"\n" +
		"#Set true or false if you want players to be able to loot items from sheeps and blocks\n" +
		"#Be aware this is only for disco team the plugin spawns.\n" +
		"#It does not stop looting colored wool from natural spawned sheeps that has changed color with \"/ds color\"\n" +
		"dropItems=" + dropItems + "\n" +
		"\n" +
		"#Set how many sheeps a player gets on party command\n" +
		"sheepNumber=" + sheepNumber + "\n" +
		"#Set max number of sheeps a player can demand to be used in party.\n" +
		"#This is per player so if you have 20 people logged in it will spawn 20*n sheeps and could slow down servers with hundreds of sheeps\n" +
		"#If the musicbeats ingame slows down, it's too much for the server\n" +
		"maxSheepNumber=" + maxSheepNumber + "\n" +
		"#Sets time duration in seconds for how long a party will last on default\n" +
		"defaultPartyTime=" + defaultPartyTime + "\n" +
		"#Sets max time duration in seconds for how long a party can last\n" +
		"maxPartyTime=" + maxPartyTime + "\n" +
		"#Sets how big the spawn distance around the player is in blocks. Sheeps spawns in a square\n" +
		"spawnDistance=" + spawnDistance + "\n" +
		"\n" +
		"\n" +
		"#Set how many sheeps get spawned on bigparty command.\n" +
		"bigPartySheepNumber=" + bigPartySheepNumber + "\n" +
		"#Set max number of sheeps a player can demand to be used in a bigparty.\n" +
		"#A bigparty is always only in one place so max number is max.\n" +
		"bigPartyMaxSheepNumber=" + bigPartyMaxSheepNumber + "\n" +
		"#Sets time duration in seconds for how long a bigparty will last on default\n" +
		"defaultPartyTime=" + bigPartyDefaultPartyTime + "\n" +
		"#Sets max time duration in seconds for how long a bigparty can last\n" +
		"bigPartyMaxPartyTime=" + bigPartyMaxPartyTime + "\n" +
		"#Sets how big the spawn distance around the player is in blocks. Sheeps spawns in a square\n" +
		"bigPartySpawnDistance=" + bigPartySpawnDistance + "\n" +
		"\n" +
		"" ;
	
	public DiscoSettings(DiscoSheep discoSheep) {
		plugin = discoSheep;
		configFile = new Properties();
		if(checkSettingsFile()){
			readSettingsFile();
		}
		else{
			createSettingsFile();
		}
		
		settingsValidation();
		configFile.clear();
	}
	/**
	 * Checks if some settings contradict each others 
	 */
	private void settingsValidation() {
		if(defaultPartyTime > maxPartyTime){
			System.out.println(
					"[DiscoSheep] defaultPartyTime is bigger then maxPartyTime in DiscoSheep.properties. " +
					"defaultPartyTime is set to maxPartyTime");
			defaultPartyTime = maxPartyTime;
			
		}
		if(bigPartyDefaultPartyTime > bigPartyMaxPartyTime){
			System.out.println(
					"[DiscoSheep] bigPartyDefaultPartyTime is bigger then bigPartyMaxPartyTime in DiscoSheep.properties. " +
					"bigPartyDefaultPartyTime is set to bigPartyMaxPartyTime");
			bigPartyDefaultPartyTime = bigPartyMaxPartyTime;
		}
		if(sheepNumber > maxSheepNumber){
			System.out.println(
					"[DiscoSheep] sheepNumber is bigger then maxSheepNumber in DiscoSheep.properties. " +
					"sheepNumber is set to maxPartyTime");
			sheepNumber = maxSheepNumber;
			
		}
		if(bigPartySheepNumber > bigPartyMaxSheepNumber){
			System.out.println(
					"[DiscoSheep] bigPartySheepNumber is bigger then bigPartyMaxSheepNumber in DiscoSheep.properties. " +
					"bigPartySheepNumber is set to bigPartyMaxSheepNumber");
			bigPartySheepNumber = bigPartyMaxSheepNumber;
		}
		
	}
	private void createSettingsFile() {
		
		try {
			
			FileWriter fWrit = new FileWriter(file);
			fWrit.write(defaultConfigFile);
			fWrit.close();
			System.out.println("[DiscoSheep] Created a new DiscoSheep.properties file.");
			
		} catch (IOException e) {
			System.out.println("[Discosheep] Failed to create DiscoSheep.properties file. Has plugin folder been moved or don't have permission to write?");
		}
		
		
	}
	
	/**
	 * Read in settings from configFile. 
	 */
	private void readSettingsFile() {
		FileReader fRead = null;
		try {
			fRead = new FileReader(file);
			configFile.load(fRead);
			if(configFile.containsKey("dropItems")){
				String temp = configFile.getProperty("dropItems",dropItems+"");
				if(temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("1") || temp.equalsIgnoreCase("on")){
					dropItems = true;
				}
				else if(temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("0") || temp.equalsIgnoreCase("off")){
					dropItems = false;
				}
				else{
					notBoolean("dropItems");
				}
			}
			if(configFile.containsKey("sheepNumber")){
				try {
					sheepNumber = Integer.parseInt(configFile.getProperty("sheepNumber",sheepNumber+""));
				} catch (NumberFormatException e) {
					notInt("sheepNumber");
				}				
			}
			
			if(configFile.containsKey("maxSheepNumber")){
				try {
					maxSheepNumber = Integer.parseInt(configFile.getProperty("maxSheepNumber",maxSheepNumber+""));
				} catch (NumberFormatException e) {
					notInt("maxSheepNumber");
				}				
			}
			
			if(configFile.containsKey("defaultPartyTime")){
				try {
					defaultPartyTime = Integer.parseInt(configFile.getProperty("defaultPartyTime",defaultPartyTime+""));
				} catch (NumberFormatException e) {
					notInt("defaultPartyTime");
				}				
			}
			
			if(configFile.containsKey("maxPartyTime")){
				try {
					maxPartyTime = Integer.parseInt(configFile.getProperty("maxPartyTime",maxPartyTime+""));
				} catch (NumberFormatException e) {
					notInt("maxPartyTime");
				}				
			}
			
			if(configFile.containsKey("spawnDistance")){
				try {
					spawnDistance = Integer.parseInt(configFile.getProperty("spawnDistance",spawnDistance+""));
				} catch (NumberFormatException e) {
					notInt("spawnDistance");
				}				
			}
			
			//BigParty configs
			
			if(configFile.containsKey("bigPartySheepNumber")){
				try {
					bigPartySheepNumber = Integer.parseInt(configFile.getProperty("bigPartySheepNumber",bigPartySheepNumber+""));
				} catch (NumberFormatException e) {
					notInt("bigPartySheepNumber");
				}				
			}
			
			if(configFile.containsKey("bigPartyMaxSheepNumber")){
				try {
					bigPartyMaxSheepNumber = Integer.parseInt(configFile.getProperty("bigPartyMaxSheepNumber",bigPartyMaxSheepNumber+""));
				} catch (NumberFormatException e) {
					notInt("bigPartyMaxSheepNumber");
				}				
			}
			
			if(configFile.containsKey("bigPartyDefaultPartyTime")){
				try {
					bigPartyDefaultPartyTime = Integer.parseInt(configFile.getProperty("bigPartyDefaultPartyTime",bigPartyDefaultPartyTime+""));
				} catch (NumberFormatException e) {
					notInt("bigPartyDefaultPartyTime");
				}				
			}
			
			if(configFile.containsKey("bigPartyMaxPartyTime")){
				try {
					bigPartyMaxPartyTime = Integer.parseInt(configFile.getProperty("bigPartyMaxPartyTime",bigPartyMaxPartyTime+""));
				} catch (NumberFormatException e) {
					notInt("bigPartyMaxPartyTime");
				}				
			}
			
			if(configFile.containsKey("bigPartySpawnDistance")){
				try {
					bigPartySpawnDistance = Integer.parseInt(configFile.getProperty("bigPartySpawnDistance",bigPartySpawnDistance+""));
				} catch (NumberFormatException e) {
					notInt("bigPartySpawnDistance");
				}				
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("[Discosheep] DiscoSheep.properties file not found. Will use default settings");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("[Discosheep] IOException on reading settings file. Will use default settings");
			e.printStackTrace();
		}
		try {
			if(fRead != null){
				fRead.close();				
			}
		} catch (IOException e) {
			System.out.println("[Discosheep] IOException on closing settings file. File can not be moved while program is on");

		}
		
	}

	private void notInt(String key){
		System.out.println("[DiscoSheep] Failed to convert " + key +" value into a number from file DiscoSheep.properties");
	}
	private void notBoolean(String key){
		System.out.println("[DiscoSheep] Failed to convert " + key +" value into a boolean from file DiscoSheep.properties. Use on/off, true/false or 1/0 as value");
	}
	
	private boolean checkSettingsFile() {

		if(file.exists()){
			if(file.canRead()){
				return true;
			}else{
				System.out.println("[DiscoSheep] Is not allowed to read DiscoSheep.properties file.");
				return false;
			}
		}
		System.out.println("[DiscoSheep] Did not find DiscoSheep.properties file.");
		return false;
	}
	public void reload() {
		if(checkSettingsFile()){
			readSettingsFile();
		}
		else{
			createSettingsFile();
		}
		
		settingsValidation();
		configFile.clear();
	}
}
