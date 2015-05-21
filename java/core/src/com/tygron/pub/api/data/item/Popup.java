package com.tygron.pub.api.data.item;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Popup extends Item {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Answer extends Item {

		private String contents = StringUtils.EMPTY;

		public String getContents() {
			return contents;
		}
	}

	public static enum PopupType {
			INTERACTION,
			ACTOR_STANDARD,
			INTERACTION_WITH_DATE;
	}

	private String title = StringUtils.EMPTY;
	private String text = StringUtils.EMPTY;

	// INTERACTION
	// INTERACTION_WITH_DATE
	// ACTOR_STANDARD
	private String type = StringUtils.EMPTY;
	private boolean ping = false;
	private String point = StringUtils.EMPTY;

	private String linkType = StringUtils.EMPTY;
	private int linkID = StringUtils.NOTHING;

	private Answer[] answers = new Answer[0];
	private Integer[] visibleForActorIDs = new Integer[0];

	public Integer[] getActorIDs() {
		return visibleForActorIDs;
	}

	public int getAnswerID(String answer) {
		for (Answer answerObject : answers) {
			if (answerObject.getContents().toLowerCase().equals(answer)) {
				return answerObject.getID();
			}
		}
		return StringUtils.NOTHING;
	}

	public Answer[] getAnswers() {
		return answers;
	}

	public int getLinkID() {
		return linkID;
	}

	public String getLinkyType() {
		return linkType;
	}

	public Double[] getPoint() {
		String strippedPoint = getPointString().replace("POINT (", "");
		strippedPoint = strippedPoint.replace(")", "");
		String[] splitPoint = strippedPoint.split(" ");
		try {
			return new Double[] { Double.parseDouble(splitPoint[0]), Double.parseDouble(splitPoint[1]) };
		} catch (Exception e) {
			return new Double[] { (double) StringUtils.NOTHING, (double) StringUtils.NOTHING };
		}
	}

	public String getPointString() {
		return point;
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public boolean isPing() {
		return ping;
	}

	public boolean isVisibleToStakeholder(int stakeholderID) {
		return Arrays.asList(visibleForActorIDs).contains(stakeholderID);
	}

}
