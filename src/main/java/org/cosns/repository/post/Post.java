package org.cosns.repository.post;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.cosns.repository.User;
import org.cosns.repository.hashtag.PostHashTag;
import org.cosns.repository.image.PostImage;
import org.cosns.repository.postreaction.LikeReaction;
import org.cosns.repository.postreaction.PostCommentReaction;
import org.cosns.util.Auditable;
import org.cosns.util.ConstantsUtil;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "post")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "post_type")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public abstract class Post extends Auditable<String> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postId;

	private String postKey;

	private Date releaseDate;

	private int weight = 100;
	
	@Lob
	@Column(nullable = true)
	private String message;

	@JsonIgnore
	@NotNull
	@Size(max = 1)
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@OrderBy("seq ASC")
	@Where(clause = "status = '" + ConstantsUtil.IMAGE_ACTIVE + "'")
	private List<PostImage> postImages;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private List<PostHashTag> hashtags;

	@JsonIgnore
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<LikeReaction> likeReaction;

	@JsonIgnore
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private Set<PostCommentReaction> commentReaction;

	Long totalViewCount;

	@Transient
	Long todayViewCount;

	@Transient
	Long likeCount;

	@Transient
	Long likeCoinCount;

	@Transient
	Long retweetCount;

	@Transient
	boolean isLiked;

	@Transient
	boolean isRetweeted;

	@Transient
	boolean isRemoved = false;

	@Transient
	List<Post> retweetedBy;

	public String getType() {
		DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);

		return val == null ? null : val.value();
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<PostImage> getPostImages() {
		return postImages;
	}

	public void setPostImages(List<PostImage> postImages) {
		this.postImages = postImages;
	}

	public List<PostHashTag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(List<PostHashTag> hashtags) {
		this.hashtags = hashtags;
	}


	public Set<LikeReaction> getLikeReaction() {
		return likeReaction;
	}

	public void setLikeReaction(Set<LikeReaction> likeReaction) {
		this.likeReaction = likeReaction;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(Long retweetCount) {
		this.retweetCount = retweetCount;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean liked) {
		this.isLiked = liked;
	}

	public boolean isRetweeted() {
		return isRetweeted;
	}

	public void setRetweeted(boolean retweeted) {
		this.isRetweeted = retweeted;
	}

	public List<Post> getRetweetedBy() {
		return retweetedBy;
	}

	public void setRetweetedBy(List<Post> retweetedBy) {
		this.retweetedBy = retweetedBy;
	}

	public Long getTotalViewCount() {
		return totalViewCount;
	}

	public void setTotalViewCount(Long totalViewCount) {
		this.totalViewCount = totalViewCount;
	}

	public Long getTodayViewCount() {
		return todayViewCount;
	}

	public void setTodayViewCount(Long todayViewCount) {
		this.todayViewCount = todayViewCount;
	}

	public boolean isRemoved() {
		return isRemoved;
	}

	public void setRemoved(boolean isRemoved) {
		this.isRemoved = isRemoved;
	}

	public String getPostKey() {
		return postKey;
	}

	public void setPostKey(String postKey) {
		this.postKey = postKey;
	}

	public Long getLikeCoinCount() {
		return likeCoinCount;
	}

	public void setLikeCoinCount(Long likeCoinCount) {
		this.likeCoinCount = likeCoinCount;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public Set<PostCommentReaction> getCommentReaction() {
		return commentReaction;
	}

	public void setCommentReaction(Set<PostCommentReaction> commentReaction) {
		this.commentReaction = commentReaction;
	}

	@Override
	public String toString() {
		return "Post [postId=" + postId + ", releaseDate=" + releaseDate + " createDate : " + super.getCreatedate() + "]";
	}

}