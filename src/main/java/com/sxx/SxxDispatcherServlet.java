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
 * @author ���� : sxx
 * @version ����ʱ�䣺2019-7-27 ����4:01:06 
 * ˵�� :��������ࡣ��ʼ��servlet����
 */
public class SxxDispatcherServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	//��web.xml�е�param-name��ֵһ��
	private static final String location = "contextConfigLocation";
	
	//�������е�������Ϣ
	private Properties ps = new Properties();
	
	//�������б�ɨ�赽���������
	private List<String> classNames = new ArrayList<String>();
	
	//IOC�������������г�ʼ����bean
	private Map<String,Object> ioc = new HashMap<String,Object>();
	
	//�������е�URL�ͷ�����ӳ���ϵ
	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	/**
	 * ���н׶�
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req,resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//6.�ȴ�����ƥ��URL����λ�������������ִ��
		//����doGet����doPost
		doDispach(req,resp);
	}

	/**
	 * ��ʼ���׶�
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//1.���������ļ�
		doLoadConfig(config.getInitParameter(location));
		
		//2.ɨ��������ص���
		doScanner(ps.getProperty("scanPackage"));
		
		//3.��ʼ��������ص��࣬���浽IOC������
		doInstance();
		
		//4.����ע��
		doAutowired();
		
		//5.����HandlerMapping
		initHandlerMapping();
		
		System.out.println("sxx mvcFramework is init....");
	}
	//6.����
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
		//��ȡ�����Ĳ����б�
		Class<?>[] paramTypes = method.getParameterTypes();
		//��ȡ����Ĳ���
		Map<String,String[]> paramMap = req.getParameterMap();
		//�������ֵ
		Object[] paramValues = new Object[paramTypes.length];
		
		for(int i= 0;i<paramTypes.length;i++){
			Class paramType = paramTypes[i];
			if(paramType == HttpServletRequest.class){
				//ת������
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
		
		//�������
		try {
			method.invoke(ioc.get(beanName), paramValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	//5.��ʼ��Mapping
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
			//��ȡContrioller��URL����
			if(clazz.isAnnotationPresent(SxxRequestMapping.class)){
				SxxRequestMapping mapping = clazz.getAnnotation(SxxRequestMapping.class);
				baseUrl = mapping.value();
			}
			//��ȡMethod��URL����
			Method[] methods = clazz.getMethods();
			for(Method method : methods){
				//����û�м�mappingע���
				if(!method.isAnnotationPresent(SxxRequestMapping.class)){
					continue;
				}
				//ӳ��URL
				SxxRequestMapping mp = method.getAnnotation(SxxRequestMapping.class);
				//String url = ("/"+baseUrl+"/"+mp.value().replaceAll("/+","/"));
				String url = (baseUrl+mp.value().replaceAll("/+","/"));
				handlerMapping.put(url, method);
			}
		}
		
	}

	//4.����ʼ����IOC�����е���,ע��
	private void doAutowired() {
		if(ioc.isEmpty()){
			return;
		}
		for(Entry<String,Object> entry : ioc.entrySet()){
			//�õ�ʵ�������е���������
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
				Field.setAccessible(fields, true);//����˽�����Եķ���Ȩ��
				try {
					filed.set(entry.getValue(),ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//3.��ʼ��������ص��࣬�����뵽IOC����֮�С�
	//IOC������keyĬ������������ĸСд��������Լ�����������������ʹ���Զ����
	private void doInstance() {
		if(classNames == null){
			return;
		}
		for(String className : classNames){
			try {
				Class<?> clazz =  Class.forName(className);
				if(clazz.isAnnotationPresent(SxxController.class)){
					//������ĸСд
					String name = LowerFirstCaseUtil.lowerFirstCase(clazz.getSimpleName());
					ioc.put(name, clazz.newInstance());
				
				}else if(clazz.isAnnotationPresent(SxxService.class)){
					SxxService service = clazz.getAnnotation(SxxService.class);
					String beanName = service.value();
					//����û����������֣������Լ���
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
	
	//2.ɨ�����е�.class �ļ������뼯����
	private void doScanner(String scanPackage) {
		//�����еİ�·��ת��Ϊ�ļ�·��
		System.out.println(scanPackage.replaceAll("\\.", "/"));
		URL url = this.getClass().getClassLoader()
				.getResource("/"+scanPackage.replaceAll("\\.", "/"));
		File file = new File(url.getFile());
		
		for(File f : file.listFiles()){
			//��������ļ��У���ݹ�
			if(f.isDirectory()){
				doScanner(scanPackage+"."+f.getName());
			}else{
				//ȫ����
				//classPath �²����� .class�ļ������� .xml ��.properties���ļ�
				String className = (scanPackage+"."+f.getName().replace(".class", "")); 
				classNames.add(className);
			}
			
		}
		
	}
	
	//1.���������ļ���properties����
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
