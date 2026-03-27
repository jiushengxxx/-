package com.ct.service.ForegroundService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileuploadService {

    @Value("${web.upload-path}")
    private String uploadPath;

    @Value("${web.ipAddr}")
    private String ipAddr;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");

    public List<String> imgUpload(MultipartFile[] files) {

        List<String> result = new ArrayList<>();

        for (MultipartFile uploadFile : files) {

            if(uploadFile.isEmpty()){
                continue;
            }

            String format = sdf.format(new Date());

            File folder = new File(uploadPath + format);

            if(!folder.exists()){
                folder.mkdirs();
            }

            String fileName = System.currentTimeMillis()
                    + "-" + uploadFile.getOriginalFilename();

            try {

                File dest = new File(folder, fileName);

                uploadFile.transferTo(dest);

                String imgUrl = ipAddr + format + fileName;

                result.add(imgUrl);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
    }
}