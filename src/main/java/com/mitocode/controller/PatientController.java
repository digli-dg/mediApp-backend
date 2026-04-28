package com.mitocode.controller;

import com.mitocode.dto.PatientDTO;
import com.mitocode.dto.PatientRecord;
import com.mitocode.model.Patient;
import com.mitocode.service.IPatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.*;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class PatientController {

    private final IPatientService service;
    @Qualifier("defaultMapper")
    private final ModelMapper mapper;

    @GetMapping
    public ResponseEntity<List<PatientDTO>> findAll() throws Exception{
        /*List<PatientRecord> list = service.findAll().stream().map(e -> {
            PatientRecord dto = new PatientRecord(e.getIdPatient(), e.getFirstName(), e.getLastName(), e.getDni(), e.getAddress(), e.getPhone(), e.getEmail());

            return dto;
        }).toList();*/
        //ModelMapper mapper = new ModelMapper();
        List<PatientDTO> list = service.findAll().stream().map(this::convertToDto).toList(); //e -> convertToDto(e)

        return new ResponseEntity<>(list, OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> findById(@PathVariable("id") Integer id) throws Exception{
        Patient obj = service.findById(id);

        return new ResponseEntity<>(convertToDto(obj), OK);
    }

    /*@PostMapping
    public ResponseEntity<Patient> save(@RequestBody Patient patient) throws Exception{
        Patient obj = service.save(patient);

        return new ResponseEntity<>(obj, CREATED);
    }*/

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody PatientDTO dto) throws Exception{
        Patient obj = service.save(convertToEntity(dto));

        //localhost:8080/patients/7
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdPatient()).toUri();

        return ResponseEntity.created(location).build();//.body(obj);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> update(@Valid @RequestBody PatientDTO dto, @PathVariable("id") Integer id) throws Exception{
        dto.setIdPatient(id);
        Patient obj = service.update(convertToEntity(dto), id);

        return new ResponseEntity<>(convertToDto(obj), OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws Exception{
        service.delete(id);

        return new ResponseEntity<>(NO_CONTENT);
    }

    @GetMapping("/hateoas/{id}")
    public EntityModel<PatientDTO> findByIdHateoas(@PathVariable("id") Integer id) throws Exception {
        EntityModel<PatientDTO> resource = EntityModel.of(convertToDto(service.findById(id)));

        //generar links informativos
        //localhost:8080/patients/1
        WebMvcLinkBuilder link1 = linkTo(methodOn(this.getClass()).findById(id));
        WebMvcLinkBuilder link2 = linkTo(methodOn(this.getClass()).findAll());

        resource.add(link1.withRel("patient-info"));
        resource.add(link2.withRel("patients-info"));

        return resource;
    }

    @GetMapping("/pageable")
    public ResponseEntity<Page<PatientDTO>> listPage(Pageable pageable){
        Page<PatientDTO> page = service.listPage(pageable).map(p -> mapper.map(p, PatientDTO.class));

        return new ResponseEntity<>(page, OK);
    }

    private PatientDTO convertToDto(Patient obj){
        return mapper.map(obj, PatientDTO.class);
    }

    private Patient convertToEntity(PatientDTO dto){
        return mapper.map(dto, Patient.class);
    }
}
