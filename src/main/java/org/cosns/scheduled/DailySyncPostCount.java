package org.cosns.scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;

import org.cosns.service.PostService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailySyncPostCount {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	PostService postService;
	
	@Scheduled(cron = "0 0 */4 * * *")
	public void scheduleTaskWithCronExpression() {
		logger.info("Cron Task :: Execution Time - " + dateTimeFormatter.format(LocalDateTime.now()));
		
		postService.syncPostCountToDB();
	}
}
