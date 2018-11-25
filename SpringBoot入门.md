****

### springMVC基本知识

1. spring在ssm框架中的作用是一个bean工厂，用来管理bean的生命周期
2. spring的核心：IOC/DI（控制反转/依赖注入）：把dao依赖注入到service层，把service层依赖注入到controller层，把controller层反转给action层，spring顶层容器为BeanFactory
3. IOC的作用：解决对象之间的依赖问题，把所有对象的依赖关系通过配置文件或注解的方式关联起来，降低了耦合度
4. spring主要用到的设计模式：1）工厂模式：每个bean通过方法创建；2）单例模式：默认每个bean堆地作用域都是 singleton；3）代理模式：aop通过动态代理实现
5. springMVC中的controller是单例模式，在多线程访问时有线程安全问题，因此：为保证效率不要用同步，在控制中不要写属性
6. @RequestMapping用来映射一个url到一个特定的处理方法上，请求参数传递到方法形参中（例如一个对象，例如@PathVariable），建议单例（struts2则是一个url对应一个类，传递参数到类的属性，因此只能设置为多例）
7. springMVC通过参数解析器将request请求内容解析，给方法形参赋值，将处理之后的数据和视图封装为ModelAndView，其中的模型数据通过request域传输到页面
8. springMVC中的转发和重定向，转发：return "hello"；重定向：return "redirect:/hello"
9. springMVC的步骤：用户请求先到DispatcherServlet，在HandlerMapping中查询请求对应的控制器，返回控制器对象，在HandlerAdapter中执行控制器的处理逻辑，返回ModelAndView，再到ViewResolver中解析视图，返回View对象，渲染视图后响应用户



### **SpringBoot自动配置**

@**SpringBootApplication** 组合注解：

```java 
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan( excludeFilters = {
    @Filter( type = FilterType.CUSTOM, classes = TypeExcludeFilter.class ), 
    @Filter( type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication 
```

@**SpringBootConfiguration**： springBoot的配置类

@**EnableAutoConfiguration**： 开启自动配置功能

```java
@AutoConfigurationPackage
@Import(EnableAutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration 
```

@**AutoConfigurationPackage**：将主配置类（@SpringBootApplication标注的类）所在包及其下面所有子包的所有组件扫描到spring容器中

@**Import(EnableAutoConfigurationImportSelector.class)**：给容器中导入组建自动配置的选择器，在EnableAutoConfigurationImportSelector.java中的selectImports()方法中，将所有需要导入的组件以全类名的方式返回，这些组件会被添加到容器中

**这些全类名如何获取到的呢？** SpringBoot在启动时从spring-boot-autoconfigure.jar包路径下的META-INF/spring.factories中获取EnableAutoConfiguration指定的值，将这些值作为自动配置类导入到容器中

**以HttpEncodingAutoConfiguration为例**：

```java
import org.springframework.boot.autoconfigure.web.HttpEncodingProperties.Type;

@Configuration //表示是一个java配置类，可以给容器中添加组件
@EnableConfigurationProperties({HttpEncodingProperties.class})  
//启动指定类的Configurationproperties功能；将配置文件中的值和HttpEncodingProperties类中的属性绑定起来；并把HttpEncodingProperties这个组件加入到ioc容器中
@ConditionalOnWebApplication
//根据不同的条件，进行判断，如果满足条件，当前配置类里面的配置就会生效；这里是判断是否为web应用；
@ConditionalOnClass({CharacterEncodingFilter.class})
//判断当前项目有没有这个类；springMVC中解决乱码的过滤器
@ConditionalOnProperty(
    prefix = "spring.http.encoding",
    value = {"enabled"},
    matchIfMissing = true
)
//判断配置文件是否配置了spring.http.encoding.enabled这个属性，matchIfMissing = true表示如果不存在这个配置，也默认让spring.http.encoding.enabled=true生效

public class HttpEncodingAutoConfiguration {
    
    //已经和springBoot的配置文件映射了
    private final HttpEncodingProperties properties;
    
	//只有一个有参构造器情况下，参数的值就会从容器中拿
    public HttpEncodingAutoConfiguration(HttpEncodingProperties properties) {
        this.properties = properties;
    }

    @Bean      //给容器添加组件CharacterEncodingFilter，这个组件的值需要从properties属性中获取
    @ConditionalOnMissingBean     //容器中不存在这个Bean，才添加该组件
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new OrderedCharacterEncodingFilter();
        filter.setEncoding(this.properties.getCharset().name());
        filter.setForceRequestEncoding(this.properties.shouldForce(Type.REQUEST));
        filter.setForceResponseEncoding(this.properties.shouldForce(Type.RESPONSE));
        return filter;
    }
}
```

所有在配置文件中能配置的属性都是在xxxProperties类中封装着；配置文件能配置什么就可以参照这个xxxPropeerties类 

```java
@ConfigurationProperties(prefix = "spring.http.encoding")
//从配置文件中的值进行绑定和bean属性进行绑定
public class HttpEncodingProperties {
}
```

配置生效是指：这个配置类会给ioc容器添加组件，组件的属性是从对应的properties中获取的

