package com.mitocode.repo;

import com.mitocode.dto.IConsultProcDTO;
import com.mitocode.model.Consult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

//@Repository
public interface IConsultRepo extends IGenericRepo<Consult, Integer> {

    //FR: Jaime Medina -> jaime medina
    //BK BD// jaime medina
    @Query("FROM Consult c WHERE c.patient.dni = :dni OR LOWER(c.patient.firstName) LIKE %:fullname% OR LOWER(c.patient.lastName) LIKE %:fullname%" )
    List<Consult> search(@Param("dni") String dni, @Param("fullname") String fullname);

    //>=    | <
    //01-11-2023 al 14-11-2023
    @Query("FROM Consult c WHERE c.consultDate BETWEEN :date1 AND :date2")
    List<Consult> searchByDates(@Param("date1") LocalDateTime date1, @Param("date2") LocalDateTime date2);

    @Query(value = "select * from fn_list()" , nativeQuery = true)
    List<Object[]> callProcedureOrFunctionNative();

    @Query(value = "select * from fn_list()" , nativeQuery = true)
    List<IConsultProcDTO> callProcedureOrFunctionProjection();

    /*
    [1,	"09/11/2023"],
    [1,	"14/11/2023"],
    [6,	"31/10/2023"]

     */
}
