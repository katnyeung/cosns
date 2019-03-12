package org.cosns.repository;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long userId;
	private String email;
	private String password;
	private int status;

	private String displayname;
	
	private Date createdate;
	private Date lastmodifydate;

}