@**Conditional** : 必须是@Conditional指定的条件成立，才给容器中添加组件，或者配置类里面的所有内容才生效； （例如@ConditionalOnBean、@ConditionalOnMissingBean、@ConditionalOnClass、@ConditionalOnProperty、@ConditionalOnResource）



### **配置文件**

将配置文件（application.yml 或 application.properties）中配置的每一个属性值，映射到组件中

@**ConfigurationProperties**：告诉springBoot将本类中的所有属性和配置文件中相关配置进行绑定

prefix = " " 对配置文件中哪一个下面的属性进行映射

```
@Component
@ConfigurationProperties(prefix = "person")
```

@**Value**：将类中的某个属性与配置文件中的指定值进行绑定

|                                  | @ConfigurationProperties | @Value         |
| -------------------------------- | ------------------------ | -------------- |
| 功能                             | 批量注入配置文件中的属性 | 需要一个个指定 |
| 属性命名松散绑定                 | 支持                     | 不支持         |
| SpEL语法绑定(${}、#{}）          | 不支持                   | 支持           |
| JSR303数据校验( @Email、@Past等) | 支持，校验不通过会报错   | 不支持         |
| 复杂类型封装（例如map、对象）    | 支持                     | 不支持         |

如果只是在某项业务逻辑中要获取配置文件中的某个值，使用@Value

如果专门编写一个pojo（javaBean）和配置文件进行映射，使用@ConfigurationProperties

@**PropertySource**：加载指定的配置文件

```
@PropertySource(value = {"classpath:person.properties"})
```

配置文件中的**占位符**（随机数）

```properties
person.age=${random.int[10,60]}
person.lastname=ZHANG_${random.int}
```

**多Profile配置文件** ：application-dev.propertities、application-prod.propertities

默认使用application.propertities的配置

yml配置支持多文档块方式 用“---”分割

激活指定的profile：1、在配置文件中指定 spring.profiles.active=dev；（激活开发环境）

​                                   2、在VM options中添加虚拟机参数 -Dspring.profiles.active=dev

**配置文件加载位置** ：SpringBoot启动扫描以下位置的application.properties或者application.yml文件作为Spring boot的默认配置文件

- file:./config/
- file./
- classpath:/config/
- classpath:/

优先级从高到低顺序，高优先级会覆盖低优先级的相同配置；互补配置

还可以通过spring.config.location来改变配置文件的位置

项目打包好了以后，可以使用命令行参数的形式，启动项目的时候来指定配置文件的新位置；指定配置文件和默认的配置文件会共同起作用，互补配置

```
java -jar XXX.jar  --spring.config.location=E:/work/application.properties
```

运维比较有用，从外部加载，不用修改别的文件

### springBoot配置精髓

结合上面HttpEncodingAutoConfiguration的例子总结如下：

1）、SpringBoot启动会加载大量的自动配置类（一般命名为xxxAutoConfiguration）

2）、我们看我们需要的功能有没有SpringBoot默认写好的自动配置类；

3）、如果有的话，再看这个自动配置类中配置了哪些组件；（只要我们要用的组件都有，我们就不需要再来配置）

4）、给容器中自动配置类添加组件的时候，会从properties类（一般命名为xxxProperties）中获取属性，因此，如果对组件的某些属性不满意，我们可以在配置文件中指定这些属性的值

5）、如何知道哪些自动配置类生效：通过在application.properties中启用**debug=true**属性，在控制台打印自动配置报告



### 日志

spring框架默认选择**JCL**作为日志门面

springBoot框架默认选择**slf4j**作为日志门面，选择**logback**作为日志实现

使用log4j需要导入log4j与slf4j的适配层，slf4j-log412.jar

**如何让系统中所有日志统一到slf4j** : （比如系统中有spring框架的代码用到了commons-logging，Hibernate框架用到了Jboss）首先将其他日志框架排除出去，再用中间包来替换原有的日志框架，例如jcl-over-slf4j.jar、log4j-over-slf4j.jar、jul-to-slf4j.jar

springBoot能自动适配所有日志，我们在引入其他框架的时候，只需要把这个框架的日志框架排除掉，如下

```xml
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```



### web开发

#### **使用流程** 

1)、创建SpringBoot应用，选中我们需要的模块；

2)、SpringBoot已经默认将这些场景配置好了，只需要在配置文件中指定少量配置就可以运行起来

3)、自己编写业务代码

```
xxxAutoConfiguration:帮我们给容器中自动配置组件
xxxProperties:配置类，用来封装配置文件的内容
```

#### **静态资源文件映射规则** 

```java
@ConfigurationProperties(prefix = "spring.resources", ignoreUnknownFields = false)
public class ResourceProperties implements ResourceLoaderAware, InitializingBean {
    //可以设置和静态资源相关的参数
```
1. **webjars**

以jar包的方式引入静态资源 

