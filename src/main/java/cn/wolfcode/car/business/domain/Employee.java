package cn.wolfcode.car.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@Getter
@Setter
public class Employee {
    /** */
    private Long id;

    /** */
    private String name;

    /** */
    private String email;

    /** */
    private Integer age;

    /** */
    private Byte sex;

    /** */
    private Integer admin;

    /** */
    private Integer status;

    /** */
    private Long deptId;

    /** */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date hiredate;

}