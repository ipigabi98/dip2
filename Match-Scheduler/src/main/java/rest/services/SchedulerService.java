package rest.services;

import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import rest.data.dto.AdminScheduleDataDto;
import rest.data.dto.ResultDto;
import rest.data.dto.ScheduleDataDto;
import rest.data.exceptions.*;
import rest.data.requests.DeleteScheduleDataRequest;
import rest.data.requests.UploadManualInputsRequest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

public interface SchedulerService {
    Future<String> runAlgorithm(Long dataId) throws ScheduleDataNotFoundException, TryAgainLaterException, IOException, InvalidXMLException, InterruptedException;
    List<ResultDto> getResultFromByte(Long dataId) throws ScheduleDataNotFoundException, TryAgainLaterException, IOException, InvalidXMLException, ParserConfigurationException, SAXException;
    List<ScheduleDataDto> getSchedulerDataByUserId() throws UserNotFoundByEmailException;
    List<AdminScheduleDataDto> getSchedulerData() throws UserNotFoundByEmailException;
    Long uploadScheduleInputs(MultipartFile multipartFile) throws UserNotFoundByEmailException, IOException, TryAgainLaterException, InvalidXMLException;
    Long uploadManualInputs(UploadManualInputsRequest request) throws ParserConfigurationException, TransformerException, UserNotFoundByEmailException, IOException;
    byte[] downloadUserData(Long fileId, String type) throws UserNotFoundByEmailException, ScheduleDataNotFoundException, DoNotHavePermissionException, NoResultForThisInputException, IllegalRequestException;
    byte[] downloadUserDataByAdmin(Long fileId, String type) throws NoResultForThisInputException, IllegalRequestException, ScheduleDataNotFoundException;
    void deleteAdminScheduleData(DeleteScheduleDataRequest requestData) throws ScheduleDataNotFoundException, UserNotFoundByIdException;
    void deleteUserScheduleData(Long fileId) throws UserNotFoundByEmailException, ScheduleDataNotFoundException;
}
