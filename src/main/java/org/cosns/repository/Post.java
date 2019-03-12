package org.cosns.repository;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long postId;
	
	private Long userId;
	
	private String title;
	private String contentHtml;
	private int like;
	private String status;
	
	private Date createdate;
	private Date lastupdatedate;
	
	
}