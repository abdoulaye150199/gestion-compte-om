package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.assembler.TransactionModelAssembler;
import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.TransactionService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
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
        String utilisateurId = extractUserIdFromToken();
        if (!t.getUtilisateurId().toString().equals(utilisateurId)) {
            throw new IllegalStateException("Accès non autorisé à cette transaction");
        }
        return assembler.toModel(t);
    }

    @GetMapping
    public CollectionModel<EntityModel<Transaction>> list() {
        String utilisateurId = extractUserIdFromToken();
        List<EntityModel<Transaction>> list = service.listByUtilisateurId(UUID.fromString(utilisateurId)).stream()
            .map(t -> assembler.toModel(t))
            .collect(Collectors.toList());
        return CollectionModel.of(list, linkTo(methodOn(TransactionController.class).list()).withSelfRel());
    }

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    private String extractUserIdFromToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }
        throw new IllegalStateException("Utilisateur non authentifié");
    }
}

