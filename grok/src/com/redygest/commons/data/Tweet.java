/**
 * 
 */
package com.redygest.commons.data;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Class representing a Tweet
 * 
 * @author semanticvoid
 * 
 */
public class Tweet extends AbstractData {

	private String text;

	/**
	 * Constructor
	 * @param json
	 */
	public Tweet(String json) {
		JSONObject jsonObj = JSONObject.fromObject(json);
		this.text = jsonObj.getString("text");
		if(!StringUtils.isBlank(this.text)) {
			data.put(DataType.BODY, this.text);
		}
	}
	
	@Override
	protected boolean isDataPopulated() {
		return true;
	}
}