package org.cosns.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.cosns.repository.Event;
import org.cosns.util.ConstantsUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventDAO extends JpaRepository<Event, Long> {

	@Query("FROM Event e WHERE e.start >= :start AND e.end <= :end AND e.status = '" + ConstantsUtil.EVENT_ACTIVE + "' ORDER BY e.createdate DESC")
	public Set<Event> findActiveEvent(@Param("start") Date start, @Param("end") Date end);

	@Query("FROM Event e WHERE e.eventId = :eventId AND e.status = '" + ConstantsUtil.EVENT_ACTIVE + "' ORDER BY e.createdate DESC")
	public Set<Event> getEventById(@Param("eventId") Long eventId);

	@Query("FROM Event e WHERE e.eventId IN :eventId AND e.status = '" + ConstantsUtil.EVENT_ACTIVE + "' ORDER BY e.createdate DESC")
	public List<Event> getEventByIdList(@Param("eventId") Set<Long> eventIdList);

	@Query("FROM Event e WHERE e.eventKey = :eventKey AND e.status = '" + ConstantsUtil.EVENT_ACTIVE + "' ORDER BY e.createdate DESC")
	public Set<Event> getEventByEventKey(@Param("eventKey") String eventKey);
}
