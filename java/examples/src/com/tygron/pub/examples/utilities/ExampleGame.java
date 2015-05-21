package com.tygron.pub.examples.utilities;

/**
 * This class provides game details for the included examples
 * @author Rudolf
 *
 */

public class ExampleGame {

	public static class ExampleBuilding {
		public final int FUNCTION;
		public final int FLOORS;

		private ExampleBuilding(int function, int floors) {
			FUNCTION = function;
			FLOORS = floors;
		}
	}

	public static final String GAME = "delftshowcase17";
	public static final String LANGUAGE = "EN";

	public static final int STAKEHOLDER_MUNICIPALITY = 0;
	public static final int STAKEHOLDER_SSH = 2;

	public static final ExampleBuilding BUILDING1 = new ExampleBuilding(663, 4);

	public static final String[] STAKEHOLDER_OWNED_LOCATIONS = new String[] { "MULTIPOLYGON (((682.175 1372.371, 690.298 1390.647, 735.988 1370.34, 727.865 1352.063, 682.175 1372.371)))" };

}
