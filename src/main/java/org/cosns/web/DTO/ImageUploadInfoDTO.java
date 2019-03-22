package org.cosns.web.DTO;

import org.springframework.web.multipart.MultipartFile;

public class ImageUploadInfoDTO {
	Integer flowChunkNumber;
	Long flowChunkSize;
	Long flowCurrentChunkSize;
	Long flowTotalSize;
	String flowIdentifier;
	String flowFilename;
	String flowRelativePath;
	Integer flowTotalChunks;
	MultipartFile file;

	public Integer getFlowChunkNumber() {
		return flowChunkNumber;
	}

	public void setFlowChunkNumber(Integer flowChunkNumber) {
		this.flowChunkNumber = flowChunkNumber;
	}

	public Long getFlowChunkSize() {
		return flowChunkSize;
	}

	public void setFlowChunkSize(Long flowChunkSize) {
		this.flowChunkSize = flowChunkSize;
	}

	public Long getFlowCurrentChunkSize() {
		return flowCurrentChunkSize;
	}

	public void setFlowCurrentChunkSize(Long flowCurrentChunkSize) {
		this.flowCurrentChunkSize = flowCurrentChunkSize;
	}

	public Long getFlowTotalSize() {
		return flowTotalSize;
	}

	public void setFlowTotalSize(Long flowTotalSize) {
		this.flowTotalSize = flowTotalSize;
	}

	public String getFlowIdentifier() {
		return flowIdentifier;
	}

	public void setFlowIdentifier(String flowIdentifier) {
		this.flowIdentifier = flowIdentifier;
	}

	public String getFlowFilename() {
		return flowFilename;
	}

	public void setFlowFilename(String flowFilename) {
		this.flowFilename = flowFilename;
	}

	public String getFlowRelativePath() {
		return flowRelativePath;
	}

	public void setFlowRelativePath(String flowRelativePath) {
		this.flowRelativePath = flowRelativePath;
	}

	public Integer getFlowTotalChunks() {
		return flowTotalChunks;
	}

	public void setFlowTotalChunks(Integer flowTotalChunks) {
		this.flowTotalChunks = flowTotalChunks;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "ImageUploadInfoDTO [flowChunkNumber=" + flowChunkNumber + ", flowChunkSize=" + flowChunkSize
				+ ", flowCurrentChunkSize=" + flowCurrentChunkSize + ", flowTotalSize=" + flowTotalSize
				+ ", flowIdentifier=" + flowIdentifier + ", flowFilename=" + flowFilename + ", flowRelativePath="
				+ flowRelativePath + ", flowTotalChunks=" + flowTotalChunks + ", file=" + file + "]";
	}

}
