package com.myproject.AgritradeHub.Controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.AgritradeHub.API.SendAutoEmail;
import com.myproject.AgritradeHub.Model.AllUsers;
import com.myproject.AgritradeHub.Model.AllUsers.UserStatus;
import com.myproject.AgritradeHub.Model.AllUsers.userRole;
import com.myproject.AgritradeHub.Model.Category;
import com.myproject.AgritradeHub.Model.Enquiry;
import com.myproject.AgritradeHub.Model.Orders.OrderStatus;
import com.myproject.AgritradeHub.Model.Payment;
import com.myproject.AgritradeHub.Repository.AllUsersRepository;
import com.myproject.AgritradeHub.Repository.CategoryRepository;
import com.myproject.AgritradeHub.Repository.EnquiryRepository;
import com.myproject.AgritradeHub.Repository.OrdersRepository;
import com.myproject.AgritradeHub.Repository.PaymentRepository;
import com.myproject.AgritradeHub.Repository.ProductsRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {

	@Autowired
	private HttpSession session;

	@Autowired
	private AllUsersRepository usersRepo;

	@Autowired
	private CategoryRepository categoryRepo;

	@Autowired
	private EnquiryRepository enquiryRepo;

	@Autowired
	private PaymentRepository paymentRepo;
	
	@Autowired
	private ProductsRepository productsRepo;
	
	@Autowired
	private OrdersRepository ordersRepo;
	
	@Autowired
	private SendAutoEmail sendAutoEmail;

	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		// ✅ Counts
        model.addAttribute("farmerCount", usersRepo.countByRole(userRole.FARMER));
        model.addAttribute("merchantCount", usersRepo.countByRole(userRole.MERCHANT));
        model.addAttribute("productCount", productsRepo.count());
        model.addAttribute("orderCount", ordersRepo.count());
        model.addAttribute("categoryCount", categoryRepo.count());
        model.addAttribute("enquiryCount", enquiryRepo.count());

        // ✅ Recent Enquiries (last 5)
       // List<Enquiry> recentEnquiries = enquiryRepo.findTop5ByOrderByEnquiryDateDesc();
       // model.addAttribute("recentEnquiries", recentEnquiries);

        // Orders by status
        long cancelledOrders = ordersRepo.countByOrderStatus(OrderStatus.CANCELLED);
        long confirmedOrders = ordersRepo.countByOrderStatus(OrderStatus.CONFIRMED);
        long deliveredOrders = ordersRepo.countByOrderStatus(OrderStatus.DELIVERED);
        
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
		
		
		return "Admin/Dashboard";
	}

	@GetMapping("/ManageFarmers")
	public String ShowManageFarmers(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";

		}

		List<AllUsers> farmerList = usersRepo.findAllByRole(userRole.FARMER);
		model.addAttribute("farmerList", farmerList);
		return "Admin/ManageFarmers";
	}

	@GetMapping("/FarmerStatus")
	public String UpdateFarmerStatus(@RequestParam long id, RedirectAttributes attributes) {
		try {
			AllUsers farmer = usersRepo.findById(id).get();
			if (farmer.getStatus().equals(UserStatus.PENDING)) {
				farmer.setStatus(UserStatus.VERIFIED);
				usersRepo.save(farmer);
				sendAutoEmail.sendApprovalEmail(farmer);

				// email integration for inform user

			} else if (farmer.getStatus().equals(UserStatus.VERIFIED)) {
				farmer.setStatus(UserStatus.DISABLED);
				usersRepo.save(farmer);

			} else {
				farmer.setStatus(UserStatus.VERIFIED);
				usersRepo.save(farmer);
			}

			attributes.addFlashAttribute("msg", farmer.getName() + "Status Successfully Updated");
			return "redirect:/Admin/ManageFarmers";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Admin/ManageFarmers";
		}

	}

	@GetMapping("/ManageMerchants")
	public String ShowManageMerchants(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";

		}

		List<AllUsers> merchantList = usersRepo.findAllByRole(userRole.MERCHANT);
		model.addAttribute("merchantList", merchantList);
		return "Admin/ManageMerchants";
	}

	@GetMapping("/MerchantStatus")
	public String UpdateMerchantStatus(@RequestParam long id, RedirectAttributes attributes) {
		try {
			AllUsers merchant = usersRepo.findById(id).get();
			if (merchant.getStatus().equals(UserStatus.PENDING)) {
				merchant.setStatus(UserStatus.VERIFIED);
				usersRepo.save(merchant);
				sendAutoEmail.sendApprovalEmail(merchant);

				// email integration for inform user

			} else if (merchant.getStatus().equals(UserStatus.VERIFIED)) {
				merchant.setStatus(UserStatus.DISABLED);
				usersRepo.save(merchant);

			} else {
				merchant.setStatus(UserStatus.VERIFIED);
				usersRepo.save(merchant);
			}

			attributes.addFlashAttribute("msg", merchant.getName() + " Status Successfully Updated");
			return "redirect:/Admin/ManageMerchants";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Admin/ManageMerchants";
		}

	}

	@GetMapping("/AddCategory")
	public String ShowAddCategory(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";

		}

		List<Category> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);

		return "Admin/AddCategory";

	}

	@PostMapping("/AddCategory")
	public String AddCategory(@RequestParam("categoryName") String categoryName, RedirectAttributes attributes) {
		try {
			Category cate = new Category();
			cate.setCategoryName(categoryName);
			categoryRepo.save(cate);
			attributes.addFlashAttribute("msg", "Category Successfully Added!!");

			return "redirect:/Admin/AddCategory";
		}

		catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());

			return "redirect:/Admin/AddCategory";
		}
	}

	@GetMapping("/DeleteCategory")
	public String DeleteCategory(@RequestParam long id) {

		categoryRepo.deleteById(id);
		return "redirect:/Admin/AddCategory";

	}

	@GetMapping("/ChangePassword")
	public String ShowChangePassword() {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		return "Admin/ChangePassword";
	}

	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes) {
		try {
			String oldPasswoerd = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");

			if (!newPassword.equals(confirmPassword)) {
				attributes.addFlashAttribute("msg", "New and Confirm Password are not same");
				return "redirect:/Admin/ChangePassword";

			}
			AllUsers admin = (AllUsers) session.getAttribute("loggedInAdmin");

			if (newPassword.equals(admin.getPassword())) {
				attributes.addFlashAttribute("msg", "Can't Change , new and old password are same!!");
				return "redirect:/Admin/ChangePassword";

			}

			if (oldPasswoerd.equals(admin.getPassword())) {
				admin.setPassword(confirmPassword);
				usersRepo.save(admin);
				session.removeAttribute("loggedInAdmin");
				return "redirect:/Login";

			} else {
				attributes.addFlashAttribute("msg", "Invalid Old Password!!!");
			}

			return "redirect:/Admin/ChangePassword";

		} catch (Exception e) {
			return "redirect:/Admin/ChangePassword";
		}

	}

	@GetMapping("/enquiry")
	public String ShowEnquiry(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Enquiry> enqList = enquiryRepo.findAll();
		model.addAttribute("enqList", enqList);
		return "Admin/enquiry";
	}
	
	@GetMapping("/DeleteEnquiry")
	public String DeleteEnquiry(@RequestParam long id) {

		enquiryRepo.deleteById(id);
		return "redirect:/Admin/enquiry";

	}

	
	

	@GetMapping("/ViewOrder")
	public String ShowViewOrder(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "Admin/Login";
		}
		List<Payment> paymentList = paymentRepo.findAll();
		model.addAttribute("paymentList", paymentList); 
		return "Admin/ViewOrder";
	}

	@GetMapping("/EditProfile")
	public String ShowUserProfile(Model model) {
		
		if (session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/Login";
		}
		
		AllUsers admin = (AllUsers) session.getAttribute("loggedInAdmin");
		model.addAttribute("admin", admin);
		
		
		return "Admin/EditProfile";
	}
	
	@PostMapping("/EditProfile")
	public String UpdateUserProfile(@RequestParam("ImageFile")MultipartFile profilePic, RedirectAttributes attributes ) {
		try { 
			
			String storageFileName = System.currentTimeMillis()+"_"+profilePic.getOriginalFilename();
			String uploadDir = "Public/ProfilePicture/";
			Path uploadPath = Paths.get(uploadDir);
			
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			try(InputStream inputStream = profilePic.getInputStream())
			{
				Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
				
			}
			AllUsers admin = (AllUsers) session.getAttribute("loggedInAdmin");
			admin.setProfilePic(storageFileName);
			usersRepo.save(admin);
			
			attributes.addFlashAttribute("msg", "profile pic updated Successfully✅");
			
			return "redirect:/Admin/EditProfile";
		} catch (Exception e) {
			
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Admin/EditProfile";
		}
		
	}
	
	
	
	
	
	
	@GetMapping("/logout")
	public String Logout() {
		session.removeAttribute("loggedInAdmin");
		return "redirect:/Login";
	}
}
