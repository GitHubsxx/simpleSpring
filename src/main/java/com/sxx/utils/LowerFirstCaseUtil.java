package main.java.com.sxx.utils;
/** 
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-28 ����10:59:39 
 * ˵�� :����ĸСд
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
