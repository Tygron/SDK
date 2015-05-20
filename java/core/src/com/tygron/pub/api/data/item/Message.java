package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

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

	private int id = StringUtils.NOTHING;

	private boolean active = false;
	private String contents = StringUtils.EMPTY;
	private String subject = StringUtils.EMPTY;

	private Answer[] answers = new Answer[0];

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

	public String getContents() {
		return contents;
	}

	public int getID() {
		return id;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isActive() {
		return active;
	}
}
