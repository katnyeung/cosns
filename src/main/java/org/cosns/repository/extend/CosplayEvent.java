package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.Event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "cosplay")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class CosplayEvent extends Event {

}