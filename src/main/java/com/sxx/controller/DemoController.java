package main.java.com.sxx.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.com.sxx.annotation.SxxAutowired;
import main.java.com.sxx.annotation.SxxController;
import main.java.com.sxx.annotation.SxxRequestMapping;
import main.java.com.sxx.annotation.SxxRequestParam;
import main.java.com.sxx.service.DemoService;

/** 
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-28 ����10:23:36 
 * ˵�� :ҳ�����룺http://localhost:8080/demo/add?name=sxx
 */
@SxxRequestMapping("/demo")
@SxxController
public class DemoController {
	@SxxAutowired
	private DemoService demoService;
	
	@SxxRequestMapping("/add")
	public void add(HttpServletRequest req,HttpServletResponse resp
			,@SxxRequestParam("name")String name){
		String result = this.demoService.get(name);
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
