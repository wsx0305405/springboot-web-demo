package com.example.controller;

import com.example.bean.Department;
import com.example.bean.Employee;
import com.example.dao.DepartmentDao;
import com.example.dao.EmployeeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
public class EmployeeController {

    @Autowired
    EmployeeDao employeeDao;

    @Autowired
    DepartmentDao departmentDao;

    @RequestMapping("/emps")
    public String list(Model model){
        Collection<Employee> employees = employeeDao.getAll();
        model.addAttribute("emps",employees);
        return "employee/list";
    }

    @GetMapping("/emp")
    public String toAddpage(Model model){
        // 查出所有部門的訊息
        Collection<Department> departments = departmentDao.getDepartments();
        model.addAttribute("departments",departments);
        return "employee/add";
    }

    @PostMapping("/emp")
    public String addEmp(Employee employee){
        // 添加的操作
        employeeDao.save(employee); // 調用底層業務方法 保存員工訊息
        return "redirect:/emps";
    }

    // 員工修改頁面
    @GetMapping ("/emp/{id}")
    public String toUpdateEmp(@PathVariable("id")Integer id,Model model){
        // 查出原來的數據
        Employee employee = employeeDao.getEmployeeById(id);
        model.addAttribute("emp",employee);
        // 查出所有部門的訊息
        Collection<Department> departments = departmentDao.getDepartments();
        model.addAttribute("departments",departments);

        return "employee/update";
    }

    @PostMapping("/updateEmp")
    public String updateEmp(Employee employee){
        System.out.println("update emp = " + employee);
        employeeDao.save(employee);
        return "redirect:/emps";
    }

    @GetMapping("/delemp/{id}")
    public String deleteEmp(@PathVariable("id")int id){
        employeeDao.delete(id);
        return "redirect:/emps";
    }

}