所有的/webjars/**，都去classpath:/META-INF/resources/webjars/ 路径下找资源

在maven中引入webjar

```xml
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>jquery</artifactId>
            <version>3.3.1</version>
        </dependency>
```

访问时，localhost:8080/webjars/jquery/3.3.1/jquery.js 

2. **本地资源**

默认在以下几个文件夹中去寻找本地静态资源

```
"classpath:/META-INF/resources/", 
"classpath:/resources/",
"classpath:/static/", 
"classpath:/public/",
"/";   当前项目的根路径
```

访问localhost:8080/abc.html  ==>  去上述静态资源文件夹中找abc.html 

index页面欢迎页，静态资源文件夹下所有的index.html页面；被''/**''映射； 

访问localhost:8080/      ==>     index页面 

可以在配置文件中**配置静态资源文件夹**

```properties
spring.resources.static-locations = classpath:/hello/, classpath:/xxx/
```

如果在配置文件中增加

```properties
server.context-path = /demo01             #访问时增加项目名
```

则访问主页时，需要 localhost:8080/demo01/

#### 如何修改springMVC的默认配置

一、springBoot在自动配置很多MVC组件的时候，会先看用户有没有自己配置的组件（@Bean、@Component），如果有就用用户配置的，如果没有才自动配置；如果有些组件可以有多个（比如ViewResolver），就将用户配置的和默认配置的组合起来；

二、**扩展MVC配置**：如果想要保持springBoot MVC的配置，只添加额外的MVC配置（比如interceptor拦截器、formatteer格式转换器、view controller等），可以编写一个配置类（**@Configuration**），同时要继承**WebMvcConfigurerAdapter**类型，但不能标注**@EnableWebMvc**

```java
//使用WebMvcConfigurerAdapter来扩展springMVC功能
@Configuration
public class MyMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //浏览器发送hello请求，也能来到success页面
        registry.addViewController("/hello").setViewName("success");
    }
}
```

**扩展配置的原理**（源码分析）：

1. WebMvcAutoConfiguration是springMVC的自动配置类
2. 在做扩展配置时，@Import(EnableWebMvcConfiguration.class)，即

```java
@Configuration
@Import(EnableWebMvcConfiguration.class)
@EnableConfigurationProperties({WebMvcProperties.class, ResourceProperties.class})
public static class WebMvcAutoConfigurationAdapter extends WebMvcConfigurerAdapter {
```

```java
@Configuration
public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {
```

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
    private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();

    //从容器中获取所有webMvcConfigurer，赋值到this.configueres中
    @Autowired(required = false)
    public void setConfigurers(List<WebMvcConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addWebMvcConfigurers(configurers);
        }
    }
    
    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        this.configurers.addViewControllers(registry);
        
        //方法的一个参考实现，将所有的webMvcCconfigurer相关配置一起调用（包括自己的配置类）
//            	@Override
//                public void addViewControllers(ViewControllerRegistry registry) {
//                    for (WebMvcConfigurer delegate : this.delegates) {
//		                  delegate.addViewControllers(registry);
//                    }
//                }
    }
```

3. 容器中所有的webMvcConfigurer都会起作用，用户自己的配置也会被调用

三、**全面接管springMVC**：如果完全不需要springMVC的自动配置，所有都是用户自己配置，我们需要编写一个配置类（@Configuration），并且标注**@EnableWebMvc**

```java
//全面接管，所有的springMVC相关自动配置都失效了
@EnableWebMvc
@Configuration
public class MyMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //浏览器发送hello请求，也来到success页面
        registry.addViewController("/hello").setViewName("success");
    }
}
```

不推荐全面接管，除非web场景功能简单，且为了节省内存空间

**全面接管的原理**：

```java
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}

@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
//注解@EnableWebMvc将WebMvcConfigurationSupport这个组建导入进来
//导入的WebMvcConfigurationSupport只是SpringMVC最基本的功能
```

```java
@Configuration
//容器中没有这个组件的时候，这个自动配置类才生效
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
public class WebMvcAutoConfiguration {
```



### 实践经验

1）、spring4和spring5在编写mvc配置类时有不同

```java
//Spring4
@Configuration
public class MyMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index.html").setViewName("login");
    }
}
```

```java
//Spring5中WebMvcConfigurerAdapter不推荐使用
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index.html").setViewName("login");
    }
}
```

2）、编写controller组件时，若要返回页面响应，要标注@Controller，不能使用@RestController

因为它集成了@Responsebody和@Controller，只能返回json数据，或者配合视图解析器

3）、springBootApplication要保证扫描到配置文件（@Configuration）或组件（@Controller）所在的包

4）、thymeleaf和thymeleaf-spring5这两个jar包文件夹同时存在时会发生报错，提示有版本冲突的org.thymeleaf.spring5.SpringTemplateEngine

```
NoSuchMethodError:
org.thymeleaf.spring5.SpringTemplateEngine.setRenderHiddenMarkersBeforeCheckboxes
(报错行：ThymeleafAutoConfiguration.java:158)
```

原因是ThymeleafAutoConfiguration为java8版本，与thymeleaf3.0.9版本不兼容，改为thymeleaf最新版即可

5）、页面出现中文乱码

要在IDEA项目settings -> file encoding中设置default encoding for property files为UTF-8，并勾选自动转为ASCII码，然后再用中文编写properties文件

6）、@Autowired会在初始化代码块执行之后才会执行，因为要创建完对象并初始化之后，@Autowired才会知道往哪里注入，因此，在初始化代码块或者构造方法中，不可以出现@Autowired待注入的对象。



#### 模板引擎

将原本分离的html页面和业务数据进行绑定，输出组装好的新文件，SpringBoot推荐使用Thymeleaf

1. **引入thymeleaf3**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

默认导入thymeleaf2，版本太低，修正如下

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <java.version>1.8</java.version>
    <thymeleaf.version>3.0.9.RELEASE</thymeleaf.version>
    <!--布局功能支持 同时支持thymeleaf3主程序 layout2.0以上版本  -->
    <thymeleaf-layout-dialect.version>2.2.2</thymeleaf-layout-dialect.version>
</properties>
```

2. **thymeleaf使用和语法**

```java
@ConfigurationProperties(prefix = "spring.thymeleaf")
public class ThymeleafProperties {

   private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
   private static final MimeType DEFAULT_CONTENT_TYPE = MimeType.valueOf("text/html");

   public static final String DEFAULT_PREFIX = "classpath:/templates/";
   public static final String DEFAULT_SUFFIX = ".html";
   //只要把HTML文件方法类路径下的template文件夹下，就会自动导入
```

因此，要把HTML页面放到**classpath:/templates/** 路径下，才能使用thymeleaf模板引擎

```html
<html lang="en"  xmlns:th="http://www.thymeleaf.org">   <!--导入thymeleaf的名称空间-->
</html>
```

```html
thymeleaf表达式：
${...}     //获取对象属性、调用方法
*{...}     //和${}功能一样，补充功能，配合th:object使用，例如
  	    <div th:object="${session.user}">
            <p>Name: <span th:text="*{firstName}">Sebastian</span>.</p>
            <p>Surname: <span th:text="*{lastName}">Pepper</span>.</p>
            <p>Nationality: <span th:text="*{nationality}">Saturn</span>.</p>
		</div>
#{...}     //获取国际化内容
@{...}     //定义URL链接
~{...}     //插入片段文档
```

3. **thyme公共片段抽取**

```html
1、抽取公共片段
<!--footer.html-->
<div id="footid" th:fragment="copy">xxx</div>
2、引入公共片段
<!--test.html-->
<body>
   <div th:insert=~{footer::copy}></div> 
</body>
~{templatename::selector} 模板名::选择器  footer::#footid
~{templatename::fragmentname} 模板名::片段名称 footer::copy
```

三种引用方式

**th:insert** : 将公共片段插入到指定元素中

**th:replace** : 直接替换为公共片段

**th:include**：只替换公共片段中的内容



#### 错误处理机制

**ErrorMvcAutoConfiguration** 错误处理的自动配置，自动添加以下组件（@ConditionalOnMissingBean）：

**ErrorPageCustomizer** 定制错误的响应规则

```java
@Value("${error.path:/error}")
private String path = "/error";   //系统出现错误以后来到error请求进行处理，(类似于web.xml中的配置)
```

**BasicErrorController** 处理默认的/error请求

```java
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController {
    //产生HTML数据，处理浏览器的请求
    @RequestMapping(produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = this.getStatus(request);
        Map<String, Object> model = Collections.unmodifiableMap(this.getErrorAttributes(request, this.isIncludeStackTrace(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        ModelAndView modelAndView = this.resolveErrorView(request, response, status, model);
        return modelAndView != null ? modelAndView : new ModelAndView("error", model);
    }
	//产生Json数据，处理其他客户端的请求
	@RequestMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = this.getErrorAttributes(request, this.isIncludeStackTrace(request, MediaType.ALL));
        HttpStatus status = this.getStatus(request);
        return new ResponseEntity(body, status);
    }
```

**DefaultErrorViewResolver** 错误页面的视图解析器

```java
@Override
public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status,
      Map<String, Object> model) {
   ModelAndView modelAndView = resolve(String.valueOf(status), model);
   if (modelAndView == null && SERIES_VIEWS.containsKey(status.series())) {
      modelAndView = resolve(SERIES_VIEWS.get(status.series()), model);
   }
   return modelAndView;
}

private ModelAndView resolve(String viewName, Map<String, Object> model) {
    //默认SpringBoot可以找到一个页面: error/状态码
   String errorViewName = "error/" + viewName;
    //如果模板引擎可以解析地址，就返回模板引擎解析
   TemplateAvailabilityProvider provider = this.templateAvailabilityProviders
         .getProvider(errorViewName, this.applicationContext);
   if (provider != null) {
       //有模板引擎就返回到errorViewName指定的视图地址
      return new ModelAndView(errorViewName, model);
   }
    //自己的文件 就在静态文件夹下找静态文件 /静态资源文件夹/404.html
   return resolveResource(errorViewName, model);
}
```

**DefaultErrorAttributes** 传递什么错误信息到页面上

```java
@Override
public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
      boolean includeStackTrace) {
   Map<String, Object> errorAttributes = new LinkedHashMap<String, Object>();
   errorAttributes.put("timestamp", new Date());
   addStatus(errorAttributes, requestAttributes);
   addErrorDetails(errorAttributes, requestAttributes, includeStackTrace);
   addPath(errorAttributes, requestAttributes);
   return errorAttributes;
}
```

一旦系统出现4xx或者5xx错误 ErrorPageCustomizer就会生效，来到/error请求，交给BasicErrorController处理，controller中的model由DefaultErrorAttributes提供错误信息，controller中的view由DefaultErrorViewResolver进行解析 



#### 嵌入式Servlet容器

SpringBoot默认使用Tomcat作为嵌入式的Servlet容器 

**一、如何定制和修改Servlet容器的相关配置？**

1）、在配置文件中修改（对应配置类为ServerProperties）

```properties
server.port = 8082
server.context-path = /crud
server.tomcat.uri-encoding = UTF-8
server.tomcat.XXX
```

2）、编写一个EmbeddedServletContainerCustomizer （嵌入式的Servlet容器的定制器 ）

```java
@Bean    //加入容器中
public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer(){
    return new EmbeddedServletContainerCustomizer() {
        //定制嵌入式Servlet容器的相关规则
        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {
            container.setPort(8082);
        }
    };
}
```

两种方法其实同理

springBoot中的xxxCustomizer帮助我们进行定制配置

**二、注册Servlet三大组件（Servlet、Filter、Listener）**

由于SprringBoot默认是以jar包形式启动嵌入式的Servlet容器来启动SpringBoot的web应用，没有web.xml，因此注册三大组件采用如下方式

1. **ServletRegistrationBean** 

```java
@Bean
public ServletRegistrationBean myServlet(){
    return new ServletRegistrationBean(new MyServlet(), "/servlet");   
}
```

MyServlet.java

```java
public class MyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("Hello Servlet");
    }
}
```

2. **FilterRegistrationBean**

```java
@Bean
public FilterRegistrationBean myFilter(){
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
    filterRegistrationBean.setFilter(new MyFilter());
    filterRegistrationBean.setUrlPatterns(Arrays.asList("/hello","/myServlet"));
    return filterRegistrationBean;
}
```

3. **ServletListenerRegistrationBean**

```java
@Bean
public ServletListenerRegistrationBean myListener(){
    return new ServletListenerRegistrationBean<>(new MyListener());
}
```

SpringBoot帮我们自动配置SpringMVC的时候，自动注册SpringMVC的前端控制器 DispatcherServlet（在DispatcherServletAutoConfiguration.java中，有注册DispatcherServletRegistrationBean）

**三、嵌入式Servlet自动配置的步骤**：

1）、SpringBoot根据导入的依赖情况，给容器中添加相应的容器工厂  例如tomcat、Jetty

**EmbeddedServletContainerFactory**【TomcatEmbeddedServletContainerFactory】

2）、创建容器工厂对象，就要惊动后置处理器EmbeddedServletContainerCustomizerBeanPostProcessor

只要是嵌入式Servlet容器工厂，后置处理器就工作；

3）、后置处理器，从容器中获取的所有的**EmbeddedServletContainerCustomizer**，调用定制器的定制方法

**四、嵌入式Servlet启动的步骤**

什么时候创建嵌入式的Servlet的容器工厂？什么时候获取嵌入式的Servlet容器并启动Tomcat？

1）、SpringBoot应用启动Run方法

2）、创建IOC容器对象，并初始化容器，创建容器的每一个组件

如果是web应用则创建AnnotationConfigEmbeddedWebApplicationContext，否则创建默认的IOC容器AnnotationConfigApplicationContext 

3）、refresh(context)   刷新创建好的IOC容器，其中的onRefresh()方法被web的IOC容器重写了

4）、**onRefresh()**方法中调用了createEmbeddedServletContainer方法，即webioc会创建嵌入式的Servlet容器  

5）、首先要**获取嵌入式的Servlet容器工厂**，getEmbeddedServletContainerFactory()

创建一个TomcatEmbeddedServletContainerFactory对象，后置处理器看到这个对象，就来获取所有的定制器Customizer来定制Servlet容器的相关配置 

6）、使用容器工厂**获取嵌入式的Servlet容器**  getEmbeddedServletContainer()

创建一个嵌入式Servlet容器对象，并启动Servlet容器（Tomcat） 

7）、onRefresh()方法执行完毕，接下来将ioc容器中其他组件也创建出来



### Docker

Docker是一个开源的应用容器引擎

将软件编译成一个镜像，然后在镜像里对各种软件做好配置，将镜像发布出去，其他的使用者就可以直接使用这个镜像。运行中的这个镜像叫做容器，容器启动速度快，安装好之后什么都有了。

docker主机（Host）：安装了Docker程序的机器（Docker直接安装在操作系统上）

docker客户端（Client）：连接docker主机进行操作

docker仓库（Registry）：用来保存各种打包好的软件镜像（公共仓库、私有仓库）

docker镜像（Image）：软件打好包的镜像，放到docker的仓库中，例如MySQL镜像、Nginx镜像等 

docker容器（Container）：镜像启动后的实例，是独立运行的一个或一组应用（例如启动5个tomcat镜像，就有5个tomcat容器）

**docker的步骤** ：

1）、安装Docker

2）、去Docker仓库找到这个软件对应的镜像；

3）、使用Docker运行这个镜像，这个镜像就会生成一个容器；

4）、对容器的启动停止，就是对软件的启动和停止。

**在linux上安装docker**

```shell
1、查看centos版本,要求：大于3.10
# uname -r
如果小于的话升级
# yum update
2、安装docker（需要联网,root权限）
# yum install docker
3、启动docker
# systemctl start docker
# docker -v
4、开机启动docker
# systemctl enable docker
5、停止docker
# systemctl stop docker
```

**docker的常用操作** 

```shell
1. 在docker hub中搜索镜像（也可以去官网上查找）
	# docker search mysql
2. 拉取
    默认最新版本
    # docekr pull mysql 
    安装指定版本
    # docker pull mysql:5.5
3. 查看镜像列表
	# docker images		
4. 删除指定镜像
	# docker rmi imageId
5. 运行镜像，生成容器（相当于运行set-up.exe，安装软件）
    --name 自定义容器名   -d 后台运行
    # docker run --name mysql001 -d mysql -e MYSQL_ROOT_PASSWORD=123456 
6. 查看哪些容器正在运行
	# docker ps
7. 停止运行中的容器
	# docker stop containerId
8. 启动容器（相当于运行软件的.exe）
	# docker start containerId
9. 删除一个容器
	# docker rm containerId
10. 端口映射
    -p 主机端口映射到容器端口（虚拟机端口8888，容器端口8080）
    # docker run --name mytomcat -d -p 8888:8080 tomcat
    浏览器访问时访问8888，才能访问到tomcat
    # docker run --name mytomcat2 -d -p 9000:8080 tomcat
    启动两个tomcat
11. 查看容器日志
	# docker logs containerId
```



### JDBC

#### 1、DataSource配置

参考DataSourceConfiguration，根据配置创建数据源，可以使用spring.datasource.type指定数据源 

**springBoot默认支持以下数据源**：TomcatDataSource、HikariDataSource、dbcp.BasicDataSource、dbcp2.BasicDataSource（到底默认配置了哪个，要看springBoot往maven中注入了什么依赖包）

编写配置文件application.yml

```yaml
spring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://192.168.179.131:3306/jdbc
    driver-class-name: com.mysql.jdbc.Driver
```

**JdbcTemplate**: 在有数据源的情况下，springBoot会自动配置JdbcTemplate，用来操作数据库

```java
    @Autowired
    JdbcTemplate jdbcTemplate;

    @ResponseBody
    @GetMapping("/test")
    public List<Map<String,Object>> testJdbc()throws Exception{
        List<Map<String,Object>> list = jdbcTemplate.queryForList("select * from BUSDATA");
        return list;
    }
```

#### 2、自定义数据源

1）导入Druid依赖

```xml
<!-- https://mvnrepository.com/artifact/com.alibaba/druid -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.12</version>
</dependency>
```

2）添加DruidDataSource的bean，创建DruidConfig

```java
@Configuration
public class DruidConfig {

    //配置文件中的以spring.datasource开头的字段进行绑定
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource dataSource(){
        return new DruidDataSource();
    }
}
```

3）配置DruidDataSource

```yaml
spring:
  datasource:
    username: root
    password: Welcome_1
    url: jdbc:mysql://192.168.179.131:3306/jdbc
    driver-class-name: com.mysql.jdbc.Driver
	# 初始化连接池大小，最小，最大  
    initialSize: 5
    minIdle: 5
    maxActive: 20
    # 获取连接时等待超时的时间  
    maxWait: 60000
    # 间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 
    timeBetweenEvictionRunsMillis: 60000
    # 一个连接在池中最小生存的时间，单位是毫秒 
    minEvictableIdleTimeMillis: 300000
    # 用来检测连接是否有效的sql
    validationQuery: SELECT 1 FROM DUAL
    # 申请连接时检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性
    testWhileIdle: true
    # 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    testOnBorrow: false
    # 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    testOnReturn: false
    # 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle，在mysql下建议关闭
    poolPreparedStatements: true
    maxPoolPreparedStatementPerConnectionSize: 20
    # 配置监控统计用的filter:stat，日志用的filter:log4j，防御sql注入的filter:wall
    filters: stat,wall,log4j
```

4）配置Druid后台监控

```java
    //配置Druid的监控
    //1. 配置一个管理后台
    @Bean
    public ServletRegistrationBean statViewServlet(){
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String,String> initParams = new HashMap<>();
        initParams.put("loginUsername","admin");
        initParams.put("loginPassword","123456");
        bean.setInitParameters(initParams);
        return bean;
    }
    //2. 配置监控的filter
    @Bean
    public FilterRegistrationBean webStatFilter(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new WebStatFilter());
        bean.setUrlPatterns(Arrays.asList("/*"));
        Map<String,String> initParams = new HashMap<>();
        initParams.put("exclusions","*.js,*.css,/druid/*");
        bean.setInitParameters(initParams);
        return bean;
    }
```

#### 3、整合MyBatis

1）导入依赖

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
```

这里最好指定版本，否则导入的版本可能缺少@Mapper注解，无法将xxxMapper类注入容器

mybatis-spring-boot-starter的1.3.2版本会集成mybatis、mybatis-spring、mybatis-spring-boot-autoConfigure

2）新建Mapper作为一个DAO接口（注解方式）

```java
@Mapper
public interface DepartmentMapper {

    @Insert("insert into department(dept_name) value(#{deptName})")
    public int insertDept(Department department);

    @Delete("delete from department where id=#{id}")
    public int deleteDeptById(Integer id);

    @Update("update department set dept_Name=#{deptName} where id=#{id}")
    public int updateDept(Department department);

    @Select("select * from department where id=#{id}")
    public Department getDeptById(Integer id);

}
```

同时要在SpringBootApplication类上增加注解 **@MapperScan**(value="mapper")

3）在controller中注入mapper

```java
@RestController
public class DeptController {

    @Autowired
    DepartmentMapper departmentMapper;

    @RequestMapping("/getDept/{id}")
    public Department getDepartment(@PathVariable("id") Integer id){
        return departmentMapper.getDeptById(id);
    }

    @RequestMapping("/delDept/{id}")
    public int delDept(@PathVariable("id") Integer id){
        return departmentMapper.deleteDeptById(id);
    }

    @RequestMapping("/update/{id}")
    public int updateDept(@PathVariable("id") Integer id){
        return departmentMapper.updateDept(new Department(id, "XXX"));
    }

    @RequestMapping("/insert")
    public int insertDept(Department department){
        return departmentMapper.insertDept(department);
    }
}
```

如果@Autowired仍会报错，但不影响程序运行，可以通过设置settings -> edit -> inspections -> spring -> spring code -> Autowiring for bean class改为Warning

springBoot整合myBatis不需要sqlsession的获取

4）如果采用配置文件的方式替代注解方式，即全局配置文件和mapper映射文件，并在application.yml中添加如下配置

```yaml
mybatis:
  config-location: classpath:mybatis/mybatis-config.xml
  mapper-locations: classpath:mybatis/mapper/*.xml
```

#### 4、整合JPA

Application可采用**springData**实现统一的数据访问，包括springData JPA、springData MongoDB、springData Redis，springData提供了包括CRUD、查询、排序、分页、乐观锁等功能

JPA也是基于ORM（对象关系映射）思想，底层是Hibernate，Hibernate可以自动生成和执行sql语句，在java代码层面基本不需要编写sql（JPA和MyBatis的比较实质上是Hibernate和MyBatis的比较）

1）新建一个实体类

````java
//使用JPA注解配置映射关系
@Entity    //告诉JPA这是一个实体类（和数据表映射的类）
@Table(name="tbl_user") //@Table来指定和哪个数据表对应，如果省略默认表明就是user;

public class User {

    @Id //这是一个主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //自增主键
    private Integer id ;

    @Column(name="last_name",length = 50)   //这是和数据表对应的一个列
    private String lastName;
    @Column  //省略默认列名就是属性名
    private String email;
````

2）新建一个Repository（DAO接口）来继承JPA的绝大部分功能

```java
//继承jpaRepository来完成对数据库的操作
//不需要加注解，可直接注入
public interface UserRepository extends JpaRepository<User, Integer> {
}
```

3）对JPA进行相关配置

```yaml
spring:
  jpa:
    hibernate:
    #启动时自动更新或创建数据表
      ddl-auto: update
    #控制台显示生成的sql
    show-sql: true
```

4）编写相应的controller

```java
@RestController
public class UserController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable("id") Integer id){
        User user = userRepository.findOne(id);
        return user;
    }

    @GetMapping("/insert")
    public User insertUser(User user){
        User user1 = userRepository.save(user);
        return user1;
    }
}
```



### springBoot启动原理

#### 回调机制

模块间调用的几种方式：

1）同步调用：类A的方法a()调用类B的方法b()，**一直等待b()方法执行完毕**，a()方法继续往下走。这种调用方式适用于方法b()执行时间不长的情况 

2）异步调用：**类A的方法a()通过新起线程的方式调用类B的方法b()，代码接着直接往下执行**，这样无论方法b()执行时间多久，都不会阻塞住方法a()的执行。但是这种方式，由于方法a()不等待方法b()的执行完成，在方法a()需要方法b()执行结果的情况下，必须通过一定的方式对方法b()的执行结果进行监听。在Java中，可以使用Future+Callable的方式做到这一点。 

3）回调：回调的思想是：类A的a()方法调用类B的b()方法，**类B的b()方法执行完毕主动调用类A的callback()方法**，形成双向的调用

#### springBoot启动流程

几个事件监听器

spring.factories中：

**ApplicationContextInitializer**

**SpringApplicationRunListener**

ioc容器中：

**ApplicationRunner**

**CommandLineRunner**

**1）创建springApplication对象**

```java
private void initialize(Object[] sources) {
    //保存主配置类
   if (sources != null && sources.length > 0) {
      this.sources.addAll(Arrays.asList(sources));
   }
    //判断当前是否是个web应用
   this.webEnvironment = deduceWebEnvironment();
    //从类路径下找到META-INF/spring.factories配置中的所有ApplicationContextInitializer 然后保存起来
   setInitializers((Collection) getSpringFactoriesInstances(
         ApplicationContextInitializer.class));
    //从类路径下找到META-INF/spring.factories配置中的所有ApplicationListener 然后保存起来
   setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    //决定哪一个是主程序
   this.mainApplicationClass = deduceMainApplicationClass();
}
```

2）执行run方法

```java
//返回一个可配置的IOC容器
public ConfigurableApplicationContext run(String... args) {
   StopWatch stopWatch = new StopWatch();
   stopWatch.start();
   ConfigurableApplicationContext context = null;
   FailureAnalyzers analyzers = null;
   configureHeadlessProperty();
    //从类路径下META-INF/spring.factory获取SpringApplicationRunListeners，这是springboot启动过程中运行的监听器，可以在相应节点执行其回调方法
   SpringApplicationRunListeners listeners = getRunListeners(args);
    //回调所有的SpringApplicationRunListener.starting()方法
   listeners.starting();
   try {
       //封装命令行参数
      ApplicationArguments applicationArguments = new DefaultApplicationArguments(
            args);
       //创建环境，完成后回调SpringApplicationRunListener.environmentPrepared()，环境准备完成
      ConfigurableEnvironment environment = prepareEnvironment(listeners,
            applicationArguments);
       //打印SpringBoot图标
      Banner printedBanner = printBanner(environment);
       //创建ApplicationContext，决定创建web的ioc容器还是普通的ioc
      context = createApplicationContext();
       //异常分析
      analyzers = new FailureAnalyzers(context);
       
       //重点：将environment保存在ioc中，执行applyInitializers(context)，在这一步回调之前所保存的所有ApplicationContextInitializer的initialize方法       
       //回调所有的SpringApplicationRunListener的contextPrepared()
       //prepareContext运行完成之后，回调所有的SpringApplicationRunListener的contextLoaded()
      prepareContext(context, environment, listeners, applicationArguments,
            printedBanner);
       //重点：刷新所有组件，ioc容器初始化，如果是web应用还会创建嵌入式的Servlet
       //在这一步，扫描、创建、加载所有组件
      refreshContext(context);
       
       //从ioc容器中获取所有的ApplicationRunner和CommandLineRunner
       //ApplicationRunner先回调，CommandLineRunner再回调
      afterRefresh(context, applicationArguments);
       //回调所有的SpringApplicationRunListener中的finished方法
      listeners.finished(context, null);
       //保存应用状态
      stopWatch.stop();
      if (this.logStartupInfo) {
         new StartupInfoLogger(this.mainApplicationClass)
               .logStarted(getApplicationLog(), stopWatch);
      }
       //整个springboot启动完成以后返回启动的ioc容器
      return context;
   }catch (Throwable ex) {
      handleRunFailure(context, listeners, analyzers, ex);
      throw new IllegalStateException(ex);
   }
}
```

#### 添加自定义的监听器

自定义的xxxApplicationContextInitializer和xxxSpringApplicationRunListener在文件META-INF/spring.factories中加入 

```properties
org.springframework.context.ApplicationContextInitializer=\
demo.listener.HelloApplicationContextInitializer

org.springframework.boot.SpringApplicationRunListener=\
demo.listener.HelloSpringApplicationRunListener
```

自定义的xxxApplicationRunner和xxxCommandLineRunner通过@Component注解加入ioc容器中



### 自定义starts

start：场景启动器

1. 场景需要使用什么依赖？
2. 如何编写自动配置（xxxAutoConfiguration类）

```java
@Configuration  //指定这个类是一个配置类
@ConditionalOnXXX   //在指定条件下自动配置类生效
@AutoConfigureAfter   //指定自动配置类的顺序

@Bean //给容器中添加组件
@EnableConfigurationProperties(xxxProperties.class)    //让xxxProperties这个组件生效加到ioc容器中
@ConfigurationProperties(prefix ="xxx")   //将xxxProperties类中的属性与配置文件中的配置绑定
```

自动配置类要能加载，**需要添加到classpath:/META-INF/spring.factories中** 

```properties
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
```

3. 模式

starter是一个空的jar，仅提供辅助性依赖管理，这些依赖可能用于自动配置或其他类库

命名规约：非官方的自定义starter，xxx-spring-boot-starter

#### starter创建流程

1. 创建一个空工程，加入两个模块，一个是xxx-spring-boot-starter（Maven模块），另一个是xxx-spring-boot-starter-autoconfigurer（spring initializer模块），两个模块中的test文件夹及依赖均可删除
2. 在starter模块的maven中依赖autoconfigurer模块
3. 在autoconfigurer模块中 ，加入以下三个文件

helloService.java

```java
public class HelloService {
    HelloProperties helloProperties;

    public void setHelloProperties(HelloProperties helloProperties) {
        this.helloProperties = helloProperties;
    }

    public String sayHello(String name){
        return helloProperties.getPrefix() + name + helloProperties.getSuffix();
    }
}
```

helloProperties.java

```java
@ConfigurationProperties(prefix = "cxlab.hello")
public class HelloProperties {
    private String prefix = "Welcome, ";
    private String suffix = " cafe babe!";

    public String getPrefix() { return prefix; }

    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }

    public void setSuffix(String suffix) { this.suffix = suffix; }
}
```

helloAutoConfiguration.java

```java
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(HelloProperties.class)
public class HelloAutoConfiguration {
    @Autowired
    HelloProperties helloProperties;

    @Bean
    public HelloService helloService(){
        HelloService helloService = new HelloService();
        helloService.setHelloProperties(helloProperties);
        return helloService;
    }
}
```

4. 在autoconfigurer模块中的resource中添加META-INF/spring.factories，并添加自动配置类

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  zju.HelloAutoConfiguration (copy reference)
```

5. 先将autoconfigurer模块打包到maven仓库，再将stater模块打包到maven仓库（maven Projects --> lifecycle --> install）
6. 新建project，spring initializer，且为web应用，maven中添加对starter的依赖，编写controller可以实现对helloService的调用
7. 在该project的application.properties中可以修改对helloProperties的配置