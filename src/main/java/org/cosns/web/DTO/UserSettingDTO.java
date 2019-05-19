package org.cosns.web.DTO;

import java.util.List;
import java.util.Map;

public class UserSettingDTO {
	String password;

	String image;

	String uniqueName;

	String displayName;

	List<Map<String, String>> keyHashTag;

	String message;

	String likeCoinId;

	String fbId;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<Map<String, String>> getKeyHashTag() {
		return keyHashTag;
	}

	public void setKeyHashTag(List<Map<String, String>> keyHashTag) {
		this.keyHashTag = keyHashTag;
	}

	public String getLikeCoinId() {
		return likeCoinId;
	}

	public void setLikeCoinId(String likeCoinId) {
		this.likeCoinId = likeCoinId;
	}

	public String getFbId() {
		return fbId;
	}

	public void setFbId(String fbId) {
		this.fbId = fbId;
	}

}
