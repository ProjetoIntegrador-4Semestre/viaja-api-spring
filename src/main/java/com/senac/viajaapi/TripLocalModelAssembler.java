package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TripLocalModelAssembler implements RepresentationModelAssembler<TripLocal, EntityModel<TripLocal>> {

    @Override
    public EntityModel<TripLocal> toModel(TripLocal tripLocal) {
        return EntityModel.of(tripLocal,
                linkTo(methodOn(TripLocalController.class).getTripLocalById(tripLocal.getTripId(), tripLocal.getLocalId())).withSelfRel(),
                linkTo(methodOn(TripLocalController.class).getAllTripLocals(Pageable.unpaged())).withRel("tripLocals"));
    }
}
