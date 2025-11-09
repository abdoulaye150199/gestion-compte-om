package com.example.gesioncompteom.assembler;

import com.example.gesioncompteom.controller.CompteController;
import com.example.gesioncompteom.model.Compte;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CompteModelAssembler implements RepresentationModelAssembler<Compte, EntityModel<Compte>> {
    @Override
    public EntityModel<Compte> toModel(Compte compte) {
        return EntityModel.of(compte,
                linkTo(methodOn(CompteController.class).get(compte.getId())).withSelfRel(),
                linkTo(methodOn(CompteController.class).list()).withRel("comptes"),
                linkTo(methodOn(CompteController.class).credit(compte.getId(), null)).withRel("credit"),
                linkTo(methodOn(CompteController.class).debit(compte.getId(), null)).withRel("debit")
        );
    }
}

