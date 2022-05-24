package rest.mappers.interfaces;

import org.mapstruct.Mapper;
import rest.data.dto.AdminScheduleDataDto;
import rest.data.dto.ScheduleDataDto;

@Mapper(componentModel="spring")
public interface AdminScheduleDataMapper {
    AdminScheduleDataDto userDataToAdminData(ScheduleDataDto scheduleDataDto);
}
