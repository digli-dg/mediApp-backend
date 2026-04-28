package com.mitocode.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.dto.*;
import com.mitocode.model.Consult;
import com.mitocode.model.Exam;
import com.mitocode.model.MediaFile;
import com.mitocode.service.IConsultService;
import com.mitocode.service.IMediaFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/consults")
@RequiredArgsConstructor
public class ConsultController {

    private final IConsultService service;
    @Qualifier("consultMapper")
    private final ModelMapper mapper;
    private final Cloudinary cloudinary;
    private final IMediaFileService mfService;

    @GetMapping
    public ResponseEntity<List<ConsultDTO>> findAll() throws Exception{
        List<ConsultDTO> list = service.findAll().stream().map(this::convertToDto).toList();

        return new ResponseEntity<>(list, OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultDTO> findById(@PathVariable("id") Integer id) throws Exception{
        Consult obj = service.findById(id);

        return new ResponseEntity<>(convertToDto(obj), OK);
    }


    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody ConsultListExamDTO dto) throws Exception{
        Consult cons = convertToEntity(dto.getConsult());
        //List<Exam> exams = dto.getLstExam().stream().map(e -> mapper.map(e, Exam.class)).toList();
        List<Exam> exams = mapper.map(dto.getLstExam(), new TypeToken<List<Exam>>(){}.getType());
        Consult obj = service.saveTransactional(cons, exams);

        //localhost:8080/consults/7
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdConsult()).toUri();

        return ResponseEntity.created(location).build();//.body(obj);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultDTO> update(@Valid @RequestBody ConsultDTO dto, @PathVariable("id") Integer id) throws Exception{
        dto.setIdConsult(id);
        Consult obj = service.update(convertToEntity(dto), id);

        return new ResponseEntity<>(convertToDto(obj), OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws Exception{
        service.delete(id);

        return new ResponseEntity<>(NO_CONTENT);
    }

    @GetMapping("/hateoas/{id}")
    public EntityModel<ConsultDTO> findByIdHateoas(@PathVariable("id") Integer id) throws Exception {
        EntityModel<ConsultDTO> resource = EntityModel.of(convertToDto(service.findById(id)));

        //generar links informativos
        //localhost:8080/consults/1
        WebMvcLinkBuilder link1 = linkTo(methodOn(this.getClass()).findById(id));
        WebMvcLinkBuilder link2 = linkTo(methodOn(this.getClass()).findAll());

        resource.add(link1.withRel("consult-info"));
        resource.add(link2.withRel("consults-info"));

        return resource;
    }

    @PostMapping("/search/others")
    public ResponseEntity<List<ConsultDTO>> searchByOthers(@RequestBody FilterConsultDTO filterDTO){
        List<Consult> consults = service.search(filterDTO.getDni(), filterDTO.getFullname());
        //List<ConsultDTO> consultDTOS = consults.stream().map(this::convertToDto).toList();
        List<ConsultDTO> consultDTOS = mapper.map(consults, new TypeToken<List<ConsultDTO>>(){}.getType());

        return new ResponseEntity<>(consultDTOS, OK);
    }

    @GetMapping("/search/dates")
    public ResponseEntity<List<ConsultDTO>> searchByDates(
            @RequestParam(value = "date1", defaultValue = "2023-14-11", required = true) String date1,
            @RequestParam(value = "date2", defaultValue = "2023-14-11", required = true) String date2
    ){
        List<Consult> consults = service.searchByDates(LocalDateTime.parse(date1), LocalDateTime.parse(date2));
        List<ConsultDTO> consultDTOS = mapper.map(consults, new TypeToken<List<ConsultDTO>>(){}.getType());

        return new ResponseEntity<>(consultDTOS, OK);
    }

    @GetMapping("/callProcedureNative")
    public ResponseEntity<List<ConsultProcDTO>> callProcedureOrFunctionNative(){
        return new ResponseEntity<>(service.callProcedureOrFunctionNative(), OK);
    }

    @GetMapping("/callProcedureProjection")
    public ResponseEntity<List<IConsultProcDTO>> callProcedureProjection(){
        return new ResponseEntity<>(service.callProcedureOrFunctionProjection(), OK);
    }

    @GetMapping(value = "/generateReport", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateReport() throws Exception{
        byte[] data = service.generateReport();

        return new ResponseEntity<>(data, OK);
    }

    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFile(@RequestParam("file") MultipartFile file) throws Exception{
        //DB
        MediaFile mf = new MediaFile();
        mf.setFileType(file.getContentType());
        mf.setFilename(file.getOriginalFilename());
        mf.setValue(file.getBytes());

        mfService.save(mf);

        //Repo Externo
        /*File f = this.convertToFile(file);
        Map response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
        JSONObject json = new JSONObject(response);
        String url = json.getString("url");

        System.out.println(url);
        //mfService.update(url);*/

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/readFile/{idFile}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> readFile(@PathVariable("idFile") Integer idFile) throws Exception {

        byte[] arr = mfService.findById(idFile).getValue();

        return new ResponseEntity<>(arr, HttpStatus.OK);
    }

    public File convertToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(multipartFile.getBytes());
        outputStream.close();
        return file;
    }

    private ConsultDTO convertToDto(Consult obj){
        return mapper.map(obj, ConsultDTO.class);
    }

    private Consult convertToEntity(ConsultDTO dto){
        return mapper.map(dto, Consult.class);
    }
}
