package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class LoginController {

    @PostMapping(value = "/user/login")
//   @RequestMapping(value = "/user/login", method = RequestMethod.POST);
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Map<String, String> map, HttpSession session){
        if( username.equals("admin") && password.equals("123456")){
            //session记录登录信息
            session.setAttribute("user",username);
            return "redirect:/main.html";
        }else{
            map.put("msg", "用户名密码错误");
            return "login";
        }

    }
}
