package main.java.com.sxx.dao.impl;

import main.java.com.sxx.annotation.SxxRepository;
import main.java.com.sxx.dao.DemoDao;

/** 
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-28 ����10:29:00 
 * ˵�� :
 */
@SxxRepository
public class DemoDaoImpl implements DemoDao{

	public String get(String name) {
		
		return "Hello sxx";
	}
	
}
