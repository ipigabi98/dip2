package rest.mappers.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import rest.data.dto.ScheduleDataDto;
import rest.data.entities.ScheduleData;
import rest.data.entities.ScheduleProgressStatus;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel="spring")
public interface UserScheduleDataMapper {

    @Mappings({
            @Mapping(source = "data.id", target = "id"),
            @Mapping(source = "data.progress.createdDate", target = "uploadDate"),
            @Mapping(source = "data.constraintsFileName", target = "inputFileName"),
            @Mapping(source = "data", target = "resultFileName", qualifiedByName = "generateResultFileName"),
            @Mapping(source = "data.progress.status", target = "status", qualifiedByName = "convertStatusToString"),
            @Mapping(source = "data.progress.finishedDate", target = "finishedDate")
    })
    ScheduleDataDto fromEntityToDto(ScheduleData data);

    @Named("convertStatusToString")
    static String convertStatusToString(ScheduleProgressStatus status) {
        return status.name();
    }

    @Named("generateResultFileName")
    static String generateResultFileName(ScheduleData data) {
        if (data.getProgress().getStatus() != ScheduleProgressStatus.COMPLETED) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
        //String dateString = data.getProgress().getFinishedDate().format(formatter);
        return "result_" + data.getConstraintsFileName();
    }
}
