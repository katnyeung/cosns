package org.cosns.scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.cosns.service.EventService;
import org.cosns.service.HashTagService;
import org.cosns.service.PostService;
import org.cosns.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SupportTaskScheduler {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	PostService postService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	HashTagService hashTagService;
	
	@Scheduled(cron = "0 0 */4 * * *")
	public void scheduleDailySyncPostCount() {
		logger.info("Cron Task :: Execution Time - " + dateTimeFormatter.format(LocalDateTime.now()));
		
		postService.syncPostCountToDB();
	}	
	
	public void resetKeysInRedis() {
		
		postService.resetPostKeyToRedis();
		
		userService.resetUserKeyToRedis();
		
		eventService.resetEventKeyToRedis();
		
		hashTagService.resetHashTagKeyToRedis();
	}
}
