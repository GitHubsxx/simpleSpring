package main.java.com.sxx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.internal.ws.wsdl.writer.document.ParamType;

import main.java.com.sxx.annotation.SxxAutowired;
import main.java.com.sxx.annotation.SxxController;
import main.java.com.sxx.annotation.SxxRequestMapping;
import main.java.com.sxx.annotation.SxxService;
import main.java.com.sxx.utils.LowerFirstCaseUtil;


/** 
 * @author 作者 : sxx
 * @version 创建时间：2019-7-27 下午4:01:06 
 * 说明 :程序入口类。初始化servlet容器
 */
public class SxxDispatcherServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	//跟web.xml中的param-name的值一致
	private static final String location = "contextConfigLocation";
	
	//保存所有的配置信息
	private Properties ps = new Properties();
	
	//保存所有被扫描到的相关类名
	private List<String> classNames = new ArrayList<String>();
	
	//IOC容器，保存所有初始化的bean
	private Map<String,Object> ioc = new HashMap<String,Object>();
	
	//保存所有的URL和方法的映射关系
	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	/**
	 * 运行阶段
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req,resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//6.等待请求，匹配URL，定位方法，反射调用执行
		//调用doGet或者doPost
		doDispach(req,resp);
	}

	/**
	 * 初始化阶段
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//1.加载配置文件
		doLoadConfig(config.getInitParameter(location));
		
		//2.扫描所有相关的类
		doScanner(ps.getProperty("scanPackage"));
		
		//3.初始化所有相关的类，保存到IOC容器中
		doInstance();
		
		//4.依赖注入
		doAutowired();
		
		//5.构造HandlerMapping
		initHandlerMapping();
		
		System.out.println("sxx mvcFramework is init....");
	}
	//6.运行
	private void doDispach(HttpServletRequest req, HttpServletResponse resp) {
		if(this.handlerMapping.isEmpty()){
			return;
		}
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "")
				.replaceAll("/+", "/");
		if(!this.handlerMapping.containsKey(url)){
			try {
				resp.getWriter().write("404 Not Found");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Map<String,String[]> params = req.getParameterMap();
		Method method = handlerMapping.get(url);
		//获取方法的参数列表
		Class<?>[] paramTypes = method.getParameterTypes();
		//获取请求的参数
		Map<String,String[]> paramMap = req.getParameterMap();
		//保存参数值
		Object[] paramValues = new Object[paramTypes.length];
		
		for(int i= 0;i<paramTypes.length;i++){
			Class paramType = paramTypes[i];
			if(paramType == HttpServletRequest.class){
				//转换类型
				paramValues[i] = req;
				continue;
			}else if(paramType == HttpServletResponse.class){
				paramValues[i] = resp;
				continue;
			}else if(paramType == String.class){
				for(Entry<String,String[]> param : paramMap.entrySet()){
					String value = Arrays.toString(param.getValue())
							.replaceAll("\\[|\\]", "")
							.replaceAll("\\s", ",");
					paramValues[i] = value;
				}
			}
			
		}
		String beanName = LowerFirstCaseUtil.lowerFirstCase(
				method.getDeclaringClass().getSimpleName());
		
		//反射调用
		try {
			method.invoke(ioc.get(beanName), paramValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	//5.初始化Mapping
	private void initHandlerMapping() {
		if(ioc.isEmpty()){
			return;
		}
		for(Entry<String,Object> entry : ioc.entrySet()){
			Class<?> clazz = entry.getValue().getClass();
			if(!clazz.isAnnotationPresent(SxxController.class)){
				continue;
			}
			String baseUrl = "";
			//获取Contrioller的URL配置
			if(clazz.isAnnotationPresent(SxxRequestMapping.class)){
				SxxRequestMapping mapping = clazz.getAnnotation(SxxRequestMapping.class);
				baseUrl = mapping.value();
			}
			//获取Method的URL配置
			Method[] methods = clazz.getMethods();
			for(Method method : methods){
				//忽略没有加mapping注解的
				if(!method.isAnnotationPresent(SxxRequestMapping.class)){
					continue;
				}
				//映射URL
				SxxRequestMapping mp = method.getAnnotation(SxxRequestMapping.class);
				//String url = ("/"+baseUrl+"/"+mp.value().replaceAll("/+","/"));
				String url = (baseUrl+mp.value().replaceAll("/+","/"));
				handlerMapping.put(url, method);
			}
		}
		
	}

	//4.将初始化到IOC容器中的类,注入
	private void doAutowired() {
		if(ioc.isEmpty()){
			return;
		}
		for(Entry<String,Object> entry : ioc.entrySet()){
			//拿到实例对象中的所有属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field filed : fields){
				if(!filed.isAnnotationPresent(SxxAutowired.class)){
					continue;
				}
				SxxAutowired aw = filed.getAnnotation(SxxAutowired.class);
				String beanName = aw.value().trim();
				if("".equals(beanName)){
					beanName = filed.getType().getName();
				}
				Field.setAccessible(fields, true);//设置私有属性的访问权限
				try {
					filed.set(entry.getValue(),ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//3.初始化所有相关的类，并放入到IOC容器之中。
	//IOC容器的key默认是类名首字母小写，如果是自己设置类名，则优先使用自定义的
	private void doInstance() {
		if(classNames == null){
			return;
		}
		for(String className : classNames){
			try {
				Class<?> clazz =  Class.forName(className);
				if(clazz.isAnnotationPresent(SxxController.class)){
					//将首字母小写
					String name = LowerFirstCaseUtil.lowerFirstCase(clazz.getSimpleName());
					ioc.put(name, clazz.newInstance());
				
				}else if(clazz.isAnnotationPresent(SxxService.class)){
					SxxService service = clazz.getAnnotation(SxxService.class);
					String beanName = service.value();
					//如果用户设置了名字，就用自己的
					if(!"".equals(beanName.trim())){
						ioc.put(beanName, clazz.newInstance());
					}
					Class<?>[] interfaces = clazz.getInterfaces();
					for(Class<?> c : interfaces){
						ioc.put(c.getName(),clazz.newInstance());
					}
				}else{
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//2.扫描所有的.class 文件。加入集合中
	private void doScanner(String scanPackage) {
		//将所有的包路径转换为文件路径
		System.out.println(scanPackage.replaceAll("\\.", "/"));
		URL url = this.getClass().getClassLoader()
				.getResource("/"+scanPackage.replaceAll("\\.", "/"));
		File file = new File(url.getFile());
		
		for(File f : file.listFiles()){
			//如果有子文件夹，则递归
			if(f.isDirectory()){
				doScanner(scanPackage+"."+f.getName());
			}else{
				//全类名
				//classPath 下不仅有 .class文件，还有 .xml ，.properties等文件
				String className = (scanPackage+"."+f.getName().replace(".class", "")); 
				classNames.add(className);
			}
			
		}
		
	}
	
	//1.加载配置文件到properties对象
	private void doLoadConfig(String location) {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(location);
		try {
			ps.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
