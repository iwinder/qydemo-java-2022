package com.windcoder.updateFile.controller;

import com.sun.xml.internal.ws.api.pipe.Tube;
import com.windcoder.updateFile.config.FileUploadProperties;
import com.windcoder.updateFile.service.FileService;
import com.windcoder.updateFile.service.TUserService;
import com.windcoder.updateFile.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/file")
public class FileController {
	@Autowired
	private FileService fileService;
	@Autowired
	private TUserService userService;

	@GetMapping("outputFile")
	public String outputFile(@RequestParam(value = "name", required = false) String savePath,
							 @RequestParam(value = "isAppend", defaultValue = "true") boolean isAppend,
							 @RequestParam(value = "total", defaultValue = "500000") int total){

		long timer = 0;
		try {
			timer = fileService.outputFile(savePath, isAppend, total);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "导入时间：" + timer;

	}
	@GetMapping("fillUser")
	public String fillUser() {
		long timer = userService.redFile(FileUploadProperties.CONTENTPATH+FileUploadProperties.DEFAULTSAVEPATH+"/会员"+DateUtil.getDayStr() +".txt");
		return "处理时间：" + timer;
	}

	@GetMapping("downloadFile")
	public String downloadFile(){
		long timer = userService.downloadFile();
		return "下载处理时间：" + timer;
	}

	@GetMapping("updateFile")
	public String updateFile(){
		long timer = userService.updateFile();
		return "上传处理时间：" + timer;
	}


}
