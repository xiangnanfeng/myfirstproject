package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.FastDFSClient;

@RestController
public class UpLoadController {

    @Value("${FILE_SERVER_URL}")
    private String ip;

    @RequestMapping("/upLoad")
    public Result upLoad(MultipartFile file){
        //获取上传的全文件名
        String originalFilename = file.getOriginalFilename();
        //通过字符串截取，获得后缀
        int index = originalFilename.lastIndexOf(".");

        String suName = originalFilename.substring(index + 1);

        String url="";
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            String filePath = fastDFSClient.uploadFile(file.getBytes(), suName);
            url=ip+filePath;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
