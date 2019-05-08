package org.cosns.repository;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.util.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "image", indexes = { @Index(name = "INDEX_IMAGEPATH", columnList = "filename") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "image_type")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public abstract class Image extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long imageId;

	private int seq;

	@JsonIgnore
	@Lob
	@NotNull
	private String storedPath;

	@NotNull
	@Size(max = 255)
	private String filename;

	@Column(nullable = true)
	@Size(max = 255)
	private String thumbnailFilename;

	@JsonIgnore
	private Long size;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	private String status;

	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(Long imageId) {
		this.imageId = imageId;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStoredPath() {
		return storedPath;
	}

	public void setStoredPath(String storedPath) {
		this.storedPath = storedPath;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}

	public void setThumbnailFilename(String thumbnailFilename) {
		this.thumbnailFilename = thumbnailFilename;
	}

}