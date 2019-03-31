package org.cosns.repository.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cosns.repository.PostReaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue(value = "like")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LikeReaction extends PostReaction {

}