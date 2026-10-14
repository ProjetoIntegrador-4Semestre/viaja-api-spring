package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionModelAssembler implements RepresentationModelAssembler<Subscription, EntityModel<Subscription>> {

    @Override
    public EntityModel<Subscription> toModel(Subscription subscription) {
        return EntityModel.of(subscription,
                linkTo(methodOn(SubscriptionController.class).getSubscriptionById(subscription.getId())).withSelfRel(),
                linkTo(methodOn(SubscriptionController.class).getAllSubscriptions(Pageable.unpaged())).withRel("subscriptions"));
    }
}
