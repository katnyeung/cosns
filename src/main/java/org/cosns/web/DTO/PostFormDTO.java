package org.cosns.web.DTO;

import java.util.Date;
import java.util.List;

public class PostFormDTO {
	Long postId;
	String postMessage;
	Date releaseDate;

	List<String> fileList;

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getPostMessage() {
		return postMessage;
	}

	public void setPostMessage(String postMessage) {
		this.postMessage = postMessage;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Override
	public String toString() {
		return "PostFormDTO [postId=" + postId + ", postMessage=" + postMessage + ", releaseDate=" + releaseDate + ", fileList=" + fileList + "]";
	}

}
