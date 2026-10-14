package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class LocalModelAssembler implements RepresentationModelAssembler<Local, EntityModel<Local>> {

    @Override
    public EntityModel<Local> toModel(Local local) {
        return EntityModel.of(local,
                linkTo(methodOn(LocalController.class).getPlaceById(local.getId())).withSelfRel(),
                linkTo(methodOn(LocalController.class).getAllPlaces(Pageable.unpaged())).withRel("places"));
    }
}
