package org.magnum.mobilecloud.video.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.model.AverageVideoRating;
import org.magnum.mobilecloud.video.model.UserVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.magnum.mobilecloud.video.model.VideoStatus;
import org.magnum.mobilecloud.video.repo.UserVideoRatingRepository;
import org.magnum.mobilecloud.video.repo.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

	@Autowired
	private VideoRepository videoRepository;

	@Autowired
	private UserVideoRatingRepository userVideoRatingRepository;

	@Autowired
	private VideoFileManager videoFileManager;

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video, Principal principal) {

		video.setOwner(principal.getName());

		return videoRepository.save(video);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return (Collection<Video>) videoRepository.findAll();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") Long id, @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, Principal principal, HttpServletResponse response) throws IOException {
		Video video = videoRepository.findOne(id);
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		if (!video.getOwner().equals(principal.getName())) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		videoFileManager.saveVideoData(video, videoData.getInputStream());
		return new VideoStatus(VideoStatus.VideoState.READY);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getData(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
		Video video = videoRepository.findOne(id);
		if (video == null || !videoFileManager.hasVideoData(video)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		videoFileManager.copyVideoData(video, response.getOutputStream());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_RATING_PATH, method = RequestMethod.GET)
	public @ResponseBody AverageVideoRating getVideoRating(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
		Video video = videoRepository.findOne(id);
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		Collection<UserVideoRating> ratings = video.getRatings();
		if (ratings == null) {
			ratings = Collections.emptyList();
		}

		int sum = 0;
		for (UserVideoRating i : ratings) {
			sum += i.getRating();
		}

		return new AverageVideoRating(sum / ratings.size(), video.getId(), ratings.size());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_RATING_PATH + "/{value}", method = RequestMethod.POST)
	public @ResponseBody Video rateVideo(@PathVariable("id") Long id, @PathVariable("value") Double rating, Principal principal, HttpServletResponse response) throws Exception {
		Video video = videoRepository.findOne(id);
		if (video == null || rating == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		Collection<UserVideoRating> ratings = video.getRatings();
		if (ratings == null) {
			ratings = new ArrayList<>();
			video.setRatings(ratings);
		}
		UserVideoRating userVideoRating = userVideoRatingRepository.findByUser(principal.getName());
		if (userVideoRating == null) {
			userVideoRating = new UserVideoRating(video, rating, principal.getName());
			userVideoRatingRepository.save(userVideoRating);
			ratings.add(userVideoRating);
			videoRepository.save(video);
		} else {
			userVideoRating.setRating(rating);
			userVideoRatingRepository.save(userVideoRating);
		}

		return video;
	}

	private String getDataUrl(long videoId) {
		return getUrlBaseForLocalServer() + VideoSvcApi.VIDEO_DATA_PATH.replace("{id}", String.valueOf(videoId));
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base = "http://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "");
		return base;
	}
}
