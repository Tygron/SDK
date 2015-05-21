package com.tygron.pub.api.enums;

import java.util.Arrays;
import com.tygron.pub.utils.StringUtils;

public enum MapLink {
		ACHIEVEMENTS,
		ACTION_MENUS,
		ASSISTANT_ACTOR_DATA,
		BEHAVIOR_TERRAINS,
		BUILDINGS,
		CHAIN_ELEMENTS,
		CHAT_MESSAGES(
			true),
		CINEMATIC_DATAS,
		CLIENT_EVENTS,
		CLIENT_WORDS,
		CONTRIBUTORS,
		COSTS,
		DEFAULT_WORDS(
			true),
		DIKES,
		ECONOMIES,
		EDITING_SESSION(
			true),
		EVENT_BUNDLES,
		FUNCTIONS,
		FUNCTION_OVERRIDES,
		GAME_LEVELS,
		GEO_LINKS(
			true),
		HEIGHTS,
		HOTSPOTS,
		INCOMES,
		INDICATORS,
		KEY_BINDINGS,
		LANDS,
		LOANS,
		LOGS,
		MEASURES,
		MESSAGES,
		MODEL_DATAS,
		MODEL_SETS,
		MONEY_TRANSFERS,
		OVERLAYS,
		PARTICLE_EMITTERS,
		PIPES,
		PIPE_CLUSTERS,
		PIPE_DEFINITIONS,
		PIPE_JUNCTIONS,
		PIPE_LOADS,
		PIPE_SETTINGS,
		POPUPS,
		PRODUCTS,
		PRODUCT_STORAGES,
		PROGRESS(
			true),
		QUALITATIVE_FUNCTION_SCORES,
		SCORES,
		SERVER_WORDS,
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

	boolean editorData = false;

	private MapLink() {
	}

	private MapLink(boolean editorData) {
		this.editorData = editorData;
	}

	public boolean isEditorEvent() {
		return editorData;
	}

	public String itemUrl(int itemNumber) {
		return this.url() + itemNumber;
	}

	public String url() {
		return this.name().replace("_", "").toLowerCase() + StringUtils.URL_DELIMITER;
	}
}
