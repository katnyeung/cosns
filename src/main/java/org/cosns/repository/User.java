package org.cosns.repository;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.cosns.util.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "user", indexes = { @Index(name = "user_email", columnList = "email", unique = true),
		@Index(name = "user_uniquename", columnList = "uniqueName", unique = true) })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User extends Auditable<String> {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@NotNull
	private String email;

	@JsonIgnore
	@NotNull
	@Size(max = 100)
	private String password;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	private String status;

	@Null
	@Column(unique = true)
	private String uniqueName;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Post> posts;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Image> images;

	@ManyToMany
	@JoinTable(name = "friends", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "friendId"))
	private List<User> friends;

	@ManyToMany
	@JoinTable(name = "friends", joinColumns = @JoinColumn(name = "friendId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	private List<User> friendOf;
	
	@OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL)
	Set<FriendRequest> friendRequest;
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Set<Post> getPosts() {
		return posts;
	}

	public void setPosts(Set<Post> posts) {
		this.posts = posts;
	}

	public Set<Image> getImages() {
		return images;
	}

	public void setImages(Set<Image> images) {
		this.images = images;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public List<User> getFriends() {
		return friends;
	}

	public void setFriends(List<User> friends) {
		this.friends = friends;
	}

	public List<User> getFriendOf() {
		return friendOf;
	}

	public void setFriendOf(List<User> friendOf) {
		this.friendOf = friendOf;
	}

	public Set<FriendRequest> getFriendRequest() {
		return friendRequest;
	}

	public void setFriendRequest(Set<FriendRequest> friendRequest) {
		this.friendRequest = friendRequest;
	}

}