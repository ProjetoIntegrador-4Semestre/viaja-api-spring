package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MediaModelAssembler implements RepresentationModelAssembler<Media, EntityModel<Media>> {

    @Override
    public EntityModel<Media> toModel(Media media) {
        return EntityModel.of(media,
                linkTo(methodOn(MediaController.class).getMediaById(media.getId())).withSelfRel(),
                linkTo(methodOn(MediaController.class).getAllMedias(Pageable.unpaged())).withRel("medias"));
    }
}
