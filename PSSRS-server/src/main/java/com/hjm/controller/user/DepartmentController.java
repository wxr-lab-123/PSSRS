package com.hjm.controller.user;

import com.hjm.pojo.Entity.Department;
import com.hjm.result.Result;
import com.hjm.service.IDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDepartmentController")
@RequestMapping("/api/department")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {
    private final IDepartmentService departmentService;
    @GetMapping("/list")
    public Result<List<Department>> list() {
        log.info("查询所有科室");
        return Result.success(departmentService.list());
    }
}
