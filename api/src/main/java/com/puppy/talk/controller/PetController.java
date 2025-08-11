package com.puppy.talk.controller;

import com.puppy.talk.service.PetLookUpService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetLookUpService petLookUpService;

    @GetMapping
    public List<PetResponse> getPets() {
        return petLookUpService.findAllPets()
            .stream()
            .map(PetResponse::from)
            .toList();
    }
}
