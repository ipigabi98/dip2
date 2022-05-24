package rest.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import rest.data.entities.ScheduleData;

public interface ScheduleDataRepo extends JpaRepository<ScheduleData, Long> {
}
