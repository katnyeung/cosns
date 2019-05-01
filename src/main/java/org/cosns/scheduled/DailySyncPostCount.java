package org.cosns.scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import org.cosns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailySyncPostCount {
	Logger logger = Logger.getLogger(this.getClass().getName());
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	PostService postService;
	
	@Scheduled(cron = "0 0 */4 * * *")
	public void scheduleTaskWithCronExpression() {
		logger.info("Cron Task :: Execution Time - " + dateTimeFormatter.format(LocalDateTime.now()));
		
		postService.syncPostCountToDB();
	}
}
