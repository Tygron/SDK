package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message extends Item {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class MessageAnswer extends Item {

		private String contents = StringUtils.EMPTY;

		public String getContents() {
			return contents;
		}
	}

	private boolean active = false;
	private String contents = StringUtils.EMPTY;
	private String subject = StringUtils.EMPTY;

	private MessageAnswer[] answers = new MessageAnswer[0];

	public int getAnswerID(String answer) {
		for (MessageAnswer answerObject : answers) {
			if (answerObject.getContents().toLowerCase().equals(answer)) {
				return answerObject.getID();
			}
		}
		return StringUtils.NOTHING;
	}

	public MessageAnswer[] getAnswers() {
		return answers;
	}

	public String getContents() {
		return contents;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isActive() {
		return active;
	}
}
