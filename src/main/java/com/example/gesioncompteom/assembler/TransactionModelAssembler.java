package com.example.gesioncompteom.assembler;

import com.example.gesioncompteom.controller.TransactionController;
import com.example.gesioncompteom.model.Transaction;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TransactionModelAssembler implements RepresentationModelAssembler<Transaction, EntityModel<Transaction>> {
    @Override
    public EntityModel<Transaction> toModel(Transaction transaction) {
        return EntityModel.of(transaction,
                linkTo(methodOn(TransactionController.class).get(transaction.getId())).withSelfRel(),
                linkTo(methodOn(TransactionController.class).list()).withRel("transactions")
        );
    }
}

