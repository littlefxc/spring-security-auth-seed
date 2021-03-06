# spring-security-auth-seed
spring security 技术栈 

期望要求：可重用的 企业级的 认证和授权模块

1. 企业级认证和授权
2. 包含 QQ 登录，微信登录常见第三方登录
3. 移动端认证授权
4. 浏览器端认证授权
5. RBAC
6. OAuth2
7. 项目示例
8. spring boot 1.x 和 spring boot 2.x
9. SSO
10. 希望能够封装起来重用，能给别人用
11. 能够支持集群环境，跨应用工作，SESSION攻击，控制用户权限，防护与用户认证相关的攻击
12. 支持多种前端渠道
13. 支持多种认证

## 企业级的认证和授权

![企业级的认证和授权.png](md/企业级的认证和授权.png)

## 代码结构介绍

- seed-security-core : 核心业务逻辑
- seed-security-browser : 浏览器安全特定代码
- seed-security-app : app 相关特定代码
- seed-security-examples : 样例程序

## 使用 Spring MVC 开发 Restful API

![Restful第一印象.png](md/Restful第一印象.png)

![restful成熟度模型.png](md/restful成熟度模型.png)

- 使用 Spring MVC 编写 Restful API
- 使用 Spring MVC 处理其它 web 应用常用的需求和场景
- Restful API 开发常用辅助框架
  - swagger：生成服务文档
  - wiremock：伪造服务
