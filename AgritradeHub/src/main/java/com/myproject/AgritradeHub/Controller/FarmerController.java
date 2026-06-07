package com.myproject.AgritradeHub.Controller;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.AgritradeHub.API.PaymentService;
import com.myproject.AgritradeHub.API.SendAutoEmail;
import com.myproject.AgritradeHub.Model.AllUsers;
import com.myproject.AgritradeHub.Model.Category;
import com.myproject.AgritradeHub.Model.Orders;
import com.myproject.AgritradeHub.Model.Orders.OrderStatus;
import com.myproject.AgritradeHub.Model.Payment;
import com.myproject.AgritradeHub.Model.Products;
import com.myproject.AgritradeHub.Model.Products.ProductStatus;
import com.myproject.AgritradeHub.Repository.AllUsersRepository;
import com.myproject.AgritradeHub.Repository.CategoryRepository;
import com.myproject.AgritradeHub.Repository.OrdersRepository;
import com.myproject.AgritradeHub.Repository.PaymentRepository;
import com.myproject.AgritradeHub.Repository.ProductsRepository;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Farmer")
public class FarmerController{
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	@Autowired
	private ProductsRepository productsRepo;
	
	@Autowired
	private OrdersRepository ordersRepo;
	
	@Autowired
	private PaymentRepository paymentRepo;
	
	@Autowired
	private AllUsersRepository usersRepo;
	
