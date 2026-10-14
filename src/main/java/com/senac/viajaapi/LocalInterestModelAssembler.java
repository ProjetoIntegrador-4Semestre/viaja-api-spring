package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class LocalInterestModelAssembler implements RepresentationModelAssembler<LocalInterest, EntityModel<LocalInterest>> {

    @Override
    public EntityModel<LocalInterest> toModel(LocalInterest localInterest) {
        return EntityModel.of(localInterest,
                linkTo(methodOn(LocalInterestController.class).getInterestsByLocal(localInterest.getLocalId(), Pageable.unpaged())).withRel("localInterests"));
    }
}
