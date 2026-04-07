package com.agencia.controller.publico;

import com.agencia.domain.dto.HomeDataDTO;
import com.agencia.service.HomeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeDataService homeDataService;

    @GetMapping
    public ResponseEntity<HomeDataDTO> obtenerHomeData() {
        return ResponseEntity.ok(homeDataService.obtenerHomeData());
    }
}
