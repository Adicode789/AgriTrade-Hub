package com.myproject.AgritradeHub.Controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.AgritradeHub.Model.AllUsers;
import com.myproject.AgritradeHub.Model.AllUsers.UserStatus;
import com.myproject.AgritradeHub.Model.AllUsers.userRole;
import com.myproject.AgritradeHub.Model.Enquiry;
import com.myproject.AgritradeHub.Repository.AllUsersRepository;
import com.myproject.AgritradeHub.Repository.EnquiryRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private AllUsersRepository userRepo;
	
	@Autowired
	private EnquiryRepository enqRepo;

	// index page get method
	@GetMapping("/")
	public String ShowIndex() {
		return "index";
	}

	@GetMapping("/AboutUs")
	public String ShowAboutUs()

	{
		return "aboutus";
	}

	@GetMapping("/Service")
	public String ShowServices() {
		return "services";
	}
	
	@GetMapping("/Login")
	public String Showlogin() 
	{
		return "Login";
	}
	
	@PostMapping("/Login")
	public String Login(HttpServletRequest request, RedirectAttributes attributes, HttpSession session)
	{
		try {
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			
			if (!userRepo.existsByEmail(email)) {
				attributes.addFlashAttribute("msg", "User not Found!!");
				return "redirect:/Login";
			}
			
			AllUsers user = userRepo.findByEmail(email);
			
			if (password.equals(user.getPassword())) {
				
				if (user.getStatus().equals(UserStatus.VERIFIED)) {
					// go to complete login
					if (user.getRole().equals(userRole.ADMIN)) {
						// go to Admin Dashbord
						session.setAttribute("loggedInAdmin", user);
						return "redirect:/Admin/Dashboard";
					}
					else if (user.getRole().equals(userRole.FARMER)) {
						// go to Farmer Dashboard
						session.setAttribute("loggedInFarmer", user);
						return "redirect:/Farmer/Dashboard";
						
					}
					else if (user.getRole().equals(userRole.MERCHANT)) {
						// go to merchant Dashboard
						session.setAttribute("loggedInMerchant", user);
						return "redirect:/Merchant/Dashboard";
						
					}
				}
				else if (user.getStatus().equals(UserStatus.PENDING)) {
					attributes.addFlashAttribute("msg", "Registration Pending, Please Wait for Admin Aproval!!!");
				}
				else {
					attributes.addFlashAttribute("msg", "Login Dissabled, Please Contact Aministration!");
				}
				
			}
			else {
				attributes.addFlashAttribute("msg", "Invalid Password!!!");
			}
			
			return "redirect:/Login";
			
		} catch (Exception e) {
			return "redirect:/Login";
		}
		
	}

	@GetMapping("/FormerRegistration")
	public String ShowFormerRegistration(Model model) {
		AllUsers farmer = new AllUsers();
		model.addAttribute("farmer", farmer);
		return "FormerRegistration";
	}

	@GetMapping("/MerchantRegistration")
	public String ShowMerchantRegistration(Model model) {
		AllUsers merchant = new AllUsers();
		model.addAttribute("merchant", merchant);
		return "MerchantRegistration";

	}

	@PostMapping("/FormerRegistration")
	public String FormerRegistration(@ModelAttribute AllUsers farmer, RedirectAttributes attributes) {
		try {
			if (userRepo.existsByEmail(farmer.getEmail())) {
				attributes.addFlashAttribute("msg", "user Already Exists!!");
				return "redirect:/FormerRegistration";
			}
			
			farmer.setRegDate(LocalDateTime.now());
			farmer.setRole(userRole.FARMER);
			farmer.setStatus(UserStatus.PENDING);
			userRepo.save(farmer);
			attributes.addFlashAttribute("msg", "Farmer Registration Successfull!!");

			return "redirect:/FormerRegistration";

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/FormerRegistration";
		}
	}

	@PostMapping("/MerchantRegistration")
	public String MerchantRegistration(@ModelAttribute AllUsers merchant) {
		try {
			merchant.setRegDate(LocalDateTime.now());
			merchant.setRole(userRole.MERCHANT);
			merchant.setStatus(UserStatus.PENDING);
			userRepo.save(merchant);
			return "redirect:/MerchantRegistration";

		} catch (Exception e) {
			System.err.println("Error :"+e.getMessage());
			return "redirect:/MerchantRegistration";
		}

	}
	
	@GetMapping("/ContactUs")
	public String showContactUs(Model model) {
	    Enquiry enq = new Enquiry();   
	    model.addAttribute("enq", enq);  
	    return "contactus";  
	}
	
	
	@PostMapping("/ContactUs")
	public String saveContactUs(@ModelAttribute("enq") Enquiry enq, RedirectAttributes attributes) {
	    try {
	        enq.setEnqDate(LocalDateTime.now());
	        

	        enqRepo.save(enq);

	        attributes.addFlashAttribute("msg", "Your enquiry has been submitted successfully!");
	        return "redirect:/ContactUs";
	    } catch (Exception e) {
	        attributes.addFlashAttribute("msg", "Something went wrong: " + e.getMessage());
	        return "redirect:/ContactUs";
	    }
	}

}
