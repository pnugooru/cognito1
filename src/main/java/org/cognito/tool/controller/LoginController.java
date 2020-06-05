package org.cognito.tool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LoginController {

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/setupMFA")
    public String landingPage(){
        return "setup-mfa";
    }

    @GetMapping("/associateMFA")
    public String page2(){
        return "associate-mfa";
    }

    @GetMapping("/caseDocument")
    public String page3(){
        return "document-case";
    }
}
