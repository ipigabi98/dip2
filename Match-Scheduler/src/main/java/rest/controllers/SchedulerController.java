package rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rest.data.dto.ResultDto;
import rest.data.dto.ScheduleDataDto;
import rest.data.dto.AdminScheduleDataDto;
import rest.data.requests.DeleteScheduleDataRequest;
import rest.data.requests.UploadManualInputsRequest;
import rest.services.SchedulerService;

import java.util.List;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
@EnableAsync
@CrossOrigin
@RequestMapping("")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @GetMapping("user/algorithm/{dataId}")
    public ResponseEntity<?> runAlgorithm(@PathVariable Long dataId) {
        try {
            Future<String> result = schedulerService.runAlgorithm(dataId);
            return ResponseEntity.ok().body(result.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("user/result/{dataId}")
    public ResponseEntity<?> getResultList(@PathVariable Long dataId) {
        try {
            List<ResultDto> result = schedulerService.getResultFromByte(dataId);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("admin/delete/schedule")
    public ResponseEntity<?> deleteAdminScheduleData(@RequestBody DeleteScheduleDataRequest requestData) {
        try {
            schedulerService.deleteAdminScheduleData(requestData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("user/delete/schedule/{fileId}")
    public ResponseEntity<?> deleteUserScheduleData(@PathVariable Long fileId) {
        try {
            schedulerService.deleteUserScheduleData(fileId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "user/download/input/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadUserInput(@PathVariable Long fileId) {
        try {
            byte[] input = schedulerService.downloadUserData(fileId, "input");
            return ResponseEntity.ok().body(input);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "user/download/result/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadUserResult(@PathVariable Long fileId) {
        try {
            byte[] result = schedulerService.downloadUserData(fileId, "result");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "admin/download/input/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadUserInputByAdmin(@PathVariable Long fileId) {
        try {
            byte[] input = schedulerService.downloadUserDataByAdmin(fileId, "input");
            return ResponseEntity.ok().body(input);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "admin/download/result/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadUserResultByAdmin(@PathVariable Long fileId) {
        try {
            byte[] result = schedulerService.downloadUserDataByAdmin(fileId, "result");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("user/schedule/data")
    public ResponseEntity<?> getUserScheduleData() {
        try {
            List<ScheduleDataDto> result = schedulerService.getSchedulerDataByUserId();
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("admin/schedule/data")
    public ResponseEntity<?> getAdminScheduleData() {
        try {
            List<AdminScheduleDataDto> result = schedulerService.getSchedulerData();
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("user/schedule/upload")
    public ResponseEntity<?> uploadScheduleInputs(@RequestParam("file") MultipartFile multipartFile) {
        try {
            Long scheduleDataId = schedulerService.uploadScheduleInputs(multipartFile);
            return ResponseEntity.ok().body(scheduleDataId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("user/schedule/manual")
    public ResponseEntity<?> uploadManualInputs(@RequestBody UploadManualInputsRequest request) {
        try {
            Long scheduleDataId = schedulerService.uploadManualInputs(request);
            return ResponseEntity.ok().body(scheduleDataId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
