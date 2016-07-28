package com.jazeee.ddp.auth;

/**
 * For logging in w/ resume token
 */
public class TokenAuth {
	private final String resume;

	public TokenAuth(String resumeToken) {
		assert (resumeToken != null);
		this.resume = resumeToken;
	}

	public String getResumeToken() {
		return resume;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TokenAuth [resume=");
		builder.append(resume);
		builder.append("]");
		return builder.toString();
	}
}
