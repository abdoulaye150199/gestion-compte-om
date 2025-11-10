package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.assembler.CompteModelAssembler;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.service.CompteService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final CompteService service;
    private final CompteModelAssembler assembler;

    public CompteController(CompteService service, CompteModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Compte c) {
        Compte saved = service.create(c);
        EntityModel<Compte> model = assembler.toModel(saved);
        return ResponseEntity.created(URI.create(model.getRequiredLink("self").getHref())).body(model);
    }

    @GetMapping("/{id}")
    public EntityModel<Compte> get(@PathVariable String id) {
        Compte c = service.getById(id);
        return assembler.toModel(c);
    }

    @GetMapping
    public CollectionModel<EntityModel<Compte>> list() {
        List<EntityModel<Compte>> comptes = service.listAll().stream().map(c -> assembler.toModel(c)).collect(Collectors.toList());
        return CollectionModel.of(comptes, linkTo(methodOn(CompteController.class).list()).withSelfRel());
    }

    @PostMapping("/{id}/credit")
    public EntityModel<Compte> credit(@PathVariable String id, @RequestBody(required = false) BigDecimal amount) {
        BigDecimal amt = amount == null ? BigDecimal.ZERO : amount;
        Compte c = service.credit(id, amt);
        return assembler.toModel(c);
    }

    @PostMapping("/{id}/debit")
    public EntityModel<Compte> debit(@PathVariable String id, @RequestBody(required = false) BigDecimal amount) {
        BigDecimal amt = amount == null ? BigDecimal.ZERO : amount;
        Compte c = service.debit(id, amt);
        return assembler.toModel(c);
    }
}

