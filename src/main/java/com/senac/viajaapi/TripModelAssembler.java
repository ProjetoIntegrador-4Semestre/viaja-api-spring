package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TripModelAssembler implements RepresentationModelAssembler<Trip, EntityModel<Trip>> {

    @Override
    public EntityModel<Trip> toModel(Trip trip) {
        return EntityModel.of(trip,
                linkTo(methodOn(TripController.class).getTripById(trip.getId())).withSelfRel(),
                linkTo(methodOn(TripController.class).getAllTrips(Pageable.unpaged())).withRel("trips"));
    }
}
