package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InterestModelAssembler implements RepresentationModelAssembler<Interest, EntityModel<Interest>> {

    @Override
    public EntityModel<Interest> toModel(Interest interest) {
        return EntityModel.of(interest,
                linkTo(methodOn(InterestController.class).getInterestById(interest.getId())).withSelfRel(),
                linkTo(methodOn(InterestController.class).getAllInterests(Pageable.unpaged())).withRel("interests"));
    }
}
