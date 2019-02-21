package com.stylefeng.guns;

import com.stylefeng.guns.rest.AlipayApplication;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlipayApplication.class)
public class GunsRestApplicationTests {
	@Autowired
	private FTPUtil ftpUtil;
	@Test
	public void contextLoads() {
		File file = new File("C:\\Users\\Administrator\\Desktop\\QRCODE\\qr-124583135asdf81222.png");
		String fileStrByAddress = ftpUtil.getFileStrByAddress("seats/cgs.json");
		boolean b = ftpUtil.uploadFile("qr-124583135asdf81222.png", file);
		System.out.println("=====================================上传============"+b);
		System.out.println(fileStrByAddress);
	}

}
