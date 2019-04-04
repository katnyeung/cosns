package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.Event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "photo")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PhotoEvent extends Event {

}