package main.java.com.sxx.utils;
/** 
 * @author 作者 : sxx
 * @version 创建时间：2019-7-28 上午10:59:39 
 * 说明 :首字母小写
 */
public class LowerFirstCaseUtil {
	public static String lowerFirstCase(String str){
		if(str == null || str =="")
			return str;
		
		char[] chars = str.toCharArray();
		chars[0] += 32;
		
		return String.valueOf(chars);
		
	}
}
