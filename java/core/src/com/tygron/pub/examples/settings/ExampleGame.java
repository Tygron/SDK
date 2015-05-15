package com.tygron.pub.examples.settings;

/**
 * This class provides game details for the included examples
 * @author Rudolf
 *
 */

public class ExampleGame {

	public class ExampleBuilding1 {
		public final int FUNCTION = 663;
		public final int FLOORS = 4;

		private ExampleBuilding1() {
		}
	}

	public static final String GAME = "delftshowcase17";
	public static final String LANGUAGE = "EN";
	public static final int STAKEHOLDER = 2; // This is the stakeholder ID of the SSH

	public static final int STAKEHOLDER_MUNICIPALITY = 0; // This is the stakeholder ID of the Municipality

	public static final ExampleBuilding1 BUILDING1 = new ExampleGame().new ExampleBuilding1();

	public static final String[] STAKEHOLDER_OWNED_LOCATIONS = new String[] { "MULTIPOLYGON (((682.175 1372.371, 690.298 1390.647, 735.988 1370.34, 727.865 1352.063, 682.175 1372.371)))" };
}
