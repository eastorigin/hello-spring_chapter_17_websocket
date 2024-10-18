package com.ktdsuniversity.edu.hello_spring.common.beans;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.ktdsuniversity.edu.hello_spring.access.dao.AccessLogDao;

// application.yml에서 설정하지 못하는 디테일한 설정을 위한 annotation
// String Bean을 수동으로 생성하는 기능
@Configuration
// Spring WebMVC에 필요한 다양한 요소를 활성화시키는 annotation
// 	- Spring Validator
//	- Spring Inteceptor
//	- ...
@EnableWebMvc
@EnableWebSocket // 웹소켓을 활성화시킴
public class WebConfig
			implements WebMvcConfigurer // WebMVC를 사용하기 위한 설정 인터페이스
					 , WebSocketConfigurer { // WebSocket을 사용하기 위한 설정 인터페이스
	
	@Autowired
	private TextWebSocketHandler textWebSocketHandler;
	
	@Autowired
	private AccessLogDao accessLogDao;
	
	@Value("${app.interceptors.check-session.path-patterns}")
	private List<String> checkSessionPathPatterns;
	@Value("${app.interceptors.check-session.exclude-path-patterns}")
	private List<String> checkSessionExcludePathPatterns;
	
	@Value("${app.interceptors.check-dup-login.path-patterns}")
	private List<String> checkDupLoginPathPatterns;
	@Value("${app.interceptors.check-dup-login.exclude-path-patterns}")
	private List<String> checkDupLoginExcludePathPatterns;
	
	@Value("${app.interceptors.add-access-log.path-patterns}")
	private List<String> addAccessLogPathPatterns;
	@Value("${app.interceptors.add-access-log.exclude-path-patterns}")
	private List<String> addAccessLogExcludePathPatterns;

	/**
	 * Auto DI: @Component
	 * Manual DI: @Bean
	 * -> 객체 생성을 스프링이 아닌 개발자가 직접 하는 것
	 * @return
	 */
	@Bean
	Sha createShaInstance() {
		Sha sha = new Sha();
		return sha;
	}
	
	/**
	 * JSP View Resolver 설정
	 */
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/views/",".jsp");
	}
	
	/**
	 * Static Resource 설정
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**") // http://localhost:8080/css/common/common.css - css 밑의 모든 경로
				.addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/js/**") // http://localhost:8080/js/jquery/jquery-3.1.7.min.js
				.addResourceLocations("classpath:/static/js/");
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		List<String> excludeCheckSessionInterceptorsURL = new ArrayList<>();
		excludeCheckSessionInterceptorsURL.add("/js/**");
		excludeCheckSessionInterceptorsURL.add("/css/**");
		excludeCheckSessionInterceptorsURL.add("/image/**");
		excludeCheckSessionInterceptorsURL.add("/member/login");
		excludeCheckSessionInterceptorsURL.add("/member/regist/**");
		
		// First Interceptor
		registry.addInterceptor(new CheckSessionInterceptor())
				.addPathPatterns(this.checkSessionPathPatterns)
				.excludePathPatterns(this.checkSessionExcludePathPatterns);
		
		// Second Interceptor
		registry.addInterceptor(new CheckDuplicateLoginInterceptor())
				.addPathPatterns(this.checkDupLoginPathPatterns)
				.excludePathPatterns(this.checkDupLoginExcludePathPatterns);
		
		// Third Interceptor
		registry.addInterceptor(new AddAccessLogHistoryInterceptor(this.accessLogDao))
				.addPathPatterns(this.addAccessLogPathPatterns)
				.excludePathPatterns(this.addAccessLogExcludePathPatterns);
	}
	
	/**
	 * 웹소켓의 엔드포인트(URL)를 설정
	 * 해당 엔드포인트(URL)를 담당할 Class가 필요
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this.textWebSocketHandler, "/ws")
				.setAllowedOrigins("http://localhost:8080") // 모든 도메인(URL)에서 /ws로 접근할 수 있도록 설정
				.withSockJS() // /ws URL에 접근할 수 있는 JS라이브러리를 sock.js로 제한
				;
	}
}
