package component;

/*
 * 解析链接上携带的区域信息，切换国际化资源
 */

import org.springframework.web.servlet.LocaleResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

//注意要添加在容器中，即在配置类中添加bean
public class MyLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        String lang = httpServletRequest.getParameter("lg");
        Locale locale = Locale.getDefault();            //这里必须获取默认的locale，否则第一次访问页面locale会等于null，出现NPE
        if( !StringUtils.isEmpty(lang)) {     //参数不为空
            String[] params = lang.split("_");
            locale = new Locale(params[0], params[1]);
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

    }
}
