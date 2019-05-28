package org.cosns.repository.extend.post;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.Post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "photo")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PhotoPost extends Post {

	@Column(nullable = true)
	private Date photoDate;

}