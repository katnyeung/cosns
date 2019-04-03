package org.cosns.dao;

import java.util.Date;
import java.util.Set;

import org.cosns.repository.Event;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventDAO extends JpaRepository<Event, Long> {

	@Query("FROM Event e WHERE e.start >= :start AND e.end <= :end AND e.status = '" + ConstantsUtil.EVENT_ACTIVE + "' ORDER BY e.createdate DESC")
	public Set<Event> findActiveEvent(@Param("start") Date start, @Param("end") Date end);
}
