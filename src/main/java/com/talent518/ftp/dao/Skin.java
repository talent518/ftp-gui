package com.talent518.ftp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Skin {
	private static final Map<String, String> skins = new HashMap<String, String>();
	private static final List<String> keys = new ArrayList<String>();
	static {
		keys.add("skin.autumn");
		keys.add("skin.businessBlackSteel");
		keys.add("skin.businessBlueSteel");
		keys.add("skin.business");
		keys.add("skin.cerulean");
		keys.add("skin.cremeCoffee");
		keys.add("skin.creme");
		keys.add("skin.dustCoffee");
		keys.add("skin.dust");
		keys.add("skin.gemini");
		keys.add("skin.graphiteAqua");
		keys.add("skin.graphiteChalk");
		keys.add("skin.graphiteElectric");
		keys.add("skin.graphiteGlass");
		keys.add("skin.graphiteGold");
		keys.add("skin.graphite");
		keys.add("skin.graphiteSunset");
		keys.add("skin.magellan");
		keys.add("skin.mariner");
		keys.add("skin.mistAqua");
		keys.add("skin.mistSilver");
		keys.add("skin.moderate");
		keys.add("skin.nebulaAmethyst");
		keys.add("skin.nebulaBrickWall");
		keys.add("skin.nebula");
		keys.add("skin.nightShade");
		keys.add("skin.raven");
		keys.add("skin.sahara");
		keys.add("skin.twilight");

		skins.put("skin.autumn", "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
		skins.put("skin.businessBlackSteel", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
		skins.put("skin.businessBlueSteel", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
		skins.put("skin.business", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
		skins.put("skin.cerulean", "org.pushingpixels.substance.api.skin.SubstanceCeruleanLookAndFeel");
		skins.put("skin.cremeCoffee", "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
		skins.put("skin.creme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
		skins.put("skin.dustCoffee", "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
		skins.put("skin.dust", "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
		skins.put("skin.gemini", "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
		skins.put("skin.graphiteAqua", "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel");
		skins.put("skin.graphiteChalk", "org.pushingpixels.substance.api.skin.SubstanceGraphiteChalkLookAndFeel");
		skins.put("skin.graphiteElectric", "org.pushingpixels.substance.api.skin.SubstanceGraphiteElectricLookAndFeel");
		skins.put("skin.graphiteGlass", "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel");
		skins.put("skin.graphiteGold", "org.pushingpixels.substance.api.skin.SubstanceGraphiteGoldLookAndFeel");
		skins.put("skin.graphite", "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
		skins.put("skin.graphiteSunset", "org.pushingpixels.substance.api.skin.SubstanceGraphiteSunsetLookAndFeel");
		skins.put("skin.magellan", "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel");
		skins.put("skin.mariner", "org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
		skins.put("skin.mistAqua", "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
		skins.put("skin.mistSilver", "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
		skins.put("skin.moderate", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
		skins.put("skin.nebulaAmethyst", "org.pushingpixels.substance.api.skin.SubstanceNebulaAmethystLookAndFeel");
		skins.put("skin.nebulaBrickWall", "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
		skins.put("skin.nebula", "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
		skins.put("skin.nightShade", "org.pushingpixels.substance.api.skin.SubstanceNightShadeLookAndFeel");
		skins.put("skin.raven", "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel");
		skins.put("skin.sahara", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
		skins.put("skin.twilight", "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel");
	}

	public static List<String> keys() {
		return keys;
	}

	public static boolean contains(String key) {
		return skins.containsKey(key);
	}

	public static String get(String key) {
		return skins.get(key);
	}
}
