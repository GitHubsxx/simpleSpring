package main.java.com.sxx.service.impl;

import main.java.com.sxx.annotation.SxxAutowired;
import main.java.com.sxx.annotation.SxxService;
import main.java.com.sxx.dao.DemoDao;
import main.java.com.sxx.service.DemoService;

/** 
 * @author 作者 : sxx
 * @version 创建时间：2019-7-28 上午10:27:06 
 * 说明 :
 */
@SxxService
public class DemoServiceImpl implements DemoService{
	@SxxAutowired
	private DemoDao demoDao;

	public String get(String name) {
		
		//return this.demoDao.get(name);
		return "Hello "+name;
	}

}
