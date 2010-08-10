package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.IssueLinkType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueLinkTypeJsonParser {
	private static final String KEY_DIRECTION = "direction";

	public IssueLinkType parseIssueLinkType(JSONObject json) throws JSONException {
		final String name = json.getString("name");
		final String description = json.getString("description");
		final String dirStr = json.getString(KEY_DIRECTION);
		final IssueLinkType.Direction direction;
		if ("OUTBOUND".equals(dirStr)) {
			direction = IssueLinkType.Direction.OUTBOUND;
		} else if ("INBOUND".equals(dirStr)) {
			direction = IssueLinkType.Direction.INBOUND;
		} else {
			throw new JSONException("Invalid value of " + KEY_DIRECTION + " key: [" + dirStr + "]");
		}
		return new IssueLinkType(name, description, direction);
	}
}

