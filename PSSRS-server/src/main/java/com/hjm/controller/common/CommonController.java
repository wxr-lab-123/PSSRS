package com.hjm.controller.common;

import com.hjm.constant.MessageConstant;
import com.hjm.result.Result;
import com.hjm.utils.AliOSSUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@Api(tags = "通用接口")
public class CommonController {
    @Autowired
    private AliOSSUtil ossUtil;
    @PostMapping("/api/admin/files/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("文件上传: {}  文件夹: {}", file.getOriginalFilename(), folder);

        try {
            // 上传逻辑（ossUtil 支持 folder 参数时一起传入）
            String url = ossUtil.upload(file); // 例如上传到 OSS 并返回完整 URL
            String path = folder != null ? folder + "/" + file.getOriginalFilename() : file.getOriginalFilename();

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("path", path);

            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

}
