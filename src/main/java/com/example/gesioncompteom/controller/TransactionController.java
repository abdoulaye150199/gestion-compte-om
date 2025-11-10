package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.assembler.TransactionModelAssembler;
import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.TransactionService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final TransactionModelAssembler assembler;

    public TransactionController(TransactionService service, TransactionModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }



    @GetMapping("/{id}")
    public EntityModel<Transaction> get(@PathVariable String id) {
        Transaction t = service.getById(id);
        return assembler.toModel(t);
    }

    @GetMapping
    public CollectionModel<EntityModel<Transaction>> list() {
    List<EntityModel<Transaction>> list = service.listAll().stream()
        .map(t -> assembler.toModel(t))
        .collect(Collectors.toList());
        return CollectionModel.of(list, linkTo(methodOn(TransactionController.class).list()).withSelfRel());
    }
}

