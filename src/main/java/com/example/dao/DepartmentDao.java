package com.example.dao;

import com.example.bean.Department;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DepartmentDao {
    // 模擬資料庫中的資料

    private static Map<Integer, Department> departments = null;

    static {
        departments = new HashMap<Integer,Department>(); // 創建一個部門表

        departments.put(101,new Department(101,"AAA部門"));
        departments.put(102,new Department(102,"BBB部門"));
        departments.put(103,new Department(103,"CCC部門"));
        departments.put(104,new Department(104,"DDD部門"));
        departments.put(105,new Department(105,"EEE部門"));
    }

    // 獲得所有部門訊息
    public Collection<Department> getDepartments(){
        return departments.values();
    }

    // 通過id得到部門
    public Department getDepartmentById(Integer id){
        return departments.get(id);
    }


}