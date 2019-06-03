package org.cosns.repository.post;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "photo")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PhotoPost extends Post {


}