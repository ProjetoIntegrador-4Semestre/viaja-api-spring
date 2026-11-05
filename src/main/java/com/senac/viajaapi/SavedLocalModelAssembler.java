package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SavedLocalModelAssembler implements RepresentationModelAssembler<SavedLocal, EntityModel<SavedLocal>> {

    @Override
    public EntityModel<SavedLocal> toModel(SavedLocal savedLocal) {
        return EntityModel.of(savedLocal,
                linkTo(methodOn(SavedLocalController.class).getSavedLocal(savedLocal.getUserId(), savedLocal.getLocalId())).withSelfRel(),
                linkTo(methodOn(SavedLocalController.class).getSavedLocalsByUser(savedLocal.getUserId(), Pageable.unpaged())).withRel("savedLocals"));
    }
}
