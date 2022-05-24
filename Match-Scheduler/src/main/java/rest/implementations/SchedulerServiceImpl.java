package rest.implementations;

import logic.parser.MyXMLParser;
import logic.scheduler.MatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import rest.data.dto.AdminScheduleDataDto;
import rest.data.dto.ResultDto;
import rest.data.dto.ScheduleDataDto;
import rest.data.entities.AppUser;
import rest.data.entities.ScheduleData;
import rest.data.entities.ScheduleProgress;
import rest.data.entities.ScheduleProgressStatus;
import rest.data.exceptions.*;
import rest.data.requests.DeleteScheduleDataRequest;
import rest.data.requests.UploadManualInputsRequest;
import rest.data.utilities.XMLUtility;
import rest.mappers.interfaces.AdminScheduleDataMapper;
import rest.mappers.interfaces.UserScheduleDataMapper;
import rest.repos.AppUserRepo;
import rest.repos.ScheduleDataRepo;
import rest.repos.ScheduleProgressRepo;
import rest.security.IAuthenticationFacade;
import rest.services.SchedulerService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Transactional
public class SchedulerServiceImpl implements SchedulerService {
    private final AppUserRepo userRepo;
    private final ScheduleDataRepo dataRepo;
    private final ScheduleProgressRepo progressRepo;

    private final UserScheduleDataMapper userScheduleDataMapper;
    private final AdminScheduleDataMapper adminScheduleDataMapper;

    private final IAuthenticationFacade authenticationFacade;

    @Override
    @Async
    public Future<String> runAlgorithm(Long dataId) throws ScheduleDataNotFoundException, TryAgainLaterException, IOException, InvalidXMLException, InterruptedException {
        ScheduleData data = dataRepo.findById(dataId).orElseThrow(ScheduleDataNotFoundException::new);
        String tempFilePath = "files/temporary/" + dataId + ".xml";

        ScheduleProgress progress = data.getProgress();
        MatchScheduler scheduler = null;
        boolean isXMLValid = true;
        try {
            scheduler = parseXml(data.getConstraints(), tempFilePath);
        } catch (Exception e) {
            isXMLValid = false;
            progress.setStatus(ScheduleProgressStatus.FAILED);
        }

        try {
            if (isXMLValid) {
                byte[] result = scheduler.optimize();
                if (result != null) {
                    progress.setStatus(ScheduleProgressStatus.COMPLETED);
                    data.setResult(result);
                }
            }
        } catch (Exception e) {
            progress.setStatus(ScheduleProgressStatus.FAILED);
        }

        progress.setFinishedDate(LocalDateTime.now());
        progressRepo.save(progress);

        dataRepo.save(data);

        return new AsyncResult<>("finished");
    }

    @Override
    public List<ResultDto> getResultFromByte(Long dataId) throws ScheduleDataNotFoundException, TryAgainLaterException, IOException, InvalidXMLException, ParserConfigurationException, SAXException {
        ScheduleData data = dataRepo.findById(dataId).orElseThrow(ScheduleDataNotFoundException::new);
        return XMLUtility.getResultsFromByteArray(data.getResult(), data.getId());
    }

    @Override
    public List<ScheduleDataDto> getSchedulerDataByUserId() throws UserNotFoundByEmailException {
        AppUser user = checkUserExists();
        Collection<ScheduleData> dataList = user.getUploadedConstraints();
        List<ScheduleDataDto> resultList = new ArrayList<>();

        for (ScheduleData data : dataList) {
            ScheduleDataDto dataDto = userScheduleDataMapper.fromEntityToDto(data);
            resultList.add(dataDto);
        }

        return resultList;
    }

    @Override
    public List<AdminScheduleDataDto> getSchedulerData() {
        Collection<ScheduleData> dataList = dataRepo.findAll();

        List<AdminScheduleDataDto> resultList = new ArrayList<>();

        for (ScheduleData data : dataList) {
            Long userId = getScheduleDataUserId(data);
            ScheduleDataDto dataDto = userScheduleDataMapper.fromEntityToDto(data);
            AdminScheduleDataDto adminScheduleDataDto = adminScheduleDataMapper.userDataToAdminData(dataDto);
            adminScheduleDataDto.setUserId(userId);
            resultList.add(adminScheduleDataDto);
        }

        return resultList;
    }

    private Long getScheduleDataUserId(ScheduleData data) {
        for (AppUser user : userRepo.findAll()) {
            for (ScheduleData scheduleData : user.getUploadedConstraints()) {
                if (scheduleData.equals(data)) {
                    return user.getId();
                }
            }
        }

        return null;
    }

    @Override
    public Long uploadScheduleInputs(MultipartFile multipartFile) throws UserNotFoundByEmailException, IOException, TryAgainLaterException, InvalidXMLException {
        AppUser user = checkUserExists();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        String dateString = LocalDateTime.now().format(formatter);

        String tempFileName = user.getId() + "_" + dateString + "_" + multipartFile.getOriginalFilename();
        String pathName = "files/temporary/" + tempFileName;

        parseXML(multipartFile, pathName);

        return saveScheduleData(multipartFile, user);
    }

    @Override
    public Long uploadManualInputs(UploadManualInputsRequest request) throws ParserConfigurationException, TransformerException, UserNotFoundByEmailException, IOException {
        Document document = XMLUtility.createXMLContent(request);
        byte[] array = XMLUtility.writeDocumentToByteArray(document);

        AppUser user = checkUserExists();
        return saveScheduleData(array, user);
    }

