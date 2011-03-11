package Goldsack.ColorSheep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/**
 * Supportclass to help control different settings in ColorSetting. Cast to BoolS, IntS or FloS to use method getV();
 * @author goldsack
 */
class Setting{
	//String settingName, settingRaw, comment
	String sN, sR, c;
	public Setting(String sN, String sR, String c) {
		this.sN = sN;
		this.sR = sR;
		this.c = c;
	}
	/** Get settingName	 */
	public String getsN() { return sN;}
	/** Get settingRaw	 */
	public String getsR() { return sR;}
	/** Get comment	 */
	public String getC() {  return c; }
	
}
/**
 * Supportclass to help control different settings in ColorSetting. Includes boolean value
 * @author goldsack
 */
class BoolS extends Setting{
	boolean v = false;
	public BoolS(String sN, String sR, String c) {
		super(sN, sR, c);
	}
	public void setV(boolean v) {this.v = v;};
	public boolean getV(){return v;}
}
/**
 * Supportclass to help control different settings in ColorSetting. Includes int value
 * @author goldsack
 */
class IntS extends Setting{
	int v = 1;
	public IntS(String sN, String sR, String c) {
		super(sN, sR, c);
	}
	public void setV(int v) {this.v = v;};
	public int getV(){return v;}
}
/**
 * Supportclass to help control different settings in ColorSetting. Includes float value
 * @author goldsack
 */
class FloS extends Setting{
	float v = 1;
	public FloS(String sN, String sR, String c) {
		super(sN, sR, c);
	}
	public void setV(float v) {this.v = v;};
	public float getV(){return v;}
}
/**
 * Supportclass to help control different settings in ColorSetting. Includes String value
 * @author goldsack
 */
class StrS extends Setting{
	String v = "NOT_SET";
	public StrS(String sN, String sR, String c) {
		super(sN, sR, c);
	}
	public void setV(String v) {this.v = v;};
	public String getV(){return v;}
}
/**
 * ColorSettings allows to read a config file and call settings by its name.
 * @author goldsack
 *
 */
public class ColorSettings { 
	
	private ColorSheep plugin;
	private Properties configFile;
	private File file = new File("plugins/ColorSheep.properties");
	public HashMap<String, Setting> hash = new HashMap<String, Setting>();

