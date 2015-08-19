package org.magnum.mobilecloud.video.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

// You might want to annotate this with Jpa annotations, add an id field,
// and store it in the database...
//
// There are also plenty of other solutions that do not require
// persisting instances of this...
@Entity
public class UserVideoRating {

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(optional = false)
	private Video video;

	@Column
	private double rating;

	@Column
	private String user;

	public UserVideoRating() {
	}

	public UserVideoRating(Video video, double rating, String user) {
		super();
		this.video = video;
		this.rating = rating;
		this.user = user;
	}

	public Video getVideo() {
		return video;
	}

	public void setVideo(Video video) {
		this.video = video;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