	@Autowired
	private SendAutoEmail sendAutoEmail;
	 
	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/Login";
		}
		AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
		model.addAttribute("totalProducts", productsRepo.countByFarmer(farmer));
		model.addAttribute("totalOrders", ordersRepo.countByFarmer(farmer));
		model.addAttribute("completedOrders", ordersRepo.countByFarmerAndOrderStatus(farmer, OrderStatus.DELIVERED));
		model.addAttribute("cancelledOrders", ordersRepo.countByFarmerAndOrderStatus(farmer, OrderStatus.CANCELLED));
		
		List<Orders> recentOrders = ordersRepo.findTop5ByFarmerOrderByOrderDateDesc(farmer);
		model.addAttribute("recentOrders", recentOrders);
		
		model.addAttribute("totalRevenue", ordersRepo.getTotalRevenueByFarmerId(farmer.getId()));
		model.addAttribute("monthlyRevenue", ordersRepo.getCurrentMonthRevenue(farmer.getId()));
		model.addAttribute("topProduct", ordersRepo.getMostOrderedProduct(farmer.getId()));
		
		BigDecimal inStockRevenue = productsRepo.calculateInStockRevenue(farmer.getId());
		model.addAttribute("inStockRevenue", inStockRevenue != null ? inStockRevenue : BigDecimal.ZERO);
		
		
		return "Farmer/Dashboard";
		
	}
	
	
	@GetMapping("/AddProduct")
	public String ShowAddProduct(Model model) 
	{
		if (session.getAttribute("loggedInFarmer")==null) {
			return"redirect:/Login";
		}
		List<Category> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);
		Products product = new Products();
		model.addAttribute("product", product);
		
		return "Farmer/AddProduct";
	}
	
	
	@PostMapping("/AddProduct")
	public String AddProduct(@ModelAttribute("product") Products product, @RequestParam("ImageFile") MultipartFile productImage,RedirectAttributes attributes)        
	{
		try {
			
			String storageFileName = UUID.randomUUID()+"_"+productImage.getOriginalFilename();
			String uploadDir = "public/ProductImage/";
			Path uploPath = Paths.get(uploadDir);
			if (!Files.exists(uploPath)) {
				Files.createDirectories(uploPath);
			}
			try(InputStream inputStream = productImage.getInputStream()){
				Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
				
			}
			product.setProductImage(storageFileName);
			if (product.getQuantity()<=0) {
				attributes.addFlashAttribute("msg", "Please Enter product Quantity");
				return ":/Farmer/AddProduct";
			}
			product.setStatus(ProductStatus.AVAILABLE);
			
			AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
			product.setFarmer(farmer);
			
			productsRepo.save(product);
			attributes.addFlashAttribute("msg", "Product Successfully added!!!");
			
			
			return "redirect:/Farmer/AddProduct";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/AddProduct";
		}
		
	}
	
	@GetMapping("/ManageProduct")
	public String ShowManageProduct(Model model) {
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/Login";
			
		}
		
		AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
		
		List<Products> productsList = productsRepo.findByFarmer(farmer);
		model.addAttribute("productsList", productsList);
		
		return "Farmer/ManageProduct";
	}
	
	@GetMapping("/UpdateProduct")
	public String ShowUpdateProduct(@RequestParam("id") long id,Model model)
	{
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/Login"; 
		}
		
		Products product = productsRepo.findById(id).get();
		model.addAttribute("product", product);
		
		return "Farmer/UpdateProduct";
	}
	
	@PostMapping("/UpdateProduct")
	public String UpdateProduct(@ModelAttribute("product")Products product, @RequestParam("productId") long productId, RedirectAttributes attributes)
	{
		 
		try {
			
			Products existingProduct = productsRepo.findById(productId).get();
			existingProduct.setQuantity(product.getQuantity());
			existingProduct.setPricePerUnit(product.getPricePerUnit());
			existingProduct.setStatus(ProductStatus.AVAILABLE);
			productsRepo.save(existingProduct);
			
			
			attributes.addFlashAttribute("msg", "Product Stock SuccessFully Updated ✅");
			
			return "redirect:/Farmer/UpdateProduct?id="+productId;
			
		} catch (Exception e) {
			
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Farmer/UpdateProduct?id="+productId;
		}
		
	}
	
	
	
	@GetMapping("/ManageOrders")
	public String ShowManageOrders(Model model)
	{
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/login";
		}
		
		AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
		
		List<Orders> orderList = ordersRepo.findAllByFarmer(farmer);
		model.addAttribute("orderList", orderList); 
		return "Farmer/ManageOrders";
		
	}
	
	@GetMapping("/CacelOrder")
	public String CacelOrder(@RequestParam("id")long id) {
		
		try {
			Orders order = ordersRepo.findById(id).get();
			Payment payment = paymentRepo.findByOrder(order);
			PaymentService refundService = new PaymentService();
			refundService.refundPayment(payment.getPayId());
			order.setOrderStatus(OrderStatus.CANCELLED);
			ordersRepo.save(order);
			sendAutoEmail.SendOrderCancellationEmail(order, payment);
			return "redirect:/Farmer/ManageOrders";
		} catch (Exception e) {
			return "redirect:/Farmer/ManageOrders";
		}
		
	}
	
	@GetMapping("/UpdateOrderStatus")
	public String UpdateOrderStatus(@RequestParam("id") long id, RedirectAttributes attributes)
	{
		try {
			Orders order = ordersRepo.findById(id).get();
			if (order.getOrderStatus().equals(OrderStatus.CONFIRMED)) {
				order.setOrderStatus(OrderStatus.DISPATCHED);
				
			}else if(order.getOrderStatus().equals(OrderStatus.DISPATCHED)){
				order.setOrderStatus(OrderStatus.DELIVERED);
			}
			ordersRepo.save(order);
			attributes.addFlashAttribute("msg", "Order Status Successfully Updated");
			
			return "redirect:/Farmer/ManageOrders";
		} catch (Exception e) {
			
			return "redirect:/Farmer/ManageOrders";
		}
		
	}
	
	
	
	@GetMapping("/ChangePassword")
	public String ShowChangePassword() {
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/Login";
		}
		return "Farmer/ChangePassword";
	}

	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes  attributes)
	{
		try {
			String oldPasswoerd = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");
			
			if (!newPassword.equals(confirmPassword)) {
				attributes.addFlashAttribute("msg", "New and Confirm Password are not same");
				return "redirect:/Farmer/ChangePassword";

			}
			AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
			
			if (newPassword.equals(farmer.getPassword())) {
				attributes.addFlashAttribute("msg", "Can't Change , new and old password are same!!");
				return "redirect:/Farmer/ChangePassword"; 
				
			}
			
			if (oldPasswoerd.equals(farmer.getPassword())) {
				farmer.setPassword(confirmPassword);
				usersRepo.save(farmer);
				session.removeAttribute("loggedInFarmer");
				return "redirect:/Login";
		
			}else {
				attributes.addFlashAttribute("msg", "Invalid Old Password!!!");
			}
			
			return "redirect:/Farmer/ChangePassword";

		} catch (Exception e) {
			return "redirect:/Farmer/ChangePassword";
		}
		
	}
	
	
	@GetMapping("/UserProfile")
	public String ShowUserProfile(Model model) {
		
		if (session.getAttribute("loggedInFarmer")==null) {
			return "redirect:/Login";
		}
		
		AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
		model.addAttribute("farmer", farmer);
		
		
		return "Farmer/UserProfile";
	}
	
	@PostMapping("/UserProfile")
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
			AllUsers farmer = (AllUsers) session.getAttribute("loggedInFarmer");
			farmer.setProfilePic(storageFileName);
			usersRepo.save(farmer);
			
			attributes.addFlashAttribute("msg", "profile pic updated Successfully✅");
			
			return "redirect:/Farmer/UserProfile";
		} catch (Exception e) {
			
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Farmer/UserProfile";
		}
		
	}
	
	
	
	
	
	@GetMapping("/logout")
	public String Logout() {
		session.removeAttribute("loggedInFarmer");
		return "redirect:/Login";
	}
}