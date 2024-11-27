package site.campingon.campingon.common.oauth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "home";
    }

    @GetMapping("/oauth/success")
    @ResponseBody
    public String success() {
        return "oauth google login success";
    }

    @GetMapping("/oauth/fail")
    @ResponseBody
    public String fail() {
        return "access denied";
    }

    @GetMapping("/oauth/logout")
    @ResponseBody
    public String logout() {
        return "logout";
    }

}
