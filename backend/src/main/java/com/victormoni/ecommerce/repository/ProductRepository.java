package com.victormoni.ecommerce.repository;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.victormoni.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/*
 * @author Victor Moni
 */

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>  {

}
