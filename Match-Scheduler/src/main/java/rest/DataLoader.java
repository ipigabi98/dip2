package rest;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rest.data.entities.*;
import rest.data.exceptions.EmailAlreadyRegisteredException;
import rest.data.exceptions.RoleAlreadyExistsException;
import rest.data.exceptions.UserNotFoundByIdException;
import rest.repos.AppRoleRepo;
import rest.repos.AppUserRepo;
import rest.repos.ScheduleDataRepo;
import rest.repos.ScheduleProgressRepo;
import rest.services.AppUserService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final String ROLE_USER = "ROLE_USER";
    private final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AppUserService userService;
    private final ScheduleDataRepo dataRepo;
    private final ScheduleProgressRepo progressRepo;
    private final AppRoleRepo roleRepo;
    private final AppUserRepo userRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing data...");

        createRandomData();
        createSpecificData();

        System.out.println("Finished.");
    }

    private void createSpecificData() throws EmailAlreadyRegisteredException, IOException {
        System.out.println("Initializing specific data...");

        AppUser newUser = new AppUser();
        newUser.setEmail("user@gmail.com");
        newUser.setPassword("password");
        newUser.getRoles().addAll(roleRepo.findAll());
        userService.saveUser(newUser);

        /*for (int i = 1; i <= 5; i++) {
            ScheduleData data = new ScheduleData();

            File input = new File("files/inputs/test" + i + ".xml");
            File output = new File("files/outputs/result" + i + ".xml");

            data.setConstraintsFileName(input.getName());

            byte[] inputByteArray = FileUtils.readFileToByteArray(input);
            byte[] outputByteArray = FileUtils.readFileToByteArray(output);

            data.setConstraints(inputByteArray);

            ScheduleProgress progress = new ScheduleProgress();
            if (i == 1 || i == 5) {
                progress.setCreatedDate(LocalDateTime.now().minusDays(5));
                progress.setFinishedDate(LocalDateTime.now().minusDays(4));
                progress.setStatus(ScheduleProgressStatus.COMPLETED);

                data.setResult(outputByteArray);
            } else if (i == 2) {
                progress.setCreatedDate(LocalDateTime.now().minusDays(7));
                progress.setFinishedDate(LocalDateTime.now().minusDays(6));
                progress.setStatus(ScheduleProgressStatus.FAILED);

                data.setResult(outputByteArray);
            } else {
                progress.setCreatedDate(LocalDateTime.now().minusHours(5));
                progress.setStatus(ScheduleProgressStatus.IN_PROGRESS);
            }

            progressRepo.save(progress);
            data.setProgress(progress);

            dataRepo.save(data);
            newUser.getUploadedConstraints().add(data);

        }*/
        userRepo.save(newUser);
    }

    private void createRandomData() throws EmailAlreadyRegisteredException, RoleAlreadyExistsException, UserNotFoundByIdException, IOException {
        Set<Long> userIds = createUsers();
        createRoles();
        assignRoles(userIds);
        createScheduleData(userIds);
    }

    private Set<Long> createUsers() throws EmailAlreadyRegisteredException {
        Set<Long> userIds = new HashSet<>();

        for (int i = 1; i < 101; i++) {
            AppUser newUser = new AppUser();
            newUser.setEmail("test" + i + "@gmail.com");
            newUser.setPassword("password");
            AppUser createdUser = userService.saveUser(newUser);
            userIds.add(createdUser.getId());
        }

        return userIds;
    }

    private void createRoles() throws RoleAlreadyExistsException {
        userService.saveRole(new AppRole(null, ROLE_USER));
        userService.saveRole(new AppRole(null, ROLE_ADMIN));
    }

    private void assignRoles(Set<Long> userIds) throws UserNotFoundByIdException {
        int min = 1;
        int max = 3;

        for (Long id : userIds) {
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);

            if (randomNum == 1) {
                userService.assignRoleToUser(id, ROLE_USER);
            }

            if (randomNum == 2) {
                userService.assignRoleToUser(id, ROLE_ADMIN);
            }

            if (randomNum == 3) {
                userService.assignRoleToUser(id, ROLE_USER);
                userService.assignRoleToUser(id, ROLE_ADMIN);
            }
        }
    }

    private void createScheduleData(Set<Long> userIds) throws IOException, UserNotFoundByIdException {
        for (Long id : userIds) {
            AppUser user = userService.findById(id);

            int numberOfFiles = ThreadLocalRandom.current().nextInt(1, 5 + 1);
            List<Integer> fileIds = pickNumbers(numberOfFiles);

            for (Integer i : fileIds) {
                ScheduleData data = new ScheduleData();

                File input = new File("files/inputs/test" + i + ".xml");
                File output = new File("files/outputs/result" + i + ".xml");

                data.setConstraintsFileName(input.getName());

                byte[] inputByteArray = FileUtils.readFileToByteArray(input);
                byte[] outputByteArray = FileUtils.readFileToByteArray(output);

                ScheduleProgress progress = createProgress();
                data.setConstraints(inputByteArray);
                data.setProgress(progress);

                if (progress.getStatus() == ScheduleProgressStatus.COMPLETED) {
                    data.setResult(outputByteArray);
                }

                dataRepo.save(data);
                user.getUploadedConstraints().add(data);
            }

            userRepo.save(user);
        }
    }

    private ScheduleProgress createProgress() {
        ScheduleProgress progress = new ScheduleProgress();
        int randomNum = ThreadLocalRandom.current().nextInt(1, 3 + 1);

        switch (randomNum) {
            case 1: {
                progress.setStatus(ScheduleProgressStatus.IN_PROGRESS);
                progress.setCreatedDate(LocalDateTime.now().minusHours(1));
                break;
            }
            case 2: {
                progress.setStatus(ScheduleProgressStatus.COMPLETED);
                progress.setCreatedDate(LocalDateTime.now().minusDays(10));
                progress.setFinishedDate(LocalDateTime.now().minusDays(9));
                break;
            }
            case 3: {
                progress.setStatus(ScheduleProgressStatus.FAILED);
                progress.setCreatedDate(LocalDateTime.now().minusDays(11));
                progress.setFinishedDate(LocalDateTime.now().minusDays(10));
                break;
            }
        }

        return progressRepo.save(progress);
    }

    private List<Integer> pickNumbers(int numberOfFiles) {
        int counter = numberOfFiles;
        List<Integer> result = new ArrayList<>();

        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);

        while (counter != 0) {
            Random random = new Random();
            int index = random.nextInt(numbers.size());
            int removedItem = numbers.remove(index);
            result.add(removedItem);
            counter--;
        }

        return result;
    }
}
