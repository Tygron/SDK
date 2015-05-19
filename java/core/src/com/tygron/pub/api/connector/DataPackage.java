package com.tygron.pub.api.connector;

import com.tygron.pub.utils.StringUtils;

public class DataPackage {
	private final String content;
	private final int requestTime;
	private final int statusCode;

	public DataPackage(final String content, final long requestTime, final int statusCode) {
		this.content = content;
		this.requestTime = (int) requestTime;
		this.statusCode = statusCode;
	}

	public String getContent() {
		return this.content;
	}

	public int getRequestTime() {
		return this.requestTime;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public boolean isContentFalse() {
		return (this.getContent().equals(StringUtils.FALSE));
	}

	public boolean isContentNull() {
		return (this.getContent().equals(StringUtils.NULL));
	}

	public boolean isContentTrue() {
		return (this.getContent().equals(StringUtils.TRUE));
	}
}
