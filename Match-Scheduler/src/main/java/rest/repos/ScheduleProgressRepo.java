package rest.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import rest.data.entities.ScheduleProgress;

public interface ScheduleProgressRepo extends JpaRepository<ScheduleProgress, Long> {
}
