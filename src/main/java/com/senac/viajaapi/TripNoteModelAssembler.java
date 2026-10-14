package com.senac.viajaapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TripNoteModelAssembler implements RepresentationModelAssembler<TripNote, EntityModel<TripNote>> {

    @Override
    public EntityModel<TripNote> toModel(TripNote tripNote) {
        return EntityModel.of(tripNote,
                linkTo(methodOn(TripNoteController.class).getTripNoteById(tripNote.getId())).withSelfRel(),
                linkTo(methodOn(TripNoteController.class).getTripNotesByTrip(tripNote.getTripId(), Pageable.unpaged())).withRel("tripNotes"));
    }
}
