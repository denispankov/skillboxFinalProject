package ru.pankov.controllers.site;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class SiteController {

    @GetMapping("/admin")
    public String getAdminPage(){
        return "index";
    }
}
