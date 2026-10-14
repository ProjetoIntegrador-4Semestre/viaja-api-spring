package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TripMemoModelAssembler implements RepresentationModelAssembler<TripMemo, EntityModel<TripMemo>> {

    @Override
    public EntityModel<TripMemo> toModel(TripMemo tripMemo) {
        return EntityModel.of(tripMemo,
                linkTo(methodOn(TripMemoController.class).getTripMemoById(tripMemo.getId())).withSelfRel(),
                linkTo(methodOn(TripMemoController.class).getAllTripMemos(Pageable.unpaged())).withRel("tripMemos"));
    }
}
