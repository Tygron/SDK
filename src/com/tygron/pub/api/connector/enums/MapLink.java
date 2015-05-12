package com.tygron.pub.api.connector.enums;

import java.util.Arrays;

public enum MapLink {
		ACHIEVEMENTS,
		ACTION_MENUS,
		ASSISTANT_ACTOR_DATA,
		LOANS,
		BEHAVIOR_TERRAINS,
		BUILDINGS,
		CHAIN_ELEMENTS,
		CINEMATIC_DATAS,
		CLIENT_EVENTS,
		CLIENT_WORDS,
		CONTRIBUTORS,
		COSTS,
		DIKES,
		ECONOMIES,
		EVENT_BUNDLES,
		FUNCTIONS,
		FUNCTION_OVERRIDES,
		GAME_LEVELS,
		HEIGHTS,
		HOTSPOTS,
		INCOMES,
		INDICATORS,
		KEY_BINDINGS,
		LANDS,
		MEASURES,
		MESSAGES,
		MODEL_DATAS,
		MODEL_SETS,
		MONEY_TRANSFERS,
		OVERLAYS,
		PARTICLE_EMITTERS,
		PIPE_CLUSTERS,
		PIPES,
		PIPE_DEFINITIONS,
		PIPE_JUNCTIONS,
		PIPE_LOADS,
		PIPE_SETTINGS,
		POPUPS,
		PRODUCTS,
		PRODUCT_STORAGES,
		QUALITATIVE_FUNCTION_SCORES,
		SCORES,
		SERVER_WORDS,
		LOGS,
		SETTINGS,
		SHRINK_SETTINGS,
		SIMTIME_SETTINGS,
		SOUNDS,
		SPECIAL_EFFECTS,
		SPECIAL_OPTIONS,
		STAKEHOLDERS,
		STRATEGIES,
		TAX_PLANS,
		TIMES,
		UNITS,
		UNIT_DATAS,
		UNIT_DATA_OVERRIDES,
		UPGRADE_TYPES,
		VACANCY_SETTINGS,
		VIDEOS,
		WATER_TERRAINS,
		WAY_POINTS,
		WEATHERS,
		ZONES,
		ZOOMLEVELS;

	private static final MapLink[] VALUES = MapLink.values();
	private static final String[] stringValues;

	static {
		stringValues = new String[MapLink.values().length];
		for (int i = 0; i < MapLink.values().length; i++) {
			stringValues[i] = VALUES[i].toString();
		}
	}

	public static String[] stringValues() {
		return Arrays.copyOf(stringValues, stringValues.length);
	}
}
