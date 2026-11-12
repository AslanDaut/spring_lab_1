package kz.iitu.spring_lab_1.web;

import kz.iitu.spring_lab_1.domain.AppUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final AppUserRepository users;

    public AdminController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping("/admin")
    public String adminPanel() {
        String list = users.findAll().stream()
                .map(u -> "<li><b>" + u.getUsername() + "</b> — roles: " + u.getRoles() + "</li>")
                .reduce("", String::concat);
        return "<h2>Admin Panel</h2><ul>" + list + "</ul>";
    }
}