package com.example.dao;

import com.example.bean.Department;
import com.example.bean.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class EmployeeDao {
    // 模擬資料庫中的資料  員工ID + 員工訊息
    private static Map<Integer, Employee> employees = null;

    // 員工有所屬的部門
    @Autowired
    private DepartmentDao departmentDao;

    static {
        employees = new HashMap<Integer, Employee>(); // 創建一個部門表

        employees.put(1001,new Employee(1001,"AA","AAA@com",1,new Department(101,"AAA部門")));
        employees.put(1002,new Employee(1002,"BB","BBB@com",0,new Department(102,"BBB部門")));
        employees.put(1003,new Employee(1003,"CC","CCC@com",1,new Department(103,"CCC部門")));
        employees.put(1004,new Employee(1004,"DD","DDD@com",0,new Department(104,"DDD部門")));
        employees.put(1005,new Employee(1005,"EE","EEE@com",1,new Department(105,"EEE部門")));
    }
    // 主鍵自增
    private static Integer initId = 1006;
    // 增加一個員工
    public void save(Employee employee){
        if(employee.getId()==null){
            employee.setId(initId++);
        }
//        // 由 employee 得到對應的部門
//        Department department = employee.getDepartment();
//        // 再由部門得到對應的部門id
//        Integer id = department.getId();
//        // 由得到的部門id 執行Dao層 藉由id得到部門
//        Department departmentById = this.departmentDao.getDepartmentById(id);
//        // 設置傳入的employee的部門
//        employee.setDepartment(departmentById);
//        // 將設定完成的employee 放入employees中

        employee.setDepartment(departmentDao.getDepartmentById(employee.getDepartment().getId()));
        employees.put(employee.getId(),employee);

    }

    // 查詢全部員工訊息
    public Collection<Employee> getAll(){

        return employees.values();
    }

    // 通過id查詢員工
    public Employee getEmployeeById(Integer id){
        return employees.get(id);

    }

    // 刪除員工 通過id
    public void delete(Integer id){
        employees.remove(id);
    }


}