    @Override
    public byte[] downloadUserData(Long fileId, String type) throws UserNotFoundByEmailException, ScheduleDataNotFoundException, DoNotHavePermissionException, NoResultForThisInputException, IllegalRequestException {
        AppUser user = checkUserExists();
        ScheduleData data = dataRepo.findById(fileId).orElseThrow(ScheduleDataNotFoundException::new);
        if (!user.getUploadedConstraints().contains(data)) {
            throw new DoNotHavePermissionException();
        }

        if (type.equals("input")) {
            return data.getConstraints();
        }

        if (type.equals("result")) {
            if (data.getResult() == null) {
                throw new NoResultForThisInputException();
            } else {
                return data.getResult();
            }
        }

        throw new IllegalRequestException();
    }

    @Override
    public byte[] downloadUserDataByAdmin(Long fileId, String type) throws NoResultForThisInputException, IllegalRequestException, ScheduleDataNotFoundException {
        ScheduleData data = dataRepo.findById(fileId).orElseThrow(ScheduleDataNotFoundException::new);

        if (type.equals("input")) {
            return data.getConstraints();
        }

        if (type.equals("result")) {
            if (data.getResult() == null) {
                throw new NoResultForThisInputException();
            } else {
                return data.getResult();
            }
        }

        throw new IllegalRequestException();
    }

    @Override
    public void deleteAdminScheduleData(DeleteScheduleDataRequest requestData) throws ScheduleDataNotFoundException, UserNotFoundByIdException {
        AppUser user = userRepo.findById(requestData.getUserId()).orElseThrow(UserNotFoundByIdException::new);

        List<ScheduleData> itemsToRemove = new ArrayList<>();
        for (ScheduleData data : user.getUploadedConstraints()) {
            if (data.getId().equals(requestData.getFileId())) {
                itemsToRemove.add(data);
                break;
            }
        }
        user.getUploadedConstraints().removeAll(itemsToRemove);

        ScheduleData data = dataRepo.findById(requestData.getFileId()).orElseThrow(ScheduleDataNotFoundException::new);
        dataRepo.delete(data);
    }

    @Override
    public void deleteUserScheduleData(Long fileId) throws UserNotFoundByEmailException, ScheduleDataNotFoundException {
        AppUser user = checkUserExists();

        List<ScheduleData> itemsToRemove = new ArrayList<>();
        for (ScheduleData data : user.getUploadedConstraints()) {
            if (data.getId().equals(fileId)) {
                itemsToRemove.add(data);
                break;
            }
        }
        user.getUploadedConstraints().removeAll(itemsToRemove);

        ScheduleData data = dataRepo.findById(fileId).orElseThrow(ScheduleDataNotFoundException::new);
        dataRepo.delete(data);
    }

    private AppUser checkUserExists() throws UserNotFoundByEmailException {
        String email = authenticationFacade.getAuthentication().getName();
        AppUser user = userRepo.findByEmail(email);

        if (user == null) {
            throw new UserNotFoundByEmailException();
        }

        return user;
    }

    private void parseXML(MultipartFile multipartFile, String pathName) throws IOException, TryAgainLaterException, InvalidXMLException {
        File file = new File(pathName);
        if (!file.createNewFile()) {
            throw new TryAgainLaterException();
        };
        OutputStream os = new FileOutputStream(file);
        os.write(multipartFile.getBytes());
        os.close();
        MyXMLParser myXMLParser;
        try {
            myXMLParser = new MyXMLParser(file);
            myXMLParser.parse();
        } catch (Exception e) {
            if (!file.delete()) {
                throw new TryAgainLaterException();
            }
            throw new InvalidXMLException(e.getMessage());
        }
    }

    private MatchScheduler parseXml(byte[] input, String path) throws IOException, TryAgainLaterException, InvalidXMLException {
        File file = new File(path);
        if (!file.createNewFile()) {
            throw new TryAgainLaterException();
        };
        OutputStream os = new FileOutputStream(file);
        os.write(input);
        os.close();
        MyXMLParser myXMLParser;
        try {
            myXMLParser = new MyXMLParser(file);
            myXMLParser.parse();
        } catch (Exception e) {
            if (!file.delete()) {
                throw new TryAgainLaterException();
            }
            throw new InvalidXMLException(e.getMessage());
        }
        return myXMLParser.getScheduler();
    }

    private Long saveScheduleData(MultipartFile multipartFile, AppUser user) throws IOException {
        ScheduleProgress progress = new ScheduleProgress();
        progress.setStatus(ScheduleProgressStatus.IN_PROGRESS);
        progressRepo.save(progress);

        ScheduleData data = new ScheduleData();
        data.setProgress(progress);
        data.setConstraints(multipartFile.getBytes());
        data.setConstraintsFileName(multipartFile.getOriginalFilename());
        dataRepo.save(data);

        user.getUploadedConstraints().add(data);
        userRepo.save(user);

        return data.getId();
    }

    private Long saveScheduleData(byte[] content, AppUser user) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        String dateString = LocalDateTime.now().format(formatter);
        String fileName = dateString + "_" + user.getId() + ".xml";

        ScheduleProgress progress = new ScheduleProgress();
        progress.setStatus(ScheduleProgressStatus.IN_PROGRESS);
        progressRepo.save(progress);

        ScheduleData data = new ScheduleData();
        data.setProgress(progress);
        data.setConstraints(content);
        data.setConstraintsFileName(fileName);
        dataRepo.save(data);

        user.getUploadedConstraints().add(data);
        userRepo.save(user);

        return data.getId();
    }
}
