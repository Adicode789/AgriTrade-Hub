package com.myproject.AgritradeHub.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.AgritradeHub.Model.AllUsers;
import com.myproject.AgritradeHub.Model.AllUsers.userRole;

public interface AllUsersRepository extends JpaRepository<AllUsers, Long> {

	boolean existsByEmail(String email);

	AllUsers findByEmail(String email);

	List<AllUsers> findAllByRole(userRole farmer);

	Object countByRole(userRole farmer);


}