- [RESTful API 设计参考文献列表](https://github.com/aisuhua/restful-api-design-references)
- 还不错的[编程规范](https://xwjie.github.io/rule/)

## Hibernate Validator

![HibernateValidator1.png](md/HibernateValidator1.png)

![HibernateValidator2.png](md/HibernateValidator2.png)

## Restful API 错误处理 

### Spring Boot中默认的错误处理机制

错误处理类：`org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController`

![SpringBoot默认错误处理机制.png](md/SpringBoot默认错误处理机制.png)

#### 如何添加自定义的 404.html, 500.html

![自定义错误页面的位置.png](md/自定义错误页面的位置.png)

### 自定义异常处理

全局统一异常处理

## Restful API 的拦截

需求：记录所有 API 的处理时间

- 过滤器(Filter)

    自定义filter

    ```java
    package com.fengxuechao.seed.security.web.filter;
    
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Component;
    
    import javax.servlet.*;
    import java.io.IOException;
    
    /**
     * @author fengxuechao
     * @date 2019-08-01
     */
    @Slf4j
    public class TimeFilter implements Filter {
    
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            log.info("time filter init");
        }
    
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            log.info("time filter start");
            long start = System.currentTimeMillis();
            chain.doFilter(request,response);
            log.info("time filter:{}ms", System.currentTimeMillis() - start);
            log.info("time filter finish");
        }
    
        @Override
        public void destroy() {
            log.info("time filter destroy");
        }
    }
    ```
    
    如何添加第三方 filter 到过滤器链中去？
    
    ```java
    /**
     * @author fengxuechao
     * @date 2019-08-08
     */
    @Configuration
    public class WebConfig {
    
        /**
         * 第三方 filter 加载方式
         *
         * @return
         */
        @Bean
        public FilterRegistrationBean timeFilter() {
            FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    
            TimeFilter timeFilter = new TimeFilter();
            registrationBean.setFilter(timeFilter);
    
            List<String> urls = new ArrayList<>();
            urls.add("/*");
            registrationBean.setUrlPatterns(urls);
    
            return registrationBean;
        }
    }
    ```

- 拦截器(Interceptor)

    ```java
    /**
     * @author fengxuechao
     * @date 2019-08-01
     */
    @Slf4j
    @Component
    public class TimeInterceptor implements HandlerInterceptor {
    
        /**
         * 这个方法在控制器某个方法调用之前会被调用
         *
         * @param request
         * @param response
         * @param handler
         * @return
         * @throws Exception
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            log.info("preHandle");
    
    //        log.info("处理器类名 {}", ((HandlerMethod) handler).getBean().getClass().getName());
    //        log.info("方法名 {}", ((HandlerMethod) handler).getMethod().getName());
    
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }
    
        /**
         * 在控制器某个方法调用之后会被调用
         * 如果控制器方法调用过程中产生异常，这个方法不会被调用
         *
         * @param request
         * @param response
         * @param handler
         * @param modelAndView
         * @throws Exception
         */
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                               ModelAndView modelAndView) throws Exception {
            log.info("postHandle");
            Long start = (Long) request.getAttribute("startTime");
            log.info("time interceptor 耗时:" + (System.currentTimeMillis() - start));
    
        }
    
        /**
         * 不管控制器方法正常调用或者抛出异常，这个方法都会被调用
         *
         * @param request
         * @param response
         * @param handler
         * @param ex
         * @throws Exception
         */
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
                throws Exception {
            log.info("afterCompletion");
            Long start = (Long) request.getAttribute("startTime");
            log.info("time interceptor 耗时:" + (System.currentTimeMillis() - start));
            log.info("ex is " + ex);
    
        }
    
    }
  
    /**
     * @author fengxuechao
     * @date 2019-08-08
     */
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
    
        @Autowired
        private TimeInterceptor timeInterceptor;
    
        /**
         * 添加 Spring 拦截器
         *
         * @param registry
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(timeInterceptor);
        }
    }
    ```

- 切片(Aspect)

    ![SpringAop简介.png](md/SpringAop简介.png)
    
    ```java
    /**
     * @author fengxuechao
     * @date 2019-08-21
     */
    @Slf4j
    @Aspect
    @Component
    public class TimeAspect {
    
        /**
         * 使用@Arount 完全覆盖了 @Before，@After @AfterThrowing，所以一般使用 @Around
         *
         * @param pjp
         * @return
         * @throws Throwable
         */
        @Around("execution(* com.fengxuechao.seed.security.web.UserController.*(..))")
        public Object handleControllerMethod(ProceedingJoinPoint pjp) throws Throwable {
    
            log.info("time aspect start");
    
            Object[] args = pjp.getArgs();
            for (Object arg : args) {
                log.info("arg is " + arg);
            }
    
            long start = System.currentTimeMillis();
    
            // 执行被被切(被拦截)的方法
            Object object = pjp.proceed();
    
            log.info("time aspect 耗时:" + (System.currentTimeMillis() - start));
    
            log.info("time aspect end");
    
            return object;
        }
    
    }
    ```
    
过滤器，拦截器，切片的拦截顺序
    
![filter-interceptor-aspect拦截顺序.png](md/filter-interceptor-aspect拦截顺序.png)

## Restful 文件上传和下载

### 文件上传

```java
package com.fengxuechao.seed.security.web;

import com.fengxuechao.seed.security.dto.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 文件上传和下载
 *
 * @author fengxuechao
 * @date 2019-08-28
 */
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {

    private String folder = "/Users/fengxuechao/IdeaProjects/seed-security-auth/seed-security-examples/src/main/resources";

    /**
     * 文件上传
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping
    public FileInfo upload(MultipartFile file) throws IOException {

        log.info("参数名 {}", file.getName());
        log.info("文件原始名 {}", file.getOriginalFilename());
        log.info("文件大小 {}", file.getSize());

        File localFile = new File(folder, System.currentTimeMillis() + ".txt");

        file.transferTo(localFile);

        return new FileInfo(localFile.getAbsolutePath());
    }

    /**
     * 文件下载
     *
     * @param id
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("/{id}")
    public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try (InputStream inputStream = new FileInputStream(new File(folder, id + ".txt"));
             OutputStream outputStream = response.getOutputStream()) {

            response.setContentType("application/x-download");
            // 以指定的文件名下载文件
            response.addHeader("Content-Disposition", "attachment;filename=test.txt");
            // 将文件的输入流拷贝到输出流，将文件内容响应到输出流中去
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();
        }

    }
}
```

### 测试文件上传

```java
package com.fengxuechao.seed.security.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author fengxuechao
 * @date 2019-08-28
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FileControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void whenUploadSuccess() throws Exception {
//        mockMvc.perform(fileUpload("/file")
        mockMvc.perform(multipart("/file")
                .file(new MockMultipartFile(
                        "file", "test.txt",
                        "multipart/form-data", "hello upload".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
```

## 异步处理 REST 服务

场景1

![异步处理REST请求.png](md/异步处理REST请求.png)

场景2

![异步处理REST请求场景2.png](md/异步处理REST请求场景2.png)

### 使用 Runnable 异步处理 Rest 服务

### 使用 DeferredResult 异步处理 Rest 服务

### 异步处理配置

## 使用 Swagger 自动生成文档

### 使用 swagger 自动生成 html 文档

### 使用 WireMock 快速伪造 Restful 服务

[wiremock 官网](http://wiremock.org/)

## SpringSecurity 核心功能

- 认证（你是谁）
- 授权（你能干什么）
- 攻击防护（防止伪造身份）

### Spring Security 基本原理

最核心的基本原理：

![springsecurity基本原理.png](md/springsecurity基本原理.png)

ExceptionTranslationFilter 捕获 FilterSecurityInterceptor 抛出的异常做相应的处理

spring security提供的所有功能、特性都是建立在上图所展示的过滤器的基础上的

1. [核心Filter-FilterSecurityInterceptor](https://www.iteye.com/blog/fengyilin-2411449)
2. [核心Filter-ExceptionTranslationFilter](https://www.iteye.com/blog/fengyilin-2411524)
3. [核心Filter-SecurityContextPersistenceFilter](https://www.iteye.com/blog/fengyilin-2411839)
4. [核心Filter-UsernamePasswordAuthenticationFilter](https://www.iteye.com/blog/fengyilin-2412078)


### 用户名 + 密码认证

### 手机号 + 短信认证

### 自定义用户认证逻辑

- 处理用户信息获取逻辑
    
    `org.springframework.security.core.userdetails.UserDetailsService`

- 处理用户校验逻辑

    `org.springframework.security.core.userdetails.UserDetails`

- 处理密码加密解密

    `PasswordEncoder`

### 个性化用户认证流程

- 自定义登录页面 http.formLogin().loginPage("/seed-signIn.html")
- 自定义登录成功处理 AuthenticationSuccessHandler
- 自定义登录失败处理 AuthenticationFailureHandler

![系统配置封装.png](md/系统配置封装.png)

## 认证流程源码级详解

### 认证处理流程说明

[认证处理流程说明.md](docs/认证处理流程说明.md)

### 认证结果如何在多个请求之间共享

[认证结果如何在多个请求之间共享.md](docs/认证结果如何在多个请求之间共享.md)

### 获取用户信息

```java
@GetMapping("profile")
public Object userProfile(@AuthenticationPrincipal UserDetails userDetails) {
    return userDetails;
}

@GetMapping("profile")
public Object userProfile(Authentication authentication) {
    return authentication;
}
```

## 图形验证码功能

### 开发生成图形验证码接口

[图形验证码接口.md](docs/图形验证码接口.md)

### 在认证流程中加入图形验证码校验

[在认证流程中加入图形验证码校验](docs/在认证流程中加入图形验证码校验.md)

## 实现 "记住我" 功能

[实现记住我功能.md](docs/实现记住我功能.md)

## 短信验证码功能

[实现短信验证码登录](docs/实现短信验证码登录.md)

## 使用 Spring Social 开发第三方登录

[使用SpringSocial开发第三方登录.md](docs/使用SpringSocial开发第三方登录.md)

## 退出处理

[退出处理](docs/退出处理.md)

## Spring Security OAuth 开发 APP 认证框架

[Spring Security OAuth 开发 APP 认证框架](docs/SpringSecurityOAuth开发APP认证框架.md)

## Spring Security 控制授权

[SpringSecurity控制授权](docs/SpringSecurity控制授权.md)

## 总结

1. 引入依赖(pom.xml)

    ```
    <dependency>
        <groupId>com.fengxuechao.seed</groupId>
        <artifactId>seed-security-browser</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    ```

2. 配置系统(参见 seed-security-examples 的 application.properties)

3. 增加UserDetailsService接口实现

4. 如果需要记住我功能，需要创建数据库表(参见 db.sql)

5. 如果需要社交登录功能，需要以下额外的步骤

    1) 配置appId和appSecret
    2) 创建并配置用户注册页面，并实现注册服务(需要配置访问权限)，注意在服务中要调用ProviderSignInUtils的doPostSignUp方法。
    3) 添加SocialUserDetailsService接口实现
    4) 创建社交登录用的表 (参见 [db.sql](docs/db.sql))

[可扩展配置说明](docs/可扩展配置说明.md)





