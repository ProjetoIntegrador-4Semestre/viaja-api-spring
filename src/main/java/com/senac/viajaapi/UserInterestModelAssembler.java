package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserInterestModelAssembler implements RepresentationModelAssembler<UserInterest, EntityModel<UserInterest>> {

    @Override
    public EntityModel<UserInterest> toModel(UserInterest userInterest) {
        return EntityModel.of(userInterest,
                linkTo(methodOn(UserInterestController.class).getById(userInterest.getUserId(), userInterest.getInterestId())).withSelfRel(),
                linkTo(methodOn(UserInterestController.class).getInterestsByUser(userInterest.getUserId(), Pageable.unpaged())).withRel("userInterests"));
    }
}
