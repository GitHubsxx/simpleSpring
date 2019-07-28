package main.java.com.sxx.dao.impl;

import main.java.com.sxx.annotation.SxxRepository;
import main.java.com.sxx.dao.DemoDao;

/** 
 * @author 作者 : sxx
 * @version 创建时间：2019-7-28 上午10:29:00 
 * 说明 :
 */
@SxxRepository
public class DemoDaoImpl implements DemoDao{

	public String get(String name) {
		
		return "Hello sxx";
	}
	
}
