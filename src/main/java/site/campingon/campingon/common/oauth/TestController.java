package site.campingon.campingon.common.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import site.campingon.campingon.common.oauth.dto.OAuth2UserDto;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final CustomOAuth2UserService customOAuth2UserService;

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

    @GetMapping("/oauth/logout/success")
    @ResponseBody
    public String logoutSuccess() {
        return "oauth google logout success";
    }

    @GetMapping("/oauth/logout")
    @ResponseBody
    public RedirectView logout(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            customOAuth2UserService.deleteGoogleAccount(principal);
        }

        return new RedirectView("/oauth/logout/success");
    }

}
