package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.Event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "anime")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AnimeEvent extends Event {

}