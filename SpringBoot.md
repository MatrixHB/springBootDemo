****

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

#### 如何修改springMVC的默认配置

1）、springBoot在自动配置很多MVC组件的时候，会先看用户有没有自己配置的组件（@Bean、@Component），如果有就用用户配置的，如果没有才自动配置；如果有些组件可以有多个（比如ViewResolver），就将用户配置的和默认配置的组合起来；

2）、**扩展MVC配置**：如果想要保持springBoot MVC的配置，只添加额外的MVC配置（比如interceptor拦截器、formatteer格式转换器、view controller等），可以编写一个配置类（**@Configuration**），同时要继承**WebMvcConfigurerAdapter**类型，但不能标注**@EnableWebMvc**

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

3）、**全面接管springMVC**：如果完全不需要springMVC的自动配置，所有都是用户自己配置，我们需要编写一个配置类（@Configuration），并且标注**@EnableWebMvc**

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