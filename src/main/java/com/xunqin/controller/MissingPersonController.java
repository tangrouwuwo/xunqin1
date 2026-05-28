package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.MissingPerson;
import com.xunqin.service.MissingPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/missing-persons")
public class MissingPersonController {

    @Autowired
    private MissingPersonService missingPersonService;

    @GetMapping
    public Result<Page<MissingPerson>> searchMissingPersons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Page<MissingPerson> page = missingPersonService.searchMissingPersons(
                name, gender, location, province, startDate, endDate, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<MissingPerson> getMissingPersonById(@PathVariable Long id) {
        MissingPerson missingPerson = missingPersonService.getMissingPersonById(id);
        return Result.success(missingPerson);
    }

    @GetMapping("/hot")
    public Result<List<MissingPerson>> getHotMissingPersons(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<MissingPerson> list = missingPersonService.getHotMissingPersons(limit);
        return Result.success(list);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<MissingPerson> createMissingPerson(Authentication authentication,
                                                   @RequestBody MissingPerson missingPerson) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        MissingPerson result = missingPersonService.createMissingPerson(userId, missingPerson);
        return Result.success("创建成功，等待审核", result);
    }

    @PostMapping("/with-photos")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<MissingPerson> createMissingPersonWithPhotos(Authentication authentication,
                                                              @RequestParam("title") String title,
                                                              @RequestParam("name") String name,
                                                              @RequestParam(required = false) String gender,
                                                              @RequestParam("ageAtMissing") Integer ageAtMissing,
                                                              @RequestParam("missingDate") String missingDate,
                                                              @RequestParam("missingLocation") String missingLocation,
                                                              @RequestParam(required = false) Integer height,
                                                              @RequestParam(required = false) Integer weight,
                                                              @RequestParam(required = false) String appearance,
                                                              @RequestParam(required = false) String clothing,
                                                              @RequestParam(required = false) String specialFeatures,
                                                              @RequestParam(required = false) String missingCause,
                                                              @RequestParam(required = false) String description,
                                                              @RequestParam(required = false) String contactName,
                                                              @RequestParam(required = false) String contactPhone,
                                                              @RequestParam(required = false) MultipartFile[] photos) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        MissingPerson missingPerson = new MissingPerson();
        missingPerson.setTitle(title);
        missingPerson.setName(name);
        missingPerson.setGender(gender);
        missingPerson.setAgeAtMissing(ageAtMissing);
        missingPerson.setMissingDate(missingDate != null ? java.time.LocalDate.parse(missingDate) : null);
        missingPerson.setMissingLocation(missingLocation);
        missingPerson.setHeight(height);
        missingPerson.setWeight(weight);
        missingPerson.setAppearance(appearance);
        missingPerson.setClothing(clothing);
        missingPerson.setSpecialFeatures(specialFeatures);
        missingPerson.setMissingCause(missingCause);
        missingPerson.setDescription(description);
        missingPerson.setContactName(contactName);
        missingPerson.setContactPhone(contactPhone);
        MissingPerson result = missingPersonService.createMissingPerson(userId, missingPerson, photos);
        return Result.success("创建成功，等待审核", result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<MissingPerson> updateMissingPerson(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @RequestBody MissingPerson missingPerson) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        MissingPerson result = missingPersonService.updateMissingPerson(id, userId, missingPerson);
        return Result.success("更新成功，等待审核", result);
    }

    @PutMapping("/{id}/with-photos")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<MissingPerson> updateMissingPersonWithPhotos(Authentication authentication,
                                                              @PathVariable Long id,
                                                              @RequestParam("title") String title,
                                                              @RequestParam("name") String name,
                                                              @RequestParam(required = false) String gender,
                                                              @RequestParam("ageAtMissing") Integer ageAtMissing,
                                                              @RequestParam("missingDate") String missingDate,
                                                              @RequestParam("missingLocation") String missingLocation,
                                                              @RequestParam(required = false) Integer height,
                                                              @RequestParam(required = false) Integer weight,
                                                              @RequestParam(required = false) String bloodType,
                                                              @RequestParam(required = false) String appearance,
                                                              @RequestParam(required = false) String clothing,
                                                              @RequestParam(required = false) String specialFeatures,
                                                              @RequestParam(required = false) String missingCause,
                                                              @RequestParam(required = false) String description,
                                                              @RequestParam(required = false) String contactName,
                                                              @RequestParam(required = false) String contactPhone,
                                                              @RequestParam(required = false) MultipartFile[] photos) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        MissingPerson missingPerson = new MissingPerson();
        missingPerson.setTitle(title);
        missingPerson.setName(name);
        missingPerson.setGender(gender);
        missingPerson.setAgeAtMissing(ageAtMissing);
        missingPerson.setMissingDate(missingDate != null ? java.time.LocalDate.parse(missingDate) : null);
        missingPerson.setMissingLocation(missingLocation);
        missingPerson.setHeight(height);
        missingPerson.setWeight(weight);
        missingPerson.setAppearance(appearance);
        missingPerson.setClothing(clothing);
        missingPerson.setSpecialFeatures(specialFeatures);
        missingPerson.setMissingCause(missingCause);
        missingPerson.setDescription(description);
        missingPerson.setContactName(contactName);
        missingPerson.setContactPhone(contactPhone);
        MissingPerson result = missingPersonService.updateMissingPerson(id, userId, missingPerson, photos);
        return Result.success("更新成功，等待审核", result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<?> deleteMissingPerson(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        missingPersonService.deleteMissingPerson(id, userId);
        return Result.success("删除成功");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SEEKER')")
    public Result<Page<MissingPerson>> getMyMissingPersons(Authentication authentication,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<MissingPerson> page = missingPersonService.getMissingPersonsBySeeker(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
