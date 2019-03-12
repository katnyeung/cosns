package org.cosns.repository;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long imageId;
	private Long userId;

	private Long postId;

	private int seq;

	private String imageUrl;

	private double size;

	private String status;

	private Date createdate;

	private Date lastupdatedate;

}