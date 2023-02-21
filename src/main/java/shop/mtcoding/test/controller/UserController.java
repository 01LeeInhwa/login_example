package shop.mtcoding.test.controller;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import shop.mtcoding.test.dto.user.UserReq.JoinReqDto;
import shop.mtcoding.test.dto.user.UserReq.LoginReqDto;
import shop.mtcoding.test.handler.ex.CustomException;
import shop.mtcoding.test.model.User;
import shop.mtcoding.test.model.UserRepository;
import shop.mtcoding.test.service.UserService;
import shop.mtcoding.test.util.SHA256;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserService userService;

    SHA256 sha256 = new SHA256();

    @GetMapping("/")
    public String main(Model model) {
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            throw new CustomException("인증이 되지 않았습니다", HttpStatus.UNAUTHORIZED);
        }
        model.addAttribute("principal", userRepository.findAll());

        return "user/main";

    }

    @PostMapping("/join")
    public String join(JoinReqDto joinReqDto) throws NoSuchAlgorithmException {

        if (joinReqDto.getUsername() == null || joinReqDto.getUsername().isEmpty()) {
            throw new CustomException("username을 작성해주세요");
        }

        if (joinReqDto.getPassword() == null || joinReqDto.getPassword().isEmpty()) {
            throw new CustomException("password를 작성해주세요");
        }

        if (joinReqDto.getEmail() == null || joinReqDto.getEmail().isEmpty()) {
            throw new CustomException("email을 작성해주세요");
        }
        String username = joinReqDto.getUsername();
        String encodedPassword = sha256.encrypt(joinReqDto.getPassword());
        String email = joinReqDto.getEmail();
        int result = userRepository.insert(username, encodedPassword, email);
        if (result != 1) {
            throw new CustomException("회원가입실패");
        }

        return "redirect:/loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    @PostMapping("/login")
    public String login(LoginReqDto loginReqDto) throws NoSuchAlgorithmException {
        if (loginReqDto.getUsername() == null || loginReqDto.getUsername().isEmpty()) {
            throw new CustomException("username을 작성해주세요");
        }
        if (loginReqDto.getPassword() == null || loginReqDto.getPassword().isEmpty()) {
            throw new CustomException("password를 작성해주세요");
        }

        String username = loginReqDto.getUsername();
        String encodedPassword = sha256.encrypt(loginReqDto.getPassword());
        User principal = userRepository.findByUsernameAndPassword(username, encodedPassword);

        session.setAttribute("principal", principal);

        return "redirect:/";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }

}
