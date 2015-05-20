package com.tygron.pub.api.data.item;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Popup {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Answer {
		private int id = StringUtils.NOTHING;

		private String contents = StringUtils.EMPTY;

		public String getContents() {
			return contents;
		}

		public int getID() {
			return id;
		}
	}

	public static enum PopupType {
			INTERACTION,
			ACTOR_STANDARD,
			INTERACTION_WITH_DATE;
	}

	private int id = StringUtils.NOTHING;
	private String title = StringUtils.EMPTY;
	private String text = StringUtils.EMPTY;

	private String type = StringUtils.EMPTY;
	private boolean ping = false;

	private String linkType = StringUtils.EMPTY;
	private int linkID = StringUtils.NOTHING;

	private Answer[] answers = new Answer[0];
	private int[] visibleForActorIDs = new int[0];

	public int[] getActorIDs() {
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

	public int getID() {
		return id;
	}

	public int getLinkID() {
		return linkID;
	}

	public String getLinkyType() {
		return linkType;
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
