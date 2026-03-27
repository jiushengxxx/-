package com.ct.controller.ForegroundController;

import com.ct.service.ForegroundService.FileuploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/fileUpload")
public class FileuploadController {

    @Resource
    private FileuploadService fileuploadService;

    @PostMapping("/imgUpload")
    public List<String> imgUpload(@RequestParam("file") MultipartFile[] file){

        return fileuploadService.imgUpload(file);

    }

}