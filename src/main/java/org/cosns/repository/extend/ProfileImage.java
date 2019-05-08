package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.Image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "profile")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProfileImage extends Image {

}