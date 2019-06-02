package org.cosns.repository.hashtag;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.cosns.util.Auditable;
import org.cosns.util.ConstantsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "hash_tag", indexes = { @Index(name = "INDEX_HASHTAG", columnList = "hashTag") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public abstract class HashTag extends Auditable<String> {
	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long hashId;

	@NotNull
	private String hashTag;

	@JsonIgnore
	private int totalCount = 0;

	public Long getHashId() {
		return hashId;
	}

	public void setHashId(Long hashId) {
		this.hashId = hashId;
	}

	public String getHashTag() {
		return hashTag;
	}

	public void setHashTag(String hashTag) {
		this.hashTag = hashTag;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public String getRedisKey() {
		return ConstantsUtil.REDIS_TAG_GROUP + ":" + this.getHashTag();
	}
	
	public abstract String getRedisValue();
}