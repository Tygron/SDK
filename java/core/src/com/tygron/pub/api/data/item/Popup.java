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

	public static enum PopupAnswerSet {

			WAITING_FOR_DATE(
				true,
				"yes",
				"no"),
			REQUEST_CONSTRUCTION_APPROVAL(
				"yes",
				"no"),
			REQUEST_ZONING_APPROVAL(
				"yes",
				"no"),
			CONSTRUCTION_APPROVED(
				"confirm",
				"revert"),
			CONSTRUCTION_DENIED(
				"ok"),
			PENDING_CONSTRUCTION(
				"no",
				"yes"),
			WAITING_FOR_DATE_UPGRADE(
				true,
				// This is not directly linked
				"confirm",
				"revert"),
			UPGRADE_APPROVED(
				// This is not directly linked
				"yes",
				"no"),
			CONSTRUCTING;

		private final String[] answers;
		private boolean requireDate = false;

		private PopupAnswerSet(boolean requireDate, String... answers) {
			this.requireDate = true;
			this.answers = answers;
		}

		private PopupAnswerSet(String... answers) {
			this.answers = answers;
		}

		public String[] getAnswers() {
			return answers.clone();
		}

		public String getConservativeAnswer() {
			if (answers.length == 0) {
				return null;
			}
			return answers[answers.length - 1];
		}

		public String getProgressiveAnswer() {
			if (answers.length == 0) {
				return null;
			}
			return answers[0];
		}

		public boolean requiresDate() {
			return requireDate;
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

	public String getLinkType() {
		return linkType;
	}

	public Double[] getPoint() {
		String strippedPoint = getPointString().replace("POINT (", StringUtils.EMPTY);
		strippedPoint = strippedPoint.replace(")", StringUtils.EMPTY);
		String[] splitPoint = strippedPoint.split(StringUtils.SPACE);
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

	public boolean requiresDate() {
		return this.getType().equals("INTERACTION_WITH_DATE");
	}

}
