package org.cosns.repository.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "anime")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AnimeEvent extends Event {

}