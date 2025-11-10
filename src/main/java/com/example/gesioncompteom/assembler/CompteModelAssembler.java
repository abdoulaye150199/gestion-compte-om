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
        // self-rel uses the owner-facing solde endpoint by account numero
        linkTo(methodOn(CompteController.class).getSolde(compte.getNumeroCompte())).withSelfRel()
    );
    }
}

