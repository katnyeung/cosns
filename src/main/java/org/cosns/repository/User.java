package org.cosns.repository;

import java.util.Date;
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

import org.cosns.repository.event.Event;
import org.cosns.repository.hashtag.UserHashTag;
import org.cosns.repository.image.Image;
import org.cosns.repository.image.ProfileImage;
import org.cosns.repository.post.Post;
import org.cosns.repository.postreaction.PostReaction;
import org.cosns.util.Auditable;
import org.cosns.util.ConstantsUtil;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
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

	@Column(nullable = true)
	private String displayName;

	@Lob
	@Column(nullable = true)
	private String message;

	@JsonIgnore
	@NotNull
	@Size(max = 10)
	private String userRole;

	@Column(nullable = true)
	@Size(max = 255)
	private String likeCoinId;

	@Column(nullable = true)
	@Size(max = 100)
	private String fbId;
	
	@OneToMany(mappedBy = "profileUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Where(clause = "status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	private Set<ProfileImage> profileImages;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Where(clause = "status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	private Set<Image> images;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Post> posts;

	@ManyToMany
	@JoinTable(name = "followers", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "followerId"))
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
	@JsonIdentityReference(alwaysAsId = true)
	private List<User> followers;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "followedBy", joinColumns = @JoinColumn(name = "followerId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
	private List<User> followedBy;

	@JsonIgnore
	@OneToMany(mappedBy = "fromUser", cascade = CascadeType.ALL)
	Set<FriendRequest> friendRequest;

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<PostReaction> postReaction;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<UserHashTag> hashtags;

	@JsonIgnore
	private Date lastUpdateUniqueNameDate;

	@JsonIgnore
	@OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
	private List<Event> createdEvent;

	@JsonIgnore
	@OneToMany(mappedBy = "approvedBy", cascade = CascadeType.ALL)
	private List<Event> approvedEvent;

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

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
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

	public Set<ProfileImage> getProfileImages() {
		return profileImages;
	}

	public void setProfileImages(Set<ProfileImage> profileImages) {
		this.profileImages = profileImages;
	}

	public Set<Image> getImages() {
		return images;
	}

	public void setImages(Set<Image> images) {
		this.images = images;
	}

	public Date getLastUpdateUniqueNameDate() {
		return lastUpdateUniqueNameDate;
	}

	public void setLastUpdateUniqueNameDate(Date lastUpdateUniqueNameDate) {
		this.lastUpdateUniqueNameDate = lastUpdateUniqueNameDate;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<UserHashTag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<UserHashTag> hashtags) {
		this.hashtags = hashtags;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public List<Event> getCreatedEvent() {
		return createdEvent;
	}

	public void setCreatedEvent(List<Event> createdEvent) {
		this.createdEvent = createdEvent;
	}

	public List<Event> getApprovedEvent() {
		return approvedEvent;
	}

	public void setApprovedEvent(List<Event> approvedEvent) {
		this.approvedEvent = approvedEvent;
	}

	public String getLikeCoinId() {
		return likeCoinId;
	}

	public void setLikeCoinId(String likeCoinId) {
		this.likeCoinId = likeCoinId;
	}

	public String getFbId() {
		return fbId;
	}

	public void setFbId(String fbId) {
		this.fbId = fbId;
	}

}