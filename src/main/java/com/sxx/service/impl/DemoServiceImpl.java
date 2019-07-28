package main.java.com.sxx.service.impl;

import main.java.com.sxx.annotation.SxxAutowired;
import main.java.com.sxx.annotation.SxxService;
import main.java.com.sxx.dao.DemoDao;
import main.java.com.sxx.service.DemoService;

/** 
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-28 ����10:27:06 
 * ˵�� :
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
