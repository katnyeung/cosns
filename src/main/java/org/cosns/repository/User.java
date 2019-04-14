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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.repository.extend.ProfileImage;
import org.cosns.util.Auditable;
import org.cosns.util.ConstantsUtil;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "user", indexes = { @Index(name = "user_email", columnList = "email", unique = true), @Index(name = "user_uniquename", columnList = "uniqueName", unique = true) })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User extends Auditable<String> {

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

	@Column(nullable = true)
	private String uniqueName;

	@Lob
	@Column(nullable = true)
	private String message;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Where(clause = "status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	private Set<ProfileImage> profileImage;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Post> posts;

	@ManyToMany
	@JoinTable(name = "followers", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "followerId"))
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "followerId")
	private List<User> followers;

	@ManyToMany
	@JoinTable(name = "followedBy", joinColumns = @JoinColumn(name = "followerId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "followedById")
	private List<User> followedBy;

	@OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL)
	Set<FriendRequest> friendRequest;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<PostReaction> postReaction;

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

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public List<User> getFollowers() {
		return followers;
	}

	public void setFollowers(List<User> followers) {
		this.followers = followers;
	}

	public List<User> getFollowedBy() {
		return followedBy;
	}

	public void setFollowedBy(List<User> followedBy) {
		this.followedBy = followedBy;
	}

	public Set<PostReaction> getPostReaction() {
		return postReaction;
	}

	public void setPostReaction(Set<PostReaction> postReaction) {
		this.postReaction = postReaction;
	}

	public Set<FriendRequest> getFriendRequest() {
		return friendRequest;
	}

	public void setFriendRequest(Set<FriendRequest> friendRequest) {
		this.friendRequest = friendRequest;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Set<ProfileImage> getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(Set<ProfileImage> profileImage) {
		this.profileImage = profileImage;
	}

}