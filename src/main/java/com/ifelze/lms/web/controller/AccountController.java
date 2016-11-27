package com.ifelze.lms.web.controller;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ifelze.lms.domain.User;
import com.ifelze.lms.repository.UserRepository;
import com.ifelze.lms.service.MailService;
import com.ifelze.lms.service.UserService;
import com.ifelze.lms.web.rest.vm.ManagedUserVM;

/**
 * controller for managing the current user's account.
 */
@Controller
public class AccountController {

    private final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;
    
    @GetMapping("register")
    public String getRegister(Model model){
		model.addAttribute("registerCommand", new ManagedUserVM());
        return "register";
    }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerCommand") @Validated ManagedUserVM managedUserVM, 
    		BindingResult bindingResult, HttpServletRequest request) {
    	log.debug("userRepository:" + userRepository);
    	log.debug(managedUserVM.toString());
    	Optional<User> users = userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase());
    	if(users != null && users.isPresent()){
    		bindingResult.rejectValue("login", "", "login already in use");
    	}else{
    		users = userRepository.findOneByEmail(managedUserVM.getEmail());
    		if(users != null && users.isPresent()){
    			bindingResult.rejectValue("email", "", "e-mail address already in use");
    		}
    	}
    	if(bindingResult.hasErrors()){
    		return "register";
    	}
        User user = userService.createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
        managedUserVM.getFirstName(), managedUserVM.getLastName(), managedUserVM.getEmail().toLowerCase(),
        managedUserVM.getLangKey());
        if(user != null){
	        String baseUrl = request.getScheme() +        // "http"
	                "://" +                                // "://"
	                request.getServerName() +              // "myhost"
	                ":" +                                  // ":"
	                request.getServerPort() +              // "80"
	                request.getContextPath();              // "/myContextPath" or "" if deployed in root context
	
	                mailService.sendActivationEmail(user, baseUrl);
	       return "redirect:start";
        }
        
        return "register";
    }
}
