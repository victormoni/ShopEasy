package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
import org.springframework.security.core.Authentication;

/**
 *
 * @author Victor Moni
 */
public interface AuthService {

    Authentication authenticate(String username, String password);

}