	Setting[] settings = {
		new IntS("itemID", 				"331", 	"#Set the item number that will be absorbed when calling /color. Etc default is redstone with ID 331. " +
												"#See http://www.minecraftwiki.net/wiki/Data_values and use the decimal code to pick your item\n"),
		new IntS("numberOfItems", 		"1", 	"#Set how many items that will be removed when called. If set to 0 the user can use the plugin as many time they want.\n"),
		new IntS("radius", 				"20", 	"#Sets the radius around the player where the sheeps will swap color. 20 means 20 blocks away in a sphere.\n")
	};
	
	
	public ColorSettings(ColorSheep discoSheep) {
		plugin = discoSheep;
		configFile = new Properties();
		
		initProperties();

		
		
	}
	private void initProperties() {
		//Set default variables and put setting in hashmap
		for (Setting s: settings) {
			setData(s, s.sR);
			hash.put(s.sN, s);
		}

		//If file exists, read file
		if(checkSettingsFile()){
			readSettingsFile();
		}
		else{
			//Else, create new file
			createSettingsFile();
		}
		
		settingsValidation();
		configFile.clear();
	}
	/**
	 * Checks if some settings contradict each others 
	 */
	private void settingsValidation() {
		
		try {
			
			setOverLimit((IntS) getSetting("numberOfItems"), 0);
			setOverLimit((IntS) getSetting("radius"), 0);
			
		} catch (NullPointerException e) {
			System.out.println("[ColorSheep] Nullpointer in settingsValidation. This is not your fault, but is plugin developers fault. Tell him you got this error");
		}

		
	}
	/**
	 * If normal is larger then max, then normal becomes max
	 * @param normal
	 * @param max
	 */
	private void setUnderLimit(IntS normal, IntS max){
		if(normal.getV() > max.getV()){
			System.out.println(
					"[ColorSheep] " + normal.getsN() + " is bigger then " + max.getsN() + " in ColorSheep.properties. " +
					normal.getsN() + " is set to " + max.getsN() );
			normal.setV(max.getV());
		}
	}
	/**
	 * If below the number given, set to min
	 * @param normal
	 */
	private void setOverLimit(IntS normal, int min){
		if(normal.getV() < min){
			System.out.println(
					"[ColorSheep] " + normal.getsN() + " is smaller then " + min + " in ColorSheep.properties. " +
					normal.getsN() + " is set to " + min );
			normal.setV(min);
		}
	}
	private void createSettingsFile() {
		
		try {
			StringBuilder writeString = new StringBuilder();
			
			//Add config title
			writeString.append("#ColorSheep config file\n\n");
			
			//Add comments, variableName and variableRaw
			for (Setting s: settings) {
				writeString.append("\n" + s.getC() + s.getsN() + "=" + s.getsR());
			}
			
			//Write file
			FileWriter fWrit = new FileWriter(file);
			fWrit.write(writeString.toString());
			fWrit.close();
			System.out.println("[ColorSheep] Created a new ColorSheep.properties file.");
			
		} catch (IOException e) {
			System.out.println("[Colorsheep] Failed to create ColorSheep.properties file. Has plugin folder been moved or don't have permission to write?");
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
			
			for(Setting s : settings){
				if(configFile.containsKey(s.sN)){
					setData(hash.get(s.sN), configFile.getProperty(s.sN));
				}
				else{
					System.out.println("ColorSheep.properties does not contain setting \"" + s.sN + "\". Will use built in default: " + s.sR);
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("[Colorsheep] ColorSheep.properties file not found. Will use default settings");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("[Colorsheep] IOException on reading settings file. Will use default settings");
			e.printStackTrace();
		}
		try {
			if(fRead != null){
				fRead.close();				
			}
		} catch (IOException e) {
			System.out.println("[Colorsheep] IOException on closing settings file. File can not be moved while program is on");

		}
		
	}

	private boolean checkSettingsFile() {

		if(file.exists()){
			if(file.canRead()){
				return true;
			}else{
				System.out.println("[ColorSheep] Is not allowed to read ColorSheep.properties file.");
				return false;
			}
		}
		System.out.println("[ColorSheep] Did not find ColorSheep.properties file.");
		return false;
	}
	public void reload() {
		initProperties();
	}
	/**
	 * Sets the correct data into BoolS, IntS and FloS
	 */
	public void setData(Setting setting, String settingRaw){
		
		if(setting == null){
			System.out.println("[ColorSheep] recived a Setting that was null in ColorSettings.setData(), this will make [ColorSheep] malfunction. " +
			"If you get this message, tell the plugindeveloper what you did so he can fix it.");
			return;
		}
		
		if(settingRaw == null){
			System.out.println("[ColorSheep] recived a String that was null in ColorSettings.setData(), this will make [ColorSheep] malfunction. " +
			"If you get this message, tell the plugindeveloper what you did so he can fix it.");
			return;
		}
		
		if(setting instanceof BoolS){	
			if(settingRaw.equalsIgnoreCase("true") || settingRaw.equalsIgnoreCase("1") || settingRaw.equalsIgnoreCase("on")){
				((BoolS) setting).setV(true);
			}
			else if(settingRaw.equalsIgnoreCase("false") || settingRaw.equalsIgnoreCase("0") || settingRaw.equalsIgnoreCase("off")){
				((BoolS) setting).setV(false);
			}
			else{
				System.out.println("[ColorSheep] Failed to convert " + settingRaw + " from " + setting.getsN() + " value into a boolean from file ColorSheep.properties. " +
						"Use on/off, true/false or 1/0 as value. " + setting.getsN() + " has been set to default value " + setting.getsR().equalsIgnoreCase("true") +".");
				((BoolS) setting).setV(setting.getsR().equalsIgnoreCase("true"));
			}
		}
		else if(setting instanceof IntS){
			try {
				((IntS)setting).setV(Integer.parseInt(settingRaw));
			} catch (NumberFormatException e) {
				System.out.println("[ColorSheep] Failed to convert " + settingRaw + " from " + setting.getsN() + " value into a whole number from file ColorSheep.properties. " +
						"Will use default value " + setting.getsR());
				
			}	
		}
		else if(setting instanceof FloS){
			try {
				((FloS)setting).setV(Float.parseFloat(settingRaw));
			} catch (NumberFormatException e) {
				System.out.println("[ColorSheep] Failed to convert " + settingRaw + " from " + setting.getsN() + " value into a decimal number from file ColorSheep.properties. " +
						"Will use default value " + setting.getsR());
				
			}	
		}
		else if(setting instanceof StrS){
			((StrS)setting).setV(settingRaw);
		}
		else{
			System.out.println("[ColorSheep] error. Please tell the plugindeveloper that he has forgot to include support for " + setting.getClass().getName());
		}		
	}
	
	public Setting getSetting(String settingName){
		Setting result = hash.get(settingName);
		if(result == null){
			System.out.println("[ColorSheep] error! getSetting in ColorSheep can't find setting \"" + settingName + "\n");
		}
		return result;
	}
	
}
