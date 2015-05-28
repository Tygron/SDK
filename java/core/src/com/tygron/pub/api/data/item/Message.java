package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message extends Item {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MessageAnswer extends Item {

		private String contents = StringUtils.EMPTY;

		private CodedEvent[] events = new CodedEvent[0];

		public String getContents() {
			return contents;
		}

		public CodedEvent[] getEvents() {
			return events;
		}
	}

	public static enum MessageAnswers {

		UPGRADE_APPROVAL(
			"yes",
			"no");

		private final String[] answers;

		private MessageAnswers(String... answers) {
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
	}

	private boolean active = false;
	private String contents = StringUtils.EMPTY;
	private String subject = StringUtils.EMPTY;

	private int actorIDsender = StringUtils.NOTHING;
	private int actorIDreceiver = StringUtils.NOTHING;

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

	public int getReceiverID() {
		return actorIDreceiver;
	}

	public int getSenderID() {
		return actorIDsender;
	}

	public String getSubject() {
		return subject;
	}

	public boolean isActive() {
		return active;
	}
}